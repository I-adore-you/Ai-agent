package com.aiagent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 对话响应 DTO
 *
 * @author ego
 * @date 2025-11-29
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    /**
     * AI 回复消息
     */
    private String message;

    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 参考来源
     */
    private List<MessageSource> sources;

    /**
     * 元数据
     */
    private Metadata metadata;

    /**
     * 消息来源
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageSource {
        private String documentId;
        private String documentTitle;
        private Integer chunkIndex;
        private Double similarity;
    }

    /**
     * 元数据
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private String model;
        private Integer tokens;
        private Double responseTime;
    }
}

