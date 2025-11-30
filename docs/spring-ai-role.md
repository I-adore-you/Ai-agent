# Spring AI 在项目中的角色

## 🎯 Spring AI 的定位

### 核心定义

**Spring AI 不是 Agent 框架，而是一个 AI 集成框架（AI Integration Framework）**

- **作用**：提供统一的 API 和抽象层，简化 AI 模型集成
- **定位**：中间层/抽象层，连接你的应用和底层 AI 模型
- **类比**：类似 Spring Data JPA（数据库抽象层），但针对 AI 模型

---

## 📊 Spring AI 在架构中的位置

### 完整架构图

```
┌─────────────────────────────────────────┐
│         你的业务应用层                    │
│  ┌──────────┐  ┌──────────┐            │
│  │ Agent    │  │ RAG      │            │
│  │ Service  │  │ Service  │            │
│  └──────────┘  └──────────┘            │
│       │              │                  │
│       └──────┬───────┘                  │
│              ▼                          │
│  ┌──────────────────────────┐          │
│  │   Spring AI 抽象层       │          │
│  │  (集成框架)              │          │
│  │  - ChatClient           │          │
│  │  - EmbeddingClient      │          │
│  │  - VectorStore          │          │
│  │  - OllamaApi            │          │
│  └──────────────────────────┘          │
│       │                                  │
│       ▼                                  │
│  ┌──────────────────────────┐          │
│  │   底层 AI 模型            │          │
│  │  - Ollama (DeepSeek)     │          │
│  │  - OpenAI                │          │
│  │  - Claude                │          │
│  └──────────────────────────┘          │
└─────────────────────────────────────────┘
```

---

## 🔍 Spring AI 的具体角色

### 1. **抽象层（Abstraction Layer）**

**作用**：统一不同 AI 模型的接口

**示例**：
```java
// 使用 Spring AI 的抽象接口
ChatClient chatClient;  // 可以是 Ollama、OpenAI、Claude 等

// 调用方式统一
String response = chatClient.call(prompt);

// 不需要关心底层是 Ollama 还是 OpenAI
```

**好处**：
- ✅ 切换模型只需改配置，不改代码
- ✅ 统一的 API，降低学习成本
- ✅ 自动处理不同模型的差异

### 2. **配置管理（Configuration Management）**

**作用**：通过配置文件管理 AI 模型

**在你的项目中**：
```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: deepseek-coder
          temperature: 0.7
    
    embedding:
      ollama:
        model: nomic-embed-text
    
    vectorstore:
      postgresql:
        index-type: HNSW
        dimensions: 768
```

**好处**：
- ✅ 配置集中管理
- ✅ 环境切换方便（dev/test/prod）
- ✅ 自动配置，减少样板代码

### 3. **自动配置（Auto Configuration）**

**作用**：Spring Boot 风格的自动配置

**在你的项目中**：
```java
@Configuration
public class AiConfig {
    // Spring AI 自动配置以下 Bean：
    // - OllamaApi（如果配置了 ollama）
    // - ChatClient（如果配置了 chat）
    // - EmbeddingClient（如果配置了 embedding）
    // - VectorStore（如果配置了 vectorstore）
}
```

**好处**：
- ✅ 开箱即用
- ✅ 按需加载（如果依赖不可用，会降级）
- ✅ 减少手动配置

### 4. **组件提供者（Component Provider）**

**作用**：提供可注入的 AI 组件

**在你的项目中使用的组件**：

#### a) OllamaApi
```java
@Autowired(required = false)
private OllamaApi ollamaApi;
```
- 直接调用 Ollama API
- 用于 RAG 服务中的向量化

#### b) ChatClient（如果使用）
```java
@Autowired
private ChatClient chatClient;
```
- 统一的对话客户端
- 可以切换不同模型

#### c) EmbeddingClient（如果使用）
```java
@Autowired
private EmbeddingClient embeddingClient;
```
- 文本向量化客户端
- 用于 RAG 中的文档向量化

#### d) VectorStore（如果使用）
```java
@Autowired
private VectorStore vectorStore;
```
- 向量存储抽象
- 用于向量检索

---

## 🆚 Spring AI vs Agent 框架

### 关键区别

| 维度 | Spring AI | Agent 框架 |
|------|-----------|-----------|
| **定位** | 集成框架 | 应用框架 |
| **作用** | 连接应用和 AI 模型 | 实现 Agent 功能 |
| **提供** | 统一 API、配置管理 | 工具调用、任务规划 |
| **类比** | Spring Data JPA | Spring MVC |

### 详细对比

#### Spring AI（集成框架）

```
你的应用
    ↓ 使用
Spring AI（统一接口）
    ↓ 调用
AI 模型（Ollama/OpenAI）
```

**提供**：
- ✅ 模型接入抽象
- ✅ 配置管理
- ✅ 自动配置
- ❌ 不提供 Agent 功能

#### Agent 框架（应用框架）

```
你的应用
    ↓ 使用
Agent 框架（工具调用、任务规划）
    ↓ 使用
LLM（通过 Spring AI 或其他方式）
```

**提供**：
- ✅ 工具调用机制
- ✅ 任务规划
- ✅ 决策逻辑
- ❌ 不直接管理模型

---

## 🏗️ 在你的项目中的实际使用

### 1. 配置层面

```yaml
# application.yml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        options:
          model: deepseek-coder
```

**Spring AI 的作用**：
- 读取配置
- 自动创建 `OllamaApi` Bean
- 自动配置相关组件

### 2. 代码层面

#### 使用 Spring AI 的组件

```java
// RAGServiceImpl.java
@Autowired(required = false)
private OllamaApi ollamaApi;  // Spring AI 提供的组件

// 使用 Spring AI 的配置
@Value("${spring.ai.rag.top-k:5}")
private int defaultTopK;
```

#### 自己实现的 Agent 功能

```java
// AgentService.java（你自己实现的）
public class AgentService {
    // 这里没有直接使用 Spring AI 的 Agent 框架
    // 而是自己实现了 Agent 逻辑
    
    public ChatResponse chatWithAgent(String message) {
        // 1. 分析意图（可以调用 LLM，通过 Spring AI）
        // 2. 决定使用工具
        // 3. 执行工具
        // 4. 整合结果
    }
}
```

### 3. 数据流

```
用户请求
    ↓
你的 AgentService（自己实现）
    ↓ 需要调用 LLM 时
Spring AI（提供统一接口）
    ↓
Ollama/DeepSeek（底层模型）
    ↓
返回结果
    ↓
你的 AgentService 处理
    ↓
返回给用户
```

---

## 📋 Spring AI 提供的核心功能

### 1. ChatClient（对话客户端）

**作用**：统一的对话接口

```java
@Autowired
private ChatClient chatClient;

public String chat(String message) {
    return chatClient.call(message);
}
```

**支持的模型**：
- Ollama
- OpenAI
- Claude
- Azure OpenAI
- 等等

### 2. EmbeddingClient（向量化客户端）

**作用**：文本向量化

```java
@Autowired
private EmbeddingClient embeddingClient;

public float[] embed(String text) {
    return embeddingClient.embed(text);
}
```

### 3. VectorStore（向量存储）

**作用**：向量存储和检索抽象

```java
@Autowired
private VectorStore vectorStore;

// 存储向量
vectorStore.add(List.of(document));

// 检索相似向量
List<Document> results = vectorStore.similaritySearch(
    SearchRequest.query(query).withTopK(5)
);
```

### 4. PromptTemplate（提示词模板）

**作用**：管理 Prompt 模板

```java
PromptTemplate promptTemplate = new PromptTemplate(
    "基于以下上下文回答问题：\n{context}\n\n问题：{question}"
);

String prompt = promptTemplate.create(
    Map.of("context", context, "question", question)
);
```

---

## 🎯 总结

### Spring AI 的角色

1. **不是 Agent 框架**
   - 不提供 Agent 功能（工具调用、任务规划等）
   - 这些需要你自己实现

2. **是集成框架**
   - 提供统一的 AI 模型接入方式
   - 简化配置和管理
   - 抽象不同模型的差异

3. **在你的项目中**
   - ✅ 用于接入 Ollama/DeepSeek
   - ✅ 提供配置管理
   - ✅ 提供基础组件（OllamaApi 等）
   - ❌ 不提供 Agent 功能（你自己实现）

### 完整关系

```
你的 Agent 应用
    ↓ 使用
Spring AI（集成框架）
    ↓ 调用
LLM（Ollama/DeepSeek）
```

**Spring AI 是桥梁，连接你的应用和 AI 模型**

### 类比理解

- **Spring AI** = **Spring Data JPA**
  - Spring Data JPA：统一数据库访问接口
  - Spring AI：统一 AI 模型访问接口

- **你的 Agent 实现** = **业务逻辑**
  - 就像你写 Service 层业务逻辑一样
  - Agent 功能需要你自己实现

---

## 💡 实际开发建议

### 使用 Spring AI 的场景

1. **接入 AI 模型**
   ```java
   // 使用 Spring AI 的 ChatClient
   @Autowired
   private ChatClient chatClient;
   ```

2. **向量化文本**
   ```java
   // 使用 Spring AI 的 EmbeddingClient
   @Autowired
   private EmbeddingClient embeddingClient;
   ```

3. **向量存储**
   ```java
   // 使用 Spring AI 的 VectorStore
   @Autowired
   private VectorStore vectorStore;
   ```

### 自己实现的场景

1. **Agent 逻辑**
   ```java
   // 自己实现 AgentService
   public class AgentService {
       // 工具调用逻辑
       // 任务规划逻辑
       // 决策逻辑
   }
   ```

2. **业务逻辑**
   ```java
   // 自己的业务服务
   public class RAGService {
       // 文档处理
       // 检索逻辑
       // Prompt 构建
   }
   ```

---

## 🔄 未来可能的演进

### Spring AI 的发展方向

Spring AI 正在发展，未来可能提供：

1. **Agent 支持**（计划中）
   - 工具调用框架
   - 任务规划支持
   - 但目前（M4 版本）还没有

2. **更多模型支持**
   - 更多开源模型
   - 更多商业模型

3. **更多功能**
   - 流式响应
   - 函数调用
   - 多模态支持

### 你的项目演进

- **现在**：使用 Spring AI 接入模型，自己实现 Agent
- **未来**：如果 Spring AI 提供 Agent 框架，可以考虑迁移
- **优势**：现在自己实现，完全可控，不受框架限制

