package com.aiagent.service.impl;

import com.aiagent.dto.ChatRequest;
import com.aiagent.dto.ChatResponse;
import com.aiagent.service.ChatService;
import com.aiagent.service.RAGService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 对话服务实现类
 *
 * @author ego
 * @date 2025-11-29
 */
@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    @Autowired(required = false)
    private RAGService ragService;

    @Autowired(required = false)
    private ChatClient chatClient;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.ai.ollama.chat.options.model:deepseek-coder}")
    private String model;

    @Value("${spring.ai.rag.top-k:5}")
    private int ragTopK;

    @Override
    public ChatResponse sendMessage(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("========== 开始处理对话请求 ==========");
            log.info("模型: {}", model);
            log.info("用户消息: {}", request.getMessage());
            log.info("使用RAG: {}", request.getUseRAG());

            String prompt = request.getMessage();
            List<ChatResponse.MessageSource> sources = new ArrayList<>();

            // 如果启用 RAG，检索相关文档
            if (request.getUseRAG() != null && request.getUseRAG() && ragService != null) {
                log.info("启用 RAG，开始检索相关文档...");
                var contextChunks = ragService.retrieveContext(request.getMessage(), ragTopK);

                if (!contextChunks.isEmpty()) {
                    log.info("检索到 {} 个相关文档块", contextChunks.size());
                    prompt = ragService.buildRAGPrompt(request.getMessage(), contextChunks);

                    // 构建来源信息
                    sources = contextChunks.stream()
                            .map(chunk -> ChatResponse.MessageSource.builder()
                                    .documentId(chunk.getDocumentId())
                                    .chunkIndex(chunk.getChunkIndex()).build())
                            .collect(Collectors.toList());
                } else {
                    log.warn("未检索到相关文档，使用普通对话模式");
                }
            }

            // 调用 AI 生成回复（优先使用 Spring AI ChatClient，降级到直接 HTTP 调用）
            String aiMessage = callOllama(prompt);

            long endTime = System.currentTimeMillis();
            double responseTime = (endTime - startTime) / 1000.0;

            log.info("AI回复: {}", aiMessage.substring(0, Math.min(aiMessage.length(), 100)) + "...");
            log.info("响应时间: {}秒", responseTime);
            log.info("========== 对话处理成功 ==========");

            return ChatResponse.builder().message(aiMessage)
                    .conversationId(
                            request.getConversationId() != null ? request.getConversationId()
                                    : UUID.randomUUID().toString())
                    .messageId(UUID.randomUUID().toString())
                    .sources(sources.isEmpty() ? null : sources)
                    .metadata(ChatResponse.Metadata.builder().model(model).tokens(0) // TODO: 从响应中提取
                            .responseTime(responseTime).build())
                    .build();

        } catch (Exception e) {
            log.error("处理对话请求失败", e);
            throw new RuntimeException("AI 服务调用失败: " + e.getMessage(), e);
        }
    }

    /**
     * 调用 Ollama 生成回复 优先使用 Spring AI ChatClient，然后是 RestTemplate，最后是直接 HTTP 调用
     *
     * @param prompt 提示词
     * @return AI 生成的回复
     */
    private String callOllama(String prompt) {
        try {
            // 优先使用 Spring AI ChatClient（最佳方案）
            if (chatClient != null) {
                log.info("使用 Spring AI ChatClient 调用 AI 模型");
                return chatClient.prompt(prompt).call().content();
            }

            // 使用 RestTemplate 调用（改进方案）
            return callOllamaWithRestTemplate(prompt);
        } catch (Exception e) {
            log.warn("RestTemplate 调用失败，降级到直接 HTTP 调用: {}", e.getMessage());
            // 降级到直接 HTTP 调用
            try {
                return callOllamaDirectly(prompt);
            } catch (Exception ex) {
                log.error("直接 HTTP 调用也失败", ex);
                throw new RuntimeException("AI 服务调用失败: " + ex.getMessage(), ex);
            }
        }
    }

    /**
     * 使用 RestTemplate 调用 Ollama API（改进方案） 使用 Spring 的 RestTemplate 和 Map 处理 JSON，更简洁可靠
     *
     * @param prompt 提示词
     * @return AI 生成的回复
     */
    private String callOllamaWithRestTemplate(String prompt) {
        String url = "http://localhost:11434/api/generate";

        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        log.debug("使用 RestTemplate 调用 Ollama API，模型: {}, 提示词长度: {}", model, prompt.length());

        // 发送请求
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url, HttpMethod.POST,
                request, new ParameterizedTypeReference<Map<String, Object>>() {});

        if (response.getStatusCode().is2xxSuccessful()) {
            Map<String, Object> body = response.getBody();
            if (body != null) {
                String result = (String) body.get("response");
                log.debug("RestTemplate 调用成功，响应长度: {}", result != null ? result.length() : 0);
                return result != null ? result : "AI 未返回有效响应";
            } else {
                throw new RuntimeException("Ollama API 返回空响应");
            }
        } else {
            throw new RuntimeException("Ollama API 调用失败，状态码: " + response.getStatusCode());
        }
    }

    /**
     * 直接调用 Ollama API（降级方案） 当 Spring AI ChatClient 不可用时使用此方法
     *
     * @param prompt 提示词
     * @return AI 生成的回复
     * @throws Exception 调用失败时抛出异常
     */
    private String callOllamaDirectly(String prompt) throws Exception {
        java.net.URI uri = java.net.URI.create("http://localhost:11434/api/generate");
        java.net.URL url = uri.toURL();
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(60000);

        String requestBody = String.format("{\"model\":\"%s\",\"prompt\":\"%s\",\"stream\":false}",
                model, escapeJson(prompt));

        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(requestBody.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Ollama API 调用失败，状态码: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (java.io.BufferedReader reader =
                new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(),
                        java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return extractResponse(response.toString());
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t");
    }

    private String extractResponse(String jsonResponse) {
        try {
            int responseStart = jsonResponse.indexOf("\"response\":\"");
            if (responseStart == -1) {
                return "无法解析 AI 响应";
            }
            responseStart += 11;

            int responseEnd = responseStart;
            boolean escaped = false;
            for (int i = responseStart; i < jsonResponse.length(); i++) {
                char c = jsonResponse.charAt(i);
                if (escaped) {
                    escaped = false;
                    continue;
                }
                if (c == '\\') {
                    escaped = true;
                    continue;
                }
                if (c == '"' && i > responseStart) {
                    if (i + 1 < jsonResponse.length() && (jsonResponse.charAt(i + 1) == ','
                            || jsonResponse.charAt(i + 1) == '}')) {
                        responseEnd = i;
                        break;
                    }
                }
            }

            if (responseEnd == responseStart) {
                responseEnd = jsonResponse.length() - 1;
            }

            String response = jsonResponse.substring(responseStart, responseEnd);
            return response.replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t")
                    .replace("\\\"", "\"").replace("\\\\", "\\");
        } catch (Exception e) {
            log.error("解析响应失败", e);
            return "解析 AI 响应时出错: " + e.getMessage();
        }
    }
}
