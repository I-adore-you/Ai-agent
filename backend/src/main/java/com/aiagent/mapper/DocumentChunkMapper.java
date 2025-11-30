package com.aiagent.mapper;

import com.aiagent.model.DocumentChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 文档块 Mapper 接口
 *
 * @author ego
 * @date 2025-11-29
 */
@Mapper
public interface DocumentChunkMapper {

    /**
     * 插入文档块
     */
    int insert(DocumentChunk chunk);

    /**
     * 批量插入文档块
     */
    int insertBatch(@Param("chunks") List<DocumentChunk> chunks);

    /**
     * 根据ID查询文档块
     */
    DocumentChunk selectById(@Param("id") String id);

    /**
     * 根据文档ID查询文档块列表
     */
    List<DocumentChunk> selectByDocumentId(@Param("documentId") String documentId);

    /**
     * 根据向量ID查询文档块
     */
    DocumentChunk selectByVectorId(@Param("vectorId") String vectorId);

    /**
     * 软删除文档块
     */
    int deleteById(@Param("id") String id);

    /**
     * 根据文档ID删除所有文档块
     */
    int deleteByDocumentId(@Param("documentId") String documentId);
    
    /**
     * 向量相似度搜索
     * @param embedding 查询向量
     * @param topK 返回前K个结果
     * @return 相似的文档块列表
     */
    List<DocumentChunk> searchSimilar(@Param("embedding") float[] embedding, @Param("topK") int topK);
    
    /**
     * 向量相似度搜索 - 返回相似度分数
     * @param embedding 查询向量
     * @param topK 返回前K个结果
     * @return 相似的文档块列表，包含相似度分数
     */
    List<Map<String, Object>> searchSimilarWithScore(@Param("embedding") float[] embedding, @Param("topK") int topK);
}

