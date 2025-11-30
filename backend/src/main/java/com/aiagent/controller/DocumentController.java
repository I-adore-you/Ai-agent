package com.aiagent.controller;

import com.aiagent.dto.ApiResponse;
import com.aiagent.dto.PageResponse;
import com.aiagent.model.Document;
import com.aiagent.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档管理控制器
 *
 * @author ego
 * @date 2025-11-29
 */
@Slf4j
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired(required = false)
    private DocumentService documentService;

    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public ApiResponse<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title) {
        try {
            if (documentService == null) {
                return ApiResponse.error("SERVICE_UNAVAILABLE", "文档服务未配置（数据库可能未启动）");
            }

            if (file.isEmpty()) {
                return ApiResponse.error("FILE_EMPTY", "文件不能为空");
            }

            log.info("上传文档: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
            Document document = documentService.uploadDocument(file, title);
            return ApiResponse.success(document, "文档上传成功，正在处理中");
        } catch (Exception e) {
            log.error("上传文档失败", e);
            return ApiResponse.error("UPLOAD_ERROR", "上传文档失败: " + e.getMessage());
        }
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Document> getDocument(@PathVariable String id) {
        try {
            if (documentService == null) {
                return ApiResponse.error("SERVICE_UNAVAILABLE", "文档服务未配置（数据库可能未启动）");
            }

            Document document = documentService.getDocument(id);
            if (document == null) {
                return ApiResponse.error("NOT_FOUND", "文档不存在");
            }

            return ApiResponse.success(document);
        } catch (Exception e) {
            log.error("获取文档失败", e);
            return ApiResponse.error("GET_ERROR", "获取文档失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询文档列表
     */
    @GetMapping
    public ApiResponse<PageResponse<Document>> listDocuments(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            if (documentService == null) {
                return ApiResponse.error("SERVICE_UNAVAILABLE", "文档服务未配置（数据库可能未启动）");
            }

            PageResponse<Document> result = documentService.listDocuments(status, search, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取文档列表失败", e);
            return ApiResponse.error("LIST_ERROR", "获取文档列表失败: " + e.getMessage());
        }
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDocument(@PathVariable String id) {
        try {
            if (documentService == null) {
                return ApiResponse.error("SERVICE_UNAVAILABLE", "文档服务未配置（数据库可能未启动）");
            }

            documentService.deleteDocument(id);
            return ApiResponse.success(null, "文档已删除");
        } catch (Exception e) {
            log.error("删除文档失败", e);
            return ApiResponse.error("DELETE_ERROR", "删除文档失败: " + e.getMessage());
        }
    }

    /**
     * 重新处理文档
     */
    @PostMapping("/{id}/reprocess")
    public ApiResponse<Void> reprocessDocument(@PathVariable String id) {
        try {
            if (documentService == null) {
                return ApiResponse.error("SERVICE_UNAVAILABLE", "文档服务未配置（数据库可能未启动）");
            }

            documentService.reprocessDocument(id);
            return ApiResponse.success(null, "文档重新处理已启动");
        } catch (Exception e) {
            log.error("重新处理文档失败", e);
            return ApiResponse.error("REPROCESS_ERROR", "重新处理文档失败: " + e.getMessage());
        }
    }
}

