package com.aiagent.service.impl;

import com.aiagent.dto.PageResponse;
import com.aiagent.mapper.ConversationMapper;
import com.aiagent.mapper.MessageMapper;
import com.aiagent.model.Conversation;
import com.aiagent.model.Message;
import com.aiagent.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 对话服务实现类
 *
 * @author ego
 * @date 2025-11-29
 */
@Slf4j
@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired(required = false)
    private ConversationMapper conversationMapper;

    @Autowired(required = false)
    private MessageMapper messageMapper;

    @Override
    @Transactional
    public Conversation createConversation(String type) {
        Conversation conversation = new Conversation();
        conversation.setId(UUID.randomUUID().toString());
        conversation.setType(type != null ? type : "rag");
        conversation.setTitle("新对话");
        conversation.setMessageCount(0);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        if (conversationMapper != null) {
            conversationMapper.insert(conversation);
            log.info("创建对话: {}", conversation.getId());
        } else {
            log.warn("ConversationMapper 未配置，使用内存存储（仅用于测试）");
        }

        return conversation;
    }

    @Override
    public Conversation getConversation(String id) {
        if (conversationMapper != null) {
            return conversationMapper.selectById(id);
        }
        log.warn("ConversationMapper 未配置，返回 null");
        return null;
    }

    @Override
    public ConversationDetail getConversationDetail(String id) {
        Conversation conversation = getConversation(id);
        if (conversation == null) {
            return null;
        }

        ConversationDetail detail = new ConversationDetail();
        detail.setId(conversation.getId());
        detail.setTitle(conversation.getTitle());
        detail.setType(conversation.getType());
        detail.setLastMessage(conversation.getLastMessage());
        detail.setMessageCount(conversation.getMessageCount());
        detail.setCreatedAt(conversation.getCreatedAt());
        detail.setUpdatedAt(conversation.getUpdatedAt());

        // 获取消息列表
        List<Message> messages = new ArrayList<>();
        if (messageMapper != null) {
            messages = messageMapper.selectByConversationId(id);
        } else {
            log.warn("MessageMapper 未配置，返回空消息列表");
        }
        detail.setMessages(messages);

        return detail;
    }

    @Override
    public PageResponse<Conversation> listConversations(String type, Integer page, Integer size) {
        if (conversationMapper == null) {
            log.warn("ConversationMapper 未配置，返回空列表");
            return PageResponse.<Conversation>builder()
                    .items(new ArrayList<>())
                    .total(0L)
                    .page(page != null ? page : 1)
                    .size(size != null ? size : 20)
                    .build();
        }

        page = page != null && page > 0 ? page : 1;
        size = size != null && size > 0 ? size : 20;
        int offset = (page - 1) * size;

        List<Conversation> items = conversationMapper.selectList(type, offset, size);
        long total = conversationMapper.count(type);

        return PageResponse.<Conversation>builder()
                .items(items)
                .total(total)
                .page(page)
                .size(size)
                .build();
    }

    @Override
    @Transactional
    public void updateConversation(String conversationId, String lastMessage) {
        if (conversationMapper == null) {
            log.warn("ConversationMapper 未配置，跳过更新");
            return;
        }

        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation != null) {
            conversation.setLastMessage(lastMessage);
            conversation.setMessageCount(conversation.getMessageCount() + 1);
            conversationMapper.update(conversation);
            log.info("更新对话: {}", conversationId);
        }
    }

    @Override
    @Transactional
    public void deleteConversation(String id) {
        if (conversationMapper != null) {
            conversationMapper.deleteById(id);
            log.info("删除对话: {}", id);
        } else {
            log.warn("ConversationMapper 未配置，跳过删除");
        }
    }
}

