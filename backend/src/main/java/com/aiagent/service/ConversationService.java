package com.aiagent.service;

import com.aiagent.dto.PageResponse;
import com.aiagent.model.Conversation;
import com.aiagent.model.Message;

import java.util.List;

/**
 * 对话服务接口
 *
 * @author ego
 * @date 2025-11-29
 */
public interface ConversationService {
    /**
     * 创建新对话
     */
    Conversation createConversation(String type);

    /**
     * 根据ID获取对话
     */
    Conversation getConversation(String id);

    /**
     * 获取对话详情（包含消息列表）
     */
    ConversationDetail getConversationDetail(String id);

    /**
     * 分页查询对话列表
     */
    PageResponse<Conversation> listConversations(String type, Integer page, Integer size);

    /**
     * 更新对话（最后一条消息、消息数量等）
     */
    void updateConversation(String conversationId, String lastMessage);

    /**
     * 删除对话
     */
    void deleteConversation(String id);

    /**
     * 对话详情（包含消息）
     */
    class ConversationDetail extends Conversation {
        private List<Message> messages;

        public List<Message> getMessages() {
            return messages;
        }

        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }
    }
}

