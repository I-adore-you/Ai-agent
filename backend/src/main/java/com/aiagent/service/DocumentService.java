package com.aiagent.service;

import com.aiagent.dto.PageResponse;
import com.aiagent.model.Document;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文档服务接口
 *
 * @author ego
 * @date 2025-11-29
 */
public interface DocumentService {
    /**
     * 上传并处理文档
     */
    Document uploadDocument(MultipartFile file, String title);

    /**
     * 根据ID获取文档
     */
    Document getDocument(String id);

    /**
     * 分页查询文档列表
     */
    PageResponse<Document> listDocuments(String status, String search, Integer page, Integer size);

    /**
     * 删除文档
     */
    void deleteDocument(String id);

    /**
     * 重新处理文档（重新向量化）
     */
    void reprocessDocument(String id);
}

