package com.aiagent.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档块实体类
 *
 * @author ego
 * @date 2025-11-29
 */
@Data
public class DocumentChunk {
    /**
     * 块ID
     */
    private String id;

    /**
     * 文档ID
     */
    private String documentId;

    /**
     * 块索引
     */
    private Integer chunkIndex;

    /**
     * 块内容
     */
    private String content;

    /**
     * 向量ID（在向量存储中的ID）
     */
    private String vectorId;

    /**
     * 向量嵌入
     */
    private float[] embedding;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 删除时间（软删除）
     */
    private LocalDateTime deletedAt;
}

