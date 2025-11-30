package com.aiagent.mapper;

import com.aiagent.model.MessageSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息来源 Mapper 接口
 *
 * @author ego
 * @date 2025-11-29
 */
@Mapper
public interface MessageSourceMapper {

    /**
     * 插入消息来源
     */
    int insert(MessageSource source);

    /**
     * 批量插入消息来源
     */
    int insertBatch(@Param("sources") List<MessageSource> sources);

    /**
     * 根据消息ID查询来源列表
     */
    List<MessageSource> selectByMessageId(@Param("messageId") String messageId);
}

