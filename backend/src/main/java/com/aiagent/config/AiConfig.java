package com.aiagent.config;

import org.springframework.context.annotation.Configuration;

/**
 * AI 配置类
 * 
 * 使用 Spring Boot 自动配置，通过 application.yml 配置 Ollama
 * 
 * 注意：spring-ai-postgresql-store 在 M4 版本中可能不可用
 * VectorStore 和 EmbeddingClient 由 Spring AI 自动配置（如果依赖可用）
 * 如果依赖不可用，RAG 功能会降级运行（不使用向量检索）
 *
 * @author ego
 * @date 2025-11-29
 */
@Configuration
public class AiConfig {
    // Spring AI Ollama 通过 spring-boot-starter 自动配置
    // 配置在 application.yml 中：spring.ai.ollama.*
    // 
    // VectorStore 配置：
    // - 如果 spring-ai-postgresql-store 可用，会自动配置 PgVectorStore
    // - 如果不可用，RAG 服务会使用 required = false 的方式降级运行
}
