package com.aiagent.controller;

import com.aiagent.dto.ApiResponse;
import com.aiagent.dto.ChatRequest;
import com.aiagent.dto.ChatResponse;
import com.aiagent.dto.PageResponse;
import com.aiagent.mapper.MessageMapper;
import com.aiagent.model.Conversation;
import com.aiagent.model.Message;
import com.aiagent.service.ChatService;
import com.aiagent.service.ConversationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 对话控制器
 *
 * @author ego
 * @date 2025-11-29
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired(required = false)
    private ConversationService conversationService;

    @Autowired(required = false)
    private MessageMapper messageMapper;

    /**
     * 发送消息
     */
    @PostMapping
    public ApiResponse<ChatResponse> sendMessage(@Valid @RequestBody ChatRequest request) {
        try {
            log.info("========== 收到对话请求 ==========");
            log.info("用户消息: {}", request.getMessage());
            log.info("对话ID: {}", request.getConversationId());
            log.info("使用RAG: {}", request.getUseRAG());
            log.info("请求来源: {}", getRequestSource());
            
            // 如果没有对话ID，创建新对话
            String conversationId = request.getConversationId();
            if (conversationId == null || conversationId.isEmpty()) {
                if (conversationService != null) {
                    try {
                        Conversation conversation = conversationService.createConversation("rag");
                        conversationId = conversation.getId();
                        log.info("创建新对话: {}", conversationId);
                    } catch (Exception e) {
                        log.warn("创建对话失败，使用临时ID: {}", e.getMessage());
                        conversationId = UUID.randomUUID().toString();
                    }
                } else {
                    conversationId = UUID.randomUUID().toString();
                    log.warn("ConversationService 未配置，使用临时对话ID: {}", conversationId);
                }
            }

            // 保存用户消息（如果 MessageMapper 可用）
            if (messageMapper != null) {
                try {
                    Message userMessage = new Message();
                    userMessage.setId(UUID.randomUUID().toString());
                    userMessage.setConversationId(conversationId);
                    userMessage.setRole("user");
                    userMessage.setContent(request.getMessage());
                    userMessage.setCreatedAt(LocalDateTime.now());
                    messageMapper.insert(userMessage);
                    log.debug("保存用户消息: {}", userMessage.getId());
                } catch (Exception e) {
                    log.warn("保存用户消息失败: {}", e.getMessage());
                }
            }
            
            // 调用 AI 服务
            ChatResponse response = chatService.sendMessage(request);
            response.setConversationId(conversationId);
            
            // 保存 AI 回复消息（如果 MessageMapper 可用）
            if (messageMapper != null) {
                try {
                    Message aiMessage = new Message();
                    aiMessage.setId(response.getMessageId());
                    aiMessage.setConversationId(conversationId);
                    aiMessage.setRole("assistant");
                    aiMessage.setContent(response.getMessage());
                    aiMessage.setModel(response.getMetadata().getModel());
                    aiMessage.setTokens(response.getMetadata().getTokens());
                    aiMessage.setResponseTime((long)(response.getMetadata().getResponseTime() * 1000));
                    aiMessage.setCreatedAt(LocalDateTime.now());
                    messageMapper.insert(aiMessage);
                    log.debug("保存 AI 消息: {}", aiMessage.getId());
                } catch (Exception e) {
                    log.warn("保存 AI 消息失败: {}", e.getMessage());
                }
            }
            
            // 更新对话（最后一条消息）
            if (conversationService != null) {
                try {
                    conversationService.updateConversation(conversationId, response.getMessage());
                } catch (Exception e) {
                    log.warn("更新对话失败: {}", e.getMessage());
                }
            }
            
            log.info("========== 响应生成成功 ==========");
            log.info("AI回复: {}", response.getMessage().substring(0, Math.min(50, response.getMessage().length())) + "...");
            log.info("响应时间: {}秒", response.getMetadata().getResponseTime());
            
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("========== 处理对话请求失败 ==========", e);
            return ApiResponse.error("CHAT_ERROR", "处理对话请求失败: " + e.getMessage());
        }
    }

    /**
     * 获取对话列表
     */
    @GetMapping("/conversations")
    public ApiResponse<PageResponse<Conversation>> getConversations(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            if (conversationService == null) {
                return ApiResponse.error("SERVICE_UNAVAILABLE", "对话服务未配置（数据库可能未启动）");
            }
            
            PageResponse<Conversation> result = conversationService.listConversations(type, page, size);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取对话列表失败", e);
            return ApiResponse.error("LIST_ERROR", "获取对话列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取对话详情
     */
    @GetMapping("/conversations/{id}")
    public ApiResponse<ConversationService.ConversationDetail> getConversation(@PathVariable String id) {
        try {
            if (conversationService == null) {
                return ApiResponse.error("SERVICE_UNAVAILABLE", "对话服务未配置（数据库可能未启动）");
            }
            
            ConversationService.ConversationDetail detail = conversationService.getConversationDetail(id);
            if (detail == null) {
                return ApiResponse.error("NOT_FOUND", "对话不存在");
            }
            
            return ApiResponse.success(detail);
        } catch (Exception e) {
            log.error("获取对话详情失败", e);
            return ApiResponse.error("GET_ERROR", "获取对话详情失败: " + e.getMessage());
        }
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/conversations/{id}")
    public ApiResponse<Void> deleteConversation(@PathVariable String id) {
        try {
            if (conversationService == null) {
                return ApiResponse.error("SERVICE_UNAVAILABLE", "对话服务未配置（数据库可能未启动）");
            }
            
            conversationService.deleteConversation(id);
            return ApiResponse.success(null, "对话已删除");
        } catch (Exception e) {
            log.error("删除对话失败", e);
            return ApiResponse.error("DELETE_ERROR", "删除对话失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取请求来源信息（用于调试）
     */
    private String getRequestSource() {
        try {
            org.springframework.web.context.request.RequestAttributes requestAttributes = 
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes();
            if (requestAttributes instanceof org.springframework.web.context.request.ServletRequestAttributes) {
                jakarta.servlet.http.HttpServletRequest request = 
                    ((org.springframework.web.context.request.ServletRequestAttributes) requestAttributes).getRequest();
                return String.format("IP: %s, User-Agent: %s", 
                    request.getRemoteAddr(), 
                    request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            // 忽略
        }
        return "未知";
    }
}
