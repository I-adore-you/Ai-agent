package com.aiagent.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文档实体类
 *
 * @author ego
 * @date 2025-11-29
 */
@Data
public class Document {
    /**
     * 文档ID
     */
    private String id;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 处理状态：processing, completed, failed
     */
    private String status;

    /**
     * 文档块数量
     */
    private Integer chunkCount;

    /**
     * 错误信息
     */
    private String errorMessage;

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

