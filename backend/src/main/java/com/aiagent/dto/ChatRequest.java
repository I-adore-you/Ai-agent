package com.aiagent.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 对话请求 DTO
 *
 * @author ego
 * @date 2025-11-29
 */
@Data
public class ChatRequest {
    /**
     * 用户消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 对话ID（可选，用于继续对话）
     */
    private String conversationId;

    /**
     * 是否使用 RAG（检索增强生成）
     */
    private Boolean useRAG = true;

    /**
     * 是否使用 Agent 模式
     */
    private Boolean useAgent = false;

    /**
     * 温度参数（0-1）
     */
    private Double temperature;

    /**
     * 最大 Token 数
     */
    private Integer maxTokens;
}

