package com.aiagent.service.impl;

import com.aiagent.dto.PageResponse;
import com.aiagent.mapper.DocumentChunkMapper;
import com.aiagent.mapper.DocumentMapper;
import com.aiagent.model.Document;
import com.aiagent.model.DocumentChunk;
import com.aiagent.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.web.client.RestTemplate;

/**
 * 文档服务实现类
 *
 * @author ego
 * @date 2025-11-29
 */
@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    @Autowired(required = false)
    private DocumentMapper documentMapper;

    @Autowired(required = false)
    private DocumentChunkMapper documentChunkMapper;

    @Autowired(required = false)
    private OllamaApi ollamaApi;

    @Value("${spring.ai.rag.chunk-size:1000}")
    private int chunkSize;

    @Value("${spring.ai.rag.chunk-overlap:200}")
    private int chunkOverlap;
    
    @Value("${spring.ai.embedding.ollama.base-url:http://localhost:11434}")
    private String embeddingBaseUrl;
    
    @Value("${spring.ai.embedding.ollama.options.model:nomic-embed-text}")
    private String embeddingModel;
    
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public Document uploadDocument(MultipartFile file, String title) {
        Document document = new Document();
        document.setId(UUID.randomUUID().toString());
        document.setTitle(title != null ? title : file.getOriginalFilename());
        document.setFileName(file.getOriginalFilename());
        document.setFileType(getFileType(file.getOriginalFilename()));
        document.setFileSize(file.getSize());
        document.setStatus("processing");
        document.setChunkCount(0);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());

        if (documentMapper != null) {
            documentMapper.insert(document);
            log.info("创建文档记录: {}", document.getId());
        } else {
            log.warn("DocumentMapper 未配置，使用内存存储（仅用于测试）");
        }

        // 异步处理文档
        processDocumentAsync(document, file);

        return document;
    }

    @Async
    private void processDocumentAsync(Document document, MultipartFile file) {
        try {
            log.info("开始处理文档: {}", document.getId());

            // 1. 解析文档内容
            String content = parseDocument(file);
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("文档内容为空");
            }

            // 2. 文档分块
            List<String> chunks = chunkText(content);
            log.info("文档分块完成，共 {} 块", chunks.size());

            // 3. 向量化并存储
            if (documentChunkMapper != null) {
                List<DocumentChunk> documentChunks = new ArrayList<>();
                
                for (int i = 0; i < chunks.size(); i++) {
                    String chunkText = chunks.get(i);
                    
                    // 创建文档块记录
                    DocumentChunk chunk = new DocumentChunk();
                    chunk.setId(UUID.randomUUID().toString());
                    chunk.setDocumentId(document.getId());
                    chunk.setChunkIndex(i);
                    chunk.setContent(chunkText);
                    chunk.setVectorId(null);
                    
                    // 生成向量嵌入
                    float[] embedding = generateEmbedding(chunkText);
                    chunk.setEmbedding(embedding);
                    
                    chunk.setCreatedAt(LocalDateTime.now());
                    
                    documentChunks.add(chunk);
                }

                // 批量保存文档块到数据库
                documentChunkMapper.insertBatch(documentChunks);
                log.info("保存 {} 个文档块到数据库，包含向量嵌入", documentChunks.size());
            }

            // 更新文档状态
            document.setStatus("completed");
            document.setChunkCount(chunks.size());
            document.setUpdatedAt(LocalDateTime.now());
            
            if (documentMapper != null) {
                documentMapper.update(document);
            }
            
            log.info("文档处理完成: {}", document.getId());
        } catch (Exception e) {
            log.error("处理文档失败: {}", document.getId(), e);
            document.setStatus("failed");
            document.setErrorMessage(e.getMessage());
            document.setUpdatedAt(LocalDateTime.now());
            if (documentMapper != null) {
                documentMapper.update(document);
            }
        }
    }

    /**
     * 解析文档内容
     */
    private String parseDocument(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String fileType = getFileType(fileName).toLowerCase();
        
        // 目前只支持文本文件，后续可以扩展支持 PDF、Word 等
        if (fileType.equals("txt") || fileType.equals("md") || fileType.equals("text")) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } else {
            // 尝试按文本读取
            try {
                return new String(file.getBytes(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new UnsupportedOperationException("暂不支持的文件类型: " + fileType);
            }
        }
    }

    /**
     * 文本分块 - 考虑语义完整性
     */
    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        // 预处理：替换多余的换行符和空格
        text = text.replaceAll("\\n{3,}", "\\n\\n")
                  .replaceAll("\\s{2,}", " ");
        
        int textLength = text.length();
        int start = 0;

        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);
            
            // 尝试在语义边界截断，优先级：段落 > 句子 > 逗号
            if (end < textLength) {
                // 1. 寻找最近的段落边界（两个连续换行）
                int lastParagraph = text.lastIndexOf("\n\n", end);
                
                // 2. 寻找最近的句子边界（句号、问号、感叹号）
                int lastSentence = Math.max(
                    text.lastIndexOf('.', end),
                    Math.max(
                        text.lastIndexOf('?', end),
                        text.lastIndexOf('!', end)
                    )
                );
                
                // 3. 寻找最近的逗号
                int lastComma = text.lastIndexOf(',', end);
                
                // 确定最佳断点
                int breakPoint = -1;
                if (lastParagraph > start + chunkSize / 3) {
                    breakPoint = lastParagraph + 2; // +2 是两个换行符
                } else if (lastSentence > start + chunkSize / 2) {
                    breakPoint = lastSentence + 1; // +1 是标点符号
                } else if (lastComma > start + chunkSize * 2 / 3) {
                    breakPoint = lastComma + 1; // +1 是逗号
                }
                
                if (breakPoint > 0) {
                    end = breakPoint;
                }
            }
            
            String chunk = text.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
            
            // 计算下一个块的起始位置（考虑重叠）
            start = Math.max(start + 1, end - chunkOverlap);
        }

        // 后处理：确保每个块至少有一定长度，合并过短的块
        List<String> finalChunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        for (String chunk : chunks) {
            if (currentChunk.length() + chunk.length() < chunkSize / 2) {
                // 如果当前块和下一个块合并后仍小于一半大小，则合并
                currentChunk.append(chunk).append("\n\n");
            } else {
                if (currentChunk.length() > 0) {
                    finalChunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                finalChunks.add(chunk);
            }
        }
        
        if (currentChunk.length() > 0) {
            finalChunks.add(currentChunk.toString().trim());
        }
        
        log.info("文本分块完成，原始 {} 字符，生成 {} 块，平均每块 {} 字符", 
                textLength, finalChunks.size(), textLength / Math.max(1, finalChunks.size()));
        
        return finalChunks;
    }

    /**
     * 获取文件类型
     */
    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "unknown";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    @Value("${spring.ai.embedding.max-retries:3}")
    private int maxRetries;
    
    @Value("${spring.ai.embedding.retry-delay:1000}")
    private long retryDelay;
    
    /**
     * 生成文本向量嵌入 - 带重试机制
     * @param text 要嵌入的文本
     * @return 向量嵌入
     */
    private float[] generateEmbedding(String text) {
        int retries = 0;
        
        while (retries < maxRetries) {
            try {
                String url = embeddingBaseUrl + "/api/embeddings";
                
                // 构建请求体
                Map<String, Object> requestBody = Map.of(
                    "model", embeddingModel,
                    "prompt", text
                );
                
                // 发送请求
                var response = restTemplate.postForObject(url, requestBody, Map.class);
                
                if (response != null && response.containsKey("embedding")) {
                    List<Double> embeddingList = (List<Double>) response.get("embedding");
                    float[] embedding = new float[embeddingList.size()];
                    for (int i = 0; i < embeddingList.size(); i++) {
                        embedding[i] = embeddingList.get(i).floatValue();
                    }
                    return embedding;
                } else {
                    throw new RuntimeException("嵌入生成失败，响应格式错误");
                }
            } catch (Exception e) {
                retries++;
                log.warn("生成嵌入失败，正在重试 ({}/{})...", retries, maxRetries, e);
                
                if (retries >= maxRetries) {
                    log.error("生成嵌入失败，已达到最大重试次数", e);
                    throw new RuntimeException("嵌入生成失败: " + e.getMessage(), e);
                }
                
                // 等待重试延迟
                try {
                    Thread.sleep(retryDelay * retries); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("嵌入生成被中断", ie);
                }
            }
        }
        
        throw new RuntimeException("生成嵌入失败，未执行任何重试");
    }

    @Override
    public Document getDocument(String id) {
        if (documentMapper != null) {
            return documentMapper.selectById(id);
        }
        log.warn("DocumentMapper 未配置，返回 null");
        return null;
    }

    @Override
    public PageResponse<Document> listDocuments(String status, String search, Integer page, Integer size) {
        if (documentMapper == null) {
            log.warn("DocumentMapper 未配置，返回空列表");
            return PageResponse.<Document>builder()
                    .items(new ArrayList<>())
                    .total(0L)
                    .page(page != null ? page : 1)
                    .size(size != null ? size : 20)
                    .build();
        }

        page = page != null && page > 0 ? page : 1;
        size = size != null && size > 0 ? size : 20;
        int offset = (page - 1) * size;

        List<Document> items = documentMapper.selectList(status, search, offset, size);
        long total = documentMapper.count(status, search);

        return PageResponse.<Document>builder()
                .items(items)
                .total(total)
                .page(page)
                .size(size)
                .build();
    }

    @Override
    @Transactional
    public void deleteDocument(String id) {
        if (documentMapper == null) {
            log.warn("DocumentMapper 未配置，跳过删除");
            return;
        }

        // 删除文档块
        if (documentChunkMapper != null) {
            documentChunkMapper.deleteByDocumentId(id);
        }

        // 软删除文档
        documentMapper.deleteById(id);
        log.info("删除文档: {}", id);
    }

    @Override
    @Transactional
    public void reprocessDocument(String id) {
        Document document = getDocument(id);
        if (document == null) {
            throw new RuntimeException("文档不存在");
        }

        // 删除旧的文档块
        if (documentChunkMapper != null) {
            documentChunkMapper.deleteByDocumentId(id);
        }

        // 重新处理（需要文件内容，这里简化处理）
        document.setStatus("processing");
        document.setChunkCount(0);
        document.setErrorMessage(null);
        document.setUpdatedAt(LocalDateTime.now());
        
        if (documentMapper != null) {
            documentMapper.update(document);
        }
        
        log.info("文档重新处理已启动: {}", id);
        // 注意：重新处理需要原始文件，这里只是标记，实际需要重新上传文件
    }
}
