package com.aiagent.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 对话实体类
 *
 * @author ego
 * @date 2025-11-29
 */
@Data
public class Conversation {
    /**
     * 对话ID
     */
    private String id;

    /**
     * 对话标题
     */
    private String title;

    /**
     * 对话类型：rag, agent, mcp
     */
    private String type;

    /**
     * 最后一条消息内容
     */
    private String lastMessage;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
}

