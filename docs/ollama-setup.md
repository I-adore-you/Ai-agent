# Ollama 配置指南

本文档详细说明如何在 macOS 上使用 Docker 安装的 Ollama 配置本项目。

## 前提条件

- macOS 系统
- Docker 已安装并运行
- Ollama 已通过 Docker 安装

## 1. 验证 Ollama 安装

### 检查 Ollama 容器状态

```bash
# 查看运行中的容器
docker ps | grep ollama

# 如果容器未运行，启动它
docker start <ollama-container-name>
```

### 测试 Ollama API

```bash
# 测试 API 是否可访问
curl http://localhost:11434/api/tags

# 应该返回已下载的模型列表，例如：
# {"models":[{"name":"llama2","modified_at":"2024-01-01T00:00:00Z"}]}
```

## 2. 下载模型

### 查看可用模型

访问 [Ollama 模型库](https://ollama.ai/library) 查看所有可用模型。

### 下载推荐模型

**对话模型（Chat Models）：**

```bash
# 方式 1: 如果 Ollama 在 Docker 容器中
docker exec -it <ollama-container-name> ollama pull llama2

# 方式 2: 如果 Ollama CLI 在主机上可用
ollama pull llama2

# 其他推荐模型：
ollama pull mistral        # Mistral 7B - 性能优秀
ollama pull codellama      # Code Llama - 代码生成
ollama pull llama2:13b     # Llama 2 13B - 更大模型
ollama pull qwen2.5        # Qwen 2.5 - 中文支持好
```

**Embedding 模型（用于向量化）：**

```bash
ollama pull nomic-embed-text  # 推荐，维度 768
# 或
ollama pull all-minilm       # 更小的模型
```

### 验证模型下载

```bash
curl http://localhost:11434/api/tags
```

## 3. 配置 Spring AI

### 创建配置文件

在项目 `src/main/resources/application.yml` 中配置：

```yaml
spring:
  application:
    name: ai-agent
  
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_agent_db
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
  
  # MyBatis 配置
  mybatis:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.aiagent.model
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

  # Ollama 配置
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: llama2  # 替换为您下载的模型名称
          temperature: 0.7
          num-predict: 2000  # 最大生成 token 数
    
    # Embedding 配置（如果使用 Ollama embedding）
    embedding:
      ollama:
        base-url: http://localhost:11434
        options:
          model: nomic-embed-text
    
    # 向量存储配置
    vectorstore:
      postgresql:
        index-type: HNSW
        distance-type: COSINE_DISTANCE
        dimensions: 768  # nomic-embed-text 的维度（OpenAI 为 1536）

server:
  port: 8080

logging:
  level:
    org.springframework.ai: DEBUG
    com.aiagent: DEBUG
```

### 环境变量配置（可选）

也可以使用环境变量：

```bash
export SPRING_AI_OLLAMA_BASE_URL=http://localhost:11434
export SPRING_AI_OLLAMA_CHAT_OPTIONS_MODEL=llama2
```

## 4. Docker Compose 配置

如果使用 Docker Compose 部署，在 `docker-compose.yml` 中添加：

```yaml
version: '3.8'

services:
  # 如果 Ollama 不在 Compose 中，确保端口映射正确
  # 假设 Ollama 在另一个容器中运行
  ollama:
    image: ollama/ollama:latest
    container_name: ai-agent-ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    networks:
      - ai-agent-network
    restart: unless-stopped

  postgres:
    image: pgvector/pgvector:pg15
    environment:
      POSTGRES_DB: ai_agent_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - ai-agent-network

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ai_agent_db
      SPRING_AI_OLLAMA_BASE_URL: http://ollama:11434  # 使用服务名
      SPRING_AI_OLLAMA_CHAT_OPTIONS_MODEL: llama2
    depends_on:
      - postgres
      - ollama
    networks:
      - ai-agent-network

volumes:
  postgres_data:
  ollama_data:

networks:
  ai-agent-network:
    driver: bridge
```

## 5. 测试配置

### 测试 Ollama 连接

```bash
# 测试模型是否可用
curl http://localhost:11434/api/generate -d '{
  "model": "llama2",
  "prompt": "Hello, how are you?",
  "stream": false
}'
```

### 测试 Spring AI 集成

启动应用后，检查日志中是否有 Ollama 连接成功的消息。

## 6. 模型选择建议

### 根据硬件资源选择

| 模型 | 参数量 | 内存需求 | 速度 | 适用场景 |
|------|--------|---------|------|----------|
| llama2 | 7B | ~4GB | 快 | 通用对话 |
| llama2:13b | 13B | ~8GB | 中等 | 更高质量回答 |
| mistral | 7B | ~4GB | 快 | 性能优秀 |
| codellama | 7B | ~4GB | 快 | 代码生成 |
| qwen2.5 | 7B | ~4GB | 快 | 中文支持好 |

### 根据用途选择

- **通用对话**：`llama2` 或 `mistral`
- **代码生成**：`codellama`
- **中文对话**：`qwen2.5` 或 `qwen2.5:14b`
- **高质量回答**：`llama2:13b` 或 `mistral:7b`

## 7. 性能优化

### GPU 加速（如果可用）

如果您的 Mac 支持 GPU（M1/M2/M3 芯片），可以启用 GPU 加速：

```bash
# 检查 GPU 支持
docker run --rm --gpus all nvidia/cuda:11.0-base nvidia-smi

# 在 docker-compose.yml 中添加
services:
  ollama:
    deploy:
      resources:
        reservations:
          devices:
            - driver: nvidia
              count: 1
              capabilities: [gpu]
```

### 内存优化

- 使用较小的模型（7B 而不是 13B）
- 调整 `num-predict` 参数限制生成长度
- 监控内存使用：`docker stats <ollama-container>`

### 响应速度优化

- 使用 `stream: true` 启用流式响应
- 调整 `temperature` 参数（较低值响应更快但更保守）
- 使用更快的模型（如 `mistral`）

## 8. 常见问题

### Q: Ollama 连接失败

**问题**：应用无法连接到 Ollama

**解决方案**：
1. 检查 Ollama 容器是否运行：`docker ps | grep ollama`
2. 检查端口是否正确：`curl http://localhost:11434/api/tags`
3. 如果使用 Docker Compose，确保使用服务名而不是 `localhost`
4. 检查防火墙设置

### Q: 模型未找到

**问题**：`model not found` 错误

**解决方案**：
1. 确认模型已下载：`curl http://localhost:11434/api/tags`
2. 检查配置中的模型名称是否正确
3. 重新下载模型：`ollama pull <model-name>`

### Q: 响应速度慢

**问题**：Ollama 响应很慢

**解决方案**：
1. 使用更小的模型
2. 减少 `num-predict` 参数
3. 检查系统资源（CPU/内存）
4. 考虑使用 GPU 加速

### Q: 内存不足

**问题**：Ollama 容器内存不足

**解决方案**：
1. 使用更小的模型
2. 增加 Docker 内存限制
3. 关闭其他占用内存的应用

### Q: 中文支持不好

**问题**：模型对中文理解不好

**解决方案**：
1. 使用支持中文的模型：`ollama pull qwen2.5`
2. 或使用 `qwen2.5:14b` 获得更好的中文能力

## 9. 高级配置

### 自定义模型参数

```yaml
spring.ai:
  ollama:
    base-url: http://localhost:11434
    chat:
      options:
        model: llama2
        temperature: 0.7        # 创造性（0-1）
        top-p: 0.9              # 核采样
        top-k: 40               # Top-K 采样
        num-predict: 2000       # 最大 token 数
        repeat-penalty: 1.1     # 重复惩罚
        seed: -1                # 随机种子（-1 为随机）
```

### 流式响应

Spring AI 支持流式响应，可以在 Controller 中配置：

```java
@GetMapping("/chat/stream")
public Flux<ChatResponse> chatStream(@RequestBody ChatRequest request) {
    return ragService.chatStream(request);
}
```

### 多模型切换

可以在运行时切换模型：

```yaml
spring:
  profiles:
    active: ollama-llama2  # 或 ollama-mistral

---
spring:
  config:
    activate:
      on-profile: ollama-llama2
  ai:
    ollama:
      chat:
        options:
          model: llama2

---
spring:
  config:
    activate:
      on-profile: ollama-mistral
  ai:
    ollama:
      chat:
        options:
          model: mistral
```

## 10. 监控和维护

### 查看模型使用情况

```bash
# 查看 Ollama 日志
docker logs <ollama-container-name>

# 查看资源使用
docker stats <ollama-container-name>
```

### 更新模型

```bash
# 拉取最新版本
ollama pull llama2

# 删除旧版本（可选）
ollama rm llama2
```

### 备份模型

Ollama 模型存储在容器的 `/root/.ollama` 目录，可以通过卷挂载持久化：

```yaml
volumes:
  - ollama_data:/root/.ollama
```

## 参考资源

- [Ollama 官方文档](https://github.com/ollama/ollama)
- [Spring AI Ollama 文档](https://docs.spring.io/spring-ai/reference/api/ollama.html)
- [Ollama 模型库](https://ollama.ai/library)

