package com.aiagent.service;

import com.aiagent.dto.ChatRequest;
import com.aiagent.dto.ChatResponse;

/**
 * 对话服务接口
 *
 * @author ego
 * @date 2025-11-29
 */
public interface ChatService {
    /**
     * 发送消息并获取 AI 回复
     *
     * @param request 对话请求
     * @return 对话响应
     */
    ChatResponse sendMessage(ChatRequest request);
}

