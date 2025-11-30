package com.aiagent.mapper;

import com.aiagent.model.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文档 Mapper 接口
 *
 * @author ego
 * @date 2025-11-29
 */
@Mapper
public interface DocumentMapper {

    /**
     * 插入文档
     */
    int insert(Document document);

    /**
     * 根据ID查询文档
     */
    Document selectById(@Param("id") String id);

    /**
     * 分页查询文档列表
     */
    List<Document> selectList(@Param("status") String status,
                              @Param("search") String search,
                              @Param("offset") Integer offset,
                              @Param("limit") Integer limit);

    /**
     * 查询文档总数
     */
    long count(@Param("status") String status, @Param("search") String search);

    /**
     * 更新文档
     */
    int update(Document document);

    /**
     * 软删除文档
     */
    int deleteById(@Param("id") String id);
}

