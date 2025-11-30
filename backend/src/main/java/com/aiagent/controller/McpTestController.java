package com.aiagent.controller;

import com.aiagent.dto.ApiResponse;
import com.aiagent.service.McpToolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MCP 测试控制器
 * 
 * 演示如何使用 Spring AI 的 ChatClient 调用集成了 MCP 的 AI 模型
 * 
 * @author ego
 * @date 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/api/mcp/test")
public class McpTestController {

    @Autowired(required = false)
    private ChatClient chatClient;

    @Autowired
    private McpToolService mcpToolService;

    /**
     * 测试 MCP 工具调用
     * 
     * @param request 请求体，包含 message 字段
     * @return API 响应
     */
    @PostMapping("/chat")
    public ApiResponse<String> testMcpChat(@RequestBody Map<String, String> request) {
        try {
            String message = request.get("message");
            if (message == null || message.isEmpty()) {
                return ApiResponse.error("消息不能为空");
            }

            log.info("测试 MCP 聊天，消息: {}", message);

            if (chatClient == null) {
                return ApiResponse.error("ChatClient 未配置");
            }

            // 创建提示模板
            PromptTemplate promptTemplate = new PromptTemplate(message);
            Prompt prompt = promptTemplate.create();

            // ChatClient 会自动检测并调用 MCP 工具
            String aiResponse = chatClient.prompt(prompt).call().content();

            log.info("MCP 聊天响应: {}", aiResponse);
            return ApiResponse.success(aiResponse);
        } catch (Exception e) {
            log.error("测试 MCP 聊天失败", e);
            return ApiResponse.error("测试 MCP 聊天失败: " + e.getMessage());
        }
    }

    /**
     * 直接测试系统信息工具
     * 
     * @return 系统信息
     */
    @PostMapping("/system-info")
    public ApiResponse<Map<String, Object>> testSystemInfo() {
        try {
            Map<String, Object> systemInfo = mcpToolService.getSystemInfo();
            return ApiResponse.success(systemInfo);
        } catch (Exception e) {
            log.error("测试系统信息工具失败", e);
            return ApiResponse.error("测试系统信息工具失败: " + e.getMessage());
        }
    }

    /**
     * 直接测试创建文件工具
     * 
     * @param request 请求体，包含 filePath 和 content 字段
     * @return 创建结果
     */
    @PostMapping("/create-file")
    public ApiResponse<Map<String, Object>> testCreateFile(
            @RequestBody Map<String, String> request) {
        try {
            String filePath = request.get("filePath");
            String content = request.get("content");

            if (filePath == null || filePath.isEmpty()) {
                return ApiResponse.error("文件路径不能为空");
            }

            Map<String, Object> result = mcpToolService.createFile(filePath, content);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("测试创建文件工具失败", e);
            return ApiResponse.error("测试创建文件工具失败: " + e.getMessage());
        }
    }

    /**
     * 直接测试读取文件工具
     * 
     * @param request 请求体，包含 filePath 字段
     * @return 文件内容
     */
    @PostMapping("/read-file")
    public ApiResponse<Map<String, Object>> testReadFile(@RequestBody Map<String, String> request) {
        try {
            String filePath = request.get("filePath");

            if (filePath == null || filePath.isEmpty()) {
                return ApiResponse.error("文件路径不能为空");
            }

            Map<String, Object> result = mcpToolService.readFile(filePath);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("测试读取文件工具失败", e);
            return ApiResponse.error("测试读取文件工具失败: " + e.getMessage());
        }
    }
}
