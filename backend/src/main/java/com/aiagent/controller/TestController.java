package com.aiagent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器 - 用于测试 Ollama 连接
 *
 * @author ego
 * @date 2025-11-29
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    // Spring AI ChatClient 通过自动配置注入
    // 暂时先测试 Ollama 直接连接，等确认正确的类名后再添加

    /**
     * 测试 Ollama 连接和 Spring AI
     */
    @GetMapping("/ollama")
    public Map<String, Object> testOllama(@RequestParam(defaultValue = "你好，请用一句话介绍你自己") String message) {
        Map<String, Object> result = new HashMap<>();
        
        // 测试 Ollama 服务是否可访问
        try {
            java.net.URL url = new java.net.URL("http://localhost:11434/api/tags");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            int responseCode = conn.getResponseCode();
            
            result.put("ollamaServiceAvailable", responseCode == 200);
            result.put("ollamaResponseCode", responseCode);
            
            if (responseCode == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                result.put("ollamaModels", response.toString());
            }
        } catch (Exception e) {
            result.put("ollamaServiceAvailable", false);
            result.put("ollamaError", e.getMessage());
        }
        
        // 测试直接调用 Ollama API（验证模型可用性）
        try {
            java.net.URL apiUrl = new java.net.URL("http://localhost:11434/api/generate");
            java.net.HttpURLConnection apiConn = (java.net.HttpURLConnection) apiUrl.openConnection();
            apiConn.setRequestMethod("POST");
            apiConn.setRequestProperty("Content-Type", "application/json");
            apiConn.setDoOutput(true);
            apiConn.setConnectTimeout(5000);
            apiConn.setReadTimeout(30000);
            
            String requestBody = "{\"model\":\"deepseek-coder\",\"prompt\":\"" + message + "\",\"stream\":false}";
            apiConn.getOutputStream().write(requestBody.getBytes("UTF-8"));
            
            int apiResponseCode = apiConn.getResponseCode();
            result.put("directApiTest", apiResponseCode == 200);
            
            if (apiResponseCode == 200) {
                java.io.BufferedReader apiReader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(apiConn.getInputStream()));
                StringBuilder apiResponse = new StringBuilder();
                String apiLine;
                while ((apiLine = apiReader.readLine()) != null) {
                    apiResponse.append(apiLine);
                }
                apiReader.close();
                
                result.put("directApiResponse", apiResponse.toString());
                result.put("ollamaModelStatus", "✅ deepseek-coder 模型可用");
            }
        } catch (Exception e) {
            result.put("directApiTest", false);
            result.put("directApiError", e.getMessage());
        }
        
        result.put("springAiStatus", "⚠️ Spring AI 配置待完善（需要确认正确的类名）");
        result.put("hint", "当前已通过直接 API 调用验证 Ollama 模型可用");
        
        return result;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("message", "后端服务运行正常");
        return result;
    }
}

