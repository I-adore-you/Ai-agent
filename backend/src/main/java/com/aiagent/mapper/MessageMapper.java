package com.aiagent.mapper;

import com.aiagent.model.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息 Mapper 接口
 *
 * @author ego
 * @date 2025-11-29
 */
@Mapper
public interface MessageMapper {

    /**
     * 插入消息
     */
    int insert(Message message);

    /**
     * 根据ID查询消息
     */
    Message selectById(@Param("id") String id);

    /**
     * 根据对话ID查询消息列表
     */
    List<Message> selectByConversationId(@Param("conversationId") String conversationId);

    /**
     * 软删除消息
     */
    int deleteById(@Param("id") String id);
}

