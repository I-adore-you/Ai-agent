package com.aiagent.service.impl;

import com.aiagent.mapper.DocumentChunkMapper;
import com.aiagent.model.DocumentChunk;
import com.aiagent.service.RAGService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * RAG 服务实现类
 *
 * @author ego
 * @date 2025-11-29
 */
@Slf4j
@Service
public class RAGServiceImpl implements RAGService {

    @Autowired(required = false)
    private DocumentChunkMapper documentChunkMapper;

    @Value("${spring.ai.rag.top-k:5}")
    private int defaultTopK;

    @Value("${spring.ai.embedding.ollama.base-url:http://localhost:11434}")
    private String embeddingBaseUrl;

    @Value("${spring.ai.embedding.ollama.options.model:nomic-embed-text}")
    private String embeddingModel;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public List<DocumentChunk> retrieveContext(String query, int topK) {
        if (documentChunkMapper == null) {
            log.warn("DocumentChunkMapper 未配置，RAG 检索功能暂不可用");
            return new ArrayList<>();
        }

        try {
            log.info("开始向量检索，查询: {}", query);

            // 1. 生成查询向量
            float[] queryEmbedding = generateEmbedding(query);

            // 2. 执行向量相似度搜索，获取带分数的结果
            List<Map<String, Object>> similarResults =
                    documentChunkMapper.searchSimilarWithScore(queryEmbedding, topK * 2); // 获取更多结果用于重新排序

            log.info("向量检索完成，找到 {} 个相关文档块", similarResults.size());

            // 3. 多因素重新排序
            List<DocumentChunk> rankedChunks = rerankResults(similarResults, query, topK);

            log.info("重新排序完成，最终返回 {} 个文档块", rankedChunks.size());
            return rankedChunks;
        } catch (Exception e) {
            log.error("向量检索失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 多因素重新排序
     * 
     * @param similarResults 相似结果列表
     * @param query 查询文本
     * @param topK 返回前K个结果
     * @return 重新排序后的文档块列表
     */
    private List<DocumentChunk> rerankResults(List<Map<String, Object>> similarResults,
            String query, int topK) {
        // 1. 转换为DocumentChunk对象并添加相似度分数
        List<DocumentChunkWithScore> chunksWithScore = new ArrayList<>();

        for (Map<String, Object> result : similarResults) {
            DocumentChunk chunk = new DocumentChunk();
            chunk.setId((String) result.get("id"));
            chunk.setDocumentId((String) result.get("document_id"));
            chunk.setChunkIndex((Integer) result.get("chunk_index"));
            chunk.setContent((String) result.get("content"));
            chunk.setVectorId((String) result.get("vector_id"));
            chunk.setCreatedAt((LocalDateTime) result.get("created_at"));

            Double similarityScore = (Double) result.get("similarity_score");

            chunksWithScore.add(new DocumentChunkWithScore(chunk,
                    similarityScore != null ? similarityScore.floatValue() : 0.0f));
        }

        // 2. 多因素排序
        chunksWithScore.sort((a, b) -> {
            // 主要因素：相似度分数
            int scoreCompare = Float.compare(b.getScore(), a.getScore());
            if (scoreCompare != 0) {
                return scoreCompare;
            }

            // 次要因素：文档块长度（优先选择适中长度的块）
            int aLength = a.getChunk().getContent().length();
            int bLength = b.getChunk().getContent().length();
            int idealLength = 500;
            int aDistance = Math.abs(aLength - idealLength);
            int bDistance = Math.abs(bLength - idealLength);

            return Integer.compare(aDistance, bDistance);
        });

        // 3. 提取前K个结果
        return chunksWithScore.stream().limit(topK).map(DocumentChunkWithScore::getChunk)
                .collect(Collectors.toList());
    }

    /**
     * 带分数的文档块包装类
     */
    private static class DocumentChunkWithScore {
        private final DocumentChunk chunk;
        private final float score;

        public DocumentChunkWithScore(DocumentChunk chunk, float score) {
            this.chunk = chunk;
            this.score = score;
        }

        public DocumentChunk getChunk() {
            return chunk;
        }

        public float getScore() {
            return score;
        }
    }

    @Value("${spring.ai.embedding.max-retries:3}")
    private int maxRetries;

    @Value("${spring.ai.embedding.retry-delay:1000}")
    private long retryDelay;

    /**
     * 生成文本向量嵌入 - 带重试机制
     * 
     * @param text 要嵌入的文本
     * @return 向量嵌入
     */
    private float[] generateEmbedding(String text) {
        int retries = 0;

        while (retries < maxRetries) {
            try {
                String url = embeddingBaseUrl + "/api/embeddings";

                // 构建请求体
                Map<String, Object> requestBody = Map.of("model", embeddingModel, "prompt", text);

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

    @Value("${spring.ai.rag.max-prompt-length:4000}")
    private int maxPromptLength;

    @Override
    public String buildRAGPrompt(String userMessage, List<DocumentChunk> contextChunks) {
        if (contextChunks == null || contextChunks.isEmpty()) {
            log.warn("没有检索到相关文档，使用普通对话模式");
            return userMessage;
        }

        // 1. 计算基础提示长度
        String basePrompt = String.format("""
                基于以下上下文信息回答问题。如果上下文中没有相关信息，请说明你不知道，不要编造答案。

                上下文信息：
                %s

                用户问题：%s

                请基于上下文信息回答用户问题：
                """, "", userMessage);

        int baseLength = basePrompt.length();
        int remainingLength = maxPromptLength - baseLength;

        // 2. 智能选择最相关的上下文，确保不超过最大长度
        StringBuilder context = new StringBuilder();
        int totalContextLength = 0;

        for (int i = 0; i < contextChunks.size(); i++) {
            DocumentChunk chunk = contextChunks.get(i);
            String chunkText = chunk.getContent();

            // 计算当前块添加后的总长度
            int chunkHeaderLength = String.format("【文档片段 %d】\n", i + 1).length();
            int chunkTotalLength = chunkHeaderLength + chunkText.length() + 2; // +2 是换行符

            // 如果添加当前块会超过最大长度，则停止
            if (totalContextLength + chunkTotalLength > remainingLength) {
                log.info("上下文长度超过限制，已添加 {} 个文档块，剩余 {} 字符", i, remainingLength - totalContextLength);
                break;
            }

            // 添加文档块到上下文
            context.append("【文档片段 ").append(i + 1).append("】\n");
            context.append(chunkText).append("\n\n");
            totalContextLength += chunkTotalLength;
        }

        log.info("最终上下文长度: {}, 总提示长度: {}", totalContextLength, baseLength + totalContextLength);

        // 3. 构建最终 RAG Prompt
        return String.format("""
                基于以下上下文信息回答问题。如果上下文中没有相关信息，请说明你不知道，不要编造答案。

                上下文信息：
                %s

                用户问题：%s

                请基于上下文信息回答用户问题，回答要简洁明了，直接针对问题：
                """, context.toString(), userMessage);
    }
}
