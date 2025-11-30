package com.aiagent.mapper;

import com.aiagent.model.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 对话 Mapper 接口
 *
 * @author ego
 * @date 2025-11-29
 */
@Mapper
public interface ConversationMapper {

    /**
     * 插入对话
     */
    int insert(Conversation conversation);

    /**
     * 根据ID查询对话
     */
    Conversation selectById(@Param("id") String id);

    /**
     * 分页查询对话列表
     */
    List<Conversation> selectList(@Param("type") String type,
                                   @Param("offset") Integer offset,
                                   @Param("limit") Integer limit);

    /**
     * 查询对话总数
     */
    long count(@Param("type") String type);

    /**
     * 更新对话
     */
    int update(Conversation conversation);

    /**
     * 软删除对话
     */
    int deleteById(@Param("id") String id);
}

