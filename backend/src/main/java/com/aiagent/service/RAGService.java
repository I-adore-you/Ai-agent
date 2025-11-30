package com.aiagent.service;

import com.aiagent.model.DocumentChunk;

import java.util.List;

/**
 * RAG 服务接口
 *
 * @author ego
 * @date 2025-11-29
 */
public interface RAGService {
    /**
     * 检索相关文档块
     *
     * @param query 查询文本
     * @param topK 返回前K个结果
     * @return 相关文档块列表
     */
    List<DocumentChunk> retrieveContext(String query, int topK);

    /**
     * 构建 RAG Prompt
     *
     * @param userMessage 用户消息
     * @param contextChunks 检索到的文档块
     * @return 增强后的 Prompt
     */
    String buildRAGPrompt(String userMessage, List<DocumentChunk> contextChunks);
}
