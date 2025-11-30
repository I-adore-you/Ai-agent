# 架构设计文档

## 1. 系统架构概述

### 1.1 整体架构

本系统采用分层架构设计，包括：

- **表现层（Presentation Layer）**：前端 Web 界面
- **API 层（API Layer）**：RESTful API 接口
- **业务逻辑层（Business Logic Layer）**：核心业务服务
- **数据访问层（Data Access Layer）**：数据持久化
- **基础设施层（Infrastructure Layer）**：外部服务集成

### 1.2 技术架构图

```
┌─────────────────────────────────────────────────────────────┐
│                        客户端层                                │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │ Web 浏览器  │  │ 移动端 H5   │  │ API 客户端  │            │
│  └────────────┘  └────────────┘  └────────────┘            │
└─────────────────────────────────────────────────────────────┘
                            │
                            │ HTTPS
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                        API 网关层                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │ 认证授权    │  │ 请求路由    │  │ 限流熔断    │            │
│  └────────────┘  └────────────┘  └────────────┘            │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      应用服务层                                │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │ Chat API   │  │ Document   │  │ Agent API  │            │
│  │ Controller │  │ Controller │  │ Controller │            │
│  └────────────┘  └────────────┘  └────────────┘            │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      业务服务层                                │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │ RAG Service│  │ Agent      │  │ MCP Service│            │
│  │            │  │ Service    │  │            │            │
│  └────────────┘  └────────────┘  └────────────┘            │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │ Document   │  │ Embedding  │  │ Vector     │            │
│  │ Service    │  │ Service    │  │ Search     │            │
│  └────────────┘  └────────────┘  └────────────┘            │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据访问层                                │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │ MyBatis    │  │ Vector     │  │ File       │            │
│  │ Mapper     │  │ Repository │  │ Storage    │            │
│  └────────────┘  └────────────┘  └────────────┘            │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      数据存储层                                │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │ PostgreSQL │  │ pgvector   │  │ File       │            │
│  │            │  │            │  │ System     │            │
│  └────────────┘  └────────────┘  └────────────┘            │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                      AI 服务层                                 │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐            │
│  │ OpenAI     │  │ Embedding  │  │ Spring AI  │            │
│  │ API        │  │ API        │  │ Framework  │            │
│  └────────────┘  └────────────┘  └────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

## 2. 核心模块设计

### 2.1 RAG 模块

#### 2.1.1 功能概述

RAG（Retrieval-Augmented Generation）模块负责：
1. 文档处理和向量化
2. 向量存储和检索
3. 上下文增强的生成

#### 2.1.2 数据流

```
文档上传
    │
    ▼
文档解析（PDF、Word、TXT 等）
    │
    ▼
文本提取
    │
    ▼
文档分块（Chunking）
    │
    ▼
向量化（Embedding）
    │
    ▼
存储到 PostgreSQL + pgvector
    │
    ▼
用户查询
    │
    ▼
查询向量化
    │
    ▼
向量相似度搜索
    │
    ▼
检索 Top-K 相关文档块
    │
    ▼
构建 Prompt（用户问题 + 检索到的文档）
    │
    ▼
调用 LLM 生成回答
    │
    ▼
返回结果（包含引用来源）
```

#### 2.1.3 关键组件

**DocumentService**
- `uploadDocument(file)`: 上传文档
- `parseDocument(file)`: 解析文档
- `chunkDocument(text)`: 文档分块
- `deleteDocument(id)`: 删除文档

**EmbeddingService**
- `embed(text)`: 文本向量化
- `embedBatch(texts)`: 批量向量化

**VectorSearchService**
- `search(query, topK)`: 向量搜索
- `similaritySearch(embedding, threshold)`: 相似度搜索

**RAGService**
- `chatWithRAG(message, conversationId)`: RAG 对话
- `retrieveContext(query)`: 检索上下文

### 2.2 Agent 模块

#### 2.2.1 功能概述

Agent 模块提供智能代理功能：
1. 多轮对话管理
2. 工具调用（Tool Calling）
3. 任务规划和执行
4. 记忆管理

#### 2.2.2 Agent 架构

```
用户输入
    │
    ▼
Agent 接收消息
    │
    ▼
分析意图和任务
    │
    ▼
是否需要工具？
    ├─ 是 → 选择工具 → 执行工具 → 整合结果
    └─ 否 → 直接生成回答
    │
    ▼
更新对话历史
    │
    ▼
返回结果
```

#### 2.2.3 工具系统

支持的工具类型：
- **搜索工具**：网络搜索、文档搜索
- **计算工具**：数学计算、代码执行
- **数据工具**：数据库查询、API 调用
- **自定义工具**：业务特定工具

#### 2.2.4 关键组件

**AgentService**
- `chatWithAgent(message, conversationId)`: Agent 对话
- `executeTool(toolName, parameters)`: 执行工具
- `planTask(goal)`: 任务规划

**ToolRegistry**
- `registerTool(tool)`: 注册工具
- `getTool(name)`: 获取工具
- `listTools()`: 列出所有工具

**ConversationManager**
- `createConversation()`: 创建对话
- `getConversation(id)`: 获取对话
- `addMessage(conversationId, message)`: 添加消息
- `getHistory(conversationId)`: 获取历史

### 2.3 MCP 模块

#### 2.3.1 功能概述

MCP（Model Context Protocol）模块提供：
1. 统一的上下文管理
2. 上下文持久化
3. 上下文共享和复用

#### 2.3.2 上下文结构

```json
{
  "contextId": "唯一标识",
  "name": "上下文名称",
  "description": "描述",
  "metadata": {
    "tags": ["标签"],
    "category": "分类"
  },
  "content": "上下文内容",
  "embeddings": "向量表示",
  "createdAt": "创建时间",
  "updatedAt": "更新时间",
  "version": "版本号"
}
```

#### 2.3.3 关键组件

**MCPService**
- `createContext(context)`: 创建上下文
- `updateContext(contextId, updates)`: 更新上下文
- `getContext(contextId)`: 获取上下文
- `searchContexts(query)`: 搜索上下文
- `deleteContext(contextId)`: 删除上下文

**ContextRepository**
- 上下文数据持久化
- 版本管理
- 搜索和过滤

## 3. 数据模型设计

### 3.1 核心实体

#### Document（文档）
```java
public class Document {
    private String id;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String content;
    private String status; // processing, completed, failed
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // getters and setters
}
```

#### DocumentChunk（文档块）
```java
public class DocumentChunk {
    private String id;
    private String documentId;
    private Integer chunkIndex;
    private String content;
    private Integer startIndex;
    private Integer endIndex;
    private float[] embedding; // 向量数据，在 Mapper 中处理 vector 类型
    private LocalDateTime createdAt;
    
    // getters and setters
}
```

#### ChatConversation（对话）
```java
public class ChatConversation {
    private String id;
    private String title;
    private String userId;
    private String type; // rag, agent, mcp
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChatMessage> messages; // 关联查询
    
    // getters and setters
}
```

#### ChatMessage（消息）
```java
public class ChatMessage {
    private String id;
    private String conversationId;
    private String role; // user, assistant, system
    private String content;
    private String[] sources; // 引用的文档，在 Mapper 中处理数组类型
    private Map<String, Object> metadata; // JSONB 类型，在 Mapper 中处理
    private LocalDateTime createdAt;
    
    // getters and setters
}
```

#### VectorEmbedding（向量嵌入）
```java
public class VectorEmbedding {
    private String id;
    private String entityType; // document_chunk, context
    private String entityId;
    private float[] embedding; // 向量数据，在 Mapper 中处理 vector 类型
    private LocalDateTime createdAt;
    
    // getters and setters
}
```

### 3.2 数据库表结构

#### documents 表
```sql
CREATE TABLE documents (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(500),
    file_name VARCHAR(500),
    file_type VARCHAR(50),
    file_size BIGINT,
    content TEXT,
    status VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_documents_status ON documents(status);
CREATE INDEX idx_documents_created_at ON documents(created_at);
```

#### document_chunks 表
```sql
CREATE TABLE document_chunks (
    id VARCHAR(255) PRIMARY KEY,
    document_id VARCHAR(255) REFERENCES documents(id),
    chunk_index INTEGER,
    content TEXT,
    start_index INTEGER,
    end_index INTEGER,
    embedding vector(1536),
    created_at TIMESTAMP
);

CREATE INDEX idx_chunks_document_id ON document_chunks(document_id);
CREATE INDEX idx_chunks_embedding ON document_chunks 
    USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);
```

#### chat_conversations 表
```sql
CREATE TABLE chat_conversations (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(500),
    user_id VARCHAR(255),
    type VARCHAR(50),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_conversations_user_id ON chat_conversations(user_id);
CREATE INDEX idx_conversations_type ON chat_conversations(type);
```

#### chat_messages 表
```sql
CREATE TABLE chat_messages (
    id VARCHAR(255) PRIMARY KEY,
    conversation_id VARCHAR(255) REFERENCES chat_conversations(id),
    role VARCHAR(50),
    content TEXT,
    sources TEXT[],
    metadata JSONB,
    created_at TIMESTAMP
);

CREATE INDEX idx_messages_conversation_id ON chat_messages(conversation_id);
CREATE INDEX idx_messages_created_at ON chat_messages(created_at);
```

## 4. API 设计

### 4.1 RESTful API 规范

- 使用标准 HTTP 方法：GET、POST、PUT、DELETE
- 使用 RESTful 资源命名
- 统一响应格式
- 错误处理规范

### 4.2 响应格式

**成功响应：**
```json
{
  "success": true,
  "data": { ... },
  "message": "操作成功",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

**错误响应：**
```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "message": "错误描述",
    "details": { ... }
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 4.3 主要 API 端点

#### 对话 API
- `POST /api/chat` - 发送消息
- `GET /api/chat/conversations` - 获取对话列表
- `GET /api/chat/conversations/{id}` - 获取对话详情
- `DELETE /api/chat/conversations/{id}` - 删除对话

#### 文档 API
- `POST /api/documents/upload` - 上传文档
- `GET /api/documents` - 获取文档列表
- `GET /api/documents/{id}` - 获取文档详情
- `DELETE /api/documents/{id}` - 删除文档

#### Agent API
- `POST /api/agent/chat` - Agent 对话
- `GET /api/agent/tools` - 获取可用工具
- `POST /api/agent/tools/{name}/execute` - 执行工具

#### MCP API
- `POST /api/mcp/contexts` - 创建上下文
- `GET /api/mcp/contexts` - 获取上下文列表
- `GET /api/mcp/contexts/{id}` - 获取上下文详情
- `PUT /api/mcp/contexts/{id}` - 更新上下文
- `DELETE /api/mcp/contexts/{id}` - 删除上下文

## 5. 安全设计

### 5.1 认证授权

- JWT Token 认证
- 基于角色的访问控制（RBAC）
- API 密钥管理

### 5.2 数据安全

- 敏感数据加密
- SQL 注入防护
- XSS 防护
- CSRF 防护

### 5.3 接口安全

- Rate Limiting（限流）
- 请求签名验证
- HTTPS 强制使用

## 6. 性能优化

### 6.1 向量搜索优化

- 使用 HNSW 索引
- 批量向量化
- 缓存常用查询

### 6.2 数据库优化

- 索引优化
- 连接池配置
- 查询优化

### 6.3 缓存策略

- Redis 缓存对话历史
- 缓存文档元数据
- 缓存向量搜索结果

## 7. 扩展性设计

### 7.1 水平扩展

- 无状态服务设计
- 负载均衡
- 数据库读写分离

### 7.2 插件化架构

- 工具插件系统
- 文档解析器插件
- AI 模型适配器

## 8. 监控和日志

### 8.1 日志

- 结构化日志
- 日志级别管理
- 日志聚合和分析

### 8.2 监控

- 应用性能监控（APM）
- 错误追踪
- 指标收集（Metrics）

### 8.3 告警

- 错误率告警
- 性能告警
- 资源使用告警

