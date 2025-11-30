package com.aiagent.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息实体类
 *
 * @author ego
 * @date 2025-11-29
 */
@Data
public class Message {
    /**
     * 消息ID
     */
    private String id;

    /**
     * 对话ID
     */
    private String conversationId;

    /**
     * 角色：user, assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * Token 数量
     */
    private Integer tokens;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
}

