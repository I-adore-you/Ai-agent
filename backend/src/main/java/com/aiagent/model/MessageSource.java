package com.aiagent.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息来源实体类（关联消息和文档块）
 *
 * @author ego
 * @date 2025-11-29
 */
@Data
public class MessageSource {
    /**
     * ID
     */
    private String id;

    /**
     * 消息ID
     */
    private String messageId;

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 文档块ID
     */
    private String chunkId;

    /**
     * 相似度分数
     */
    private Double similarity;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}

