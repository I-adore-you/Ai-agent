# API 文档

## 基础信息

- **Base URL**: `http://localhost:8080/api`
- **API 版本**: v1
- **认证方式**: Bearer Token (JWT)

## 通用说明

### 请求头

```
Content-Type: application/json
Authorization: Bearer {token}
```

### 响应格式

所有 API 响应遵循统一格式：

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

### 状态码

- `200 OK`: 请求成功
- `201 Created`: 创建成功
- `400 Bad Request`: 请求参数错误
- `401 Unauthorized`: 未认证
- `403 Forbidden`: 无权限
- `404 Not Found`: 资源不存在
- `500 Internal Server Error`: 服务器错误

## 对话 API

### 1. 发送消息

**POST** `/chat`

发送消息并获取 AI 回复。

**请求体：**
```json
{
  "message": "用户消息内容",
  "conversationId": "可选，对话ID，不提供则创建新对话",
  "useRAG": true,
  "useAgent": false,
  "temperature": 0.7,
  "maxTokens": 2000
}
```

**响应：**
```json
{
  "success": true,
  "data": {
    "message": "AI 回复内容",
    "conversationId": "对话ID",
    "messageId": "消息ID",
    "sources": [
      {
        "documentId": "文档ID",
        "documentTitle": "文档标题",
        "chunkIndex": 0,
        "similarity": 0.95
      }
    ],
    "metadata": {
      "model": "gpt-4",
      "tokens": 150,
      "responseTime": 1.2
    }
  },
  "timestamp": "2024-01-01T00:00:00Z"
}
```

**示例：**
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {token}" \
  -d '{
    "message": "什么是 Spring AI？",
    "useRAG": true
  }'
```

### 2. 获取对话列表

**GET** `/chat/conversations`

获取用户的对话列表。

**查询参数：**
- `page` (int, 可选): 页码，默认 1
- `size` (int, 可选): 每页数量，默认 20
- `type` (string, 可选): 对话类型（rag, agent, mcp）

**响应：**
```json
{
  "success": true,
  "data": {
    "conversations": [
      {
        "id": "对话ID",
        "title": "对话标题",
        "type": "rag",
        "lastMessage": "最后一条消息",
        "messageCount": 10,
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 20
  }
}
```

### 3. 获取对话详情

**GET** `/chat/conversations/{id}`

获取指定对话的详细信息，包括所有消息。

**路径参数：**
- `id` (string): 对话ID

**响应：**
```json
{
  "success": true,
  "data": {
    "id": "对话ID",
    "title": "对话标题",
    "type": "rag",
    "messages": [
      {
        "id": "消息ID",
        "role": "user",
        "content": "用户消息",
        "sources": [],
        "createdAt": "2024-01-01T00:00:00Z"
      },
      {
        "id": "消息ID",
        "role": "assistant",
        "content": "AI 回复",
        "sources": [
          {
            "documentId": "文档ID",
            "documentTitle": "文档标题",
            "chunkIndex": 0
          }
        ],
        "createdAt": "2024-01-01T00:00:00Z"
      }
    ],
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 4. 删除对话

**DELETE** `/chat/conversations/{id}`

删除指定对话。

**路径参数：**
- `id` (string): 对话ID

**响应：**
```json
{
  "success": true,
  "message": "对话已删除",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## 文档 API

### 1. 上传文档

**POST** `/documents/upload`

上传文档并自动处理（解析、分块、向量化）。

**请求：** `multipart/form-data`

**表单字段：**
- `file` (file, 必需): 文档文件
- `title` (string, 可选): 文档标题
- `chunkSize` (int, 可选): 分块大小，默认 1000
- `chunkOverlap` (int, 可选): 分块重叠，默认 200

**支持的文件类型：**
- PDF (.pdf)
- Word (.docx, .doc)
- 文本 (.txt)
- Markdown (.md)

**响应：**
```json
{
  "success": true,
  "data": {
    "documentId": "文档ID",
    "title": "文档标题",
    "fileName": "example.pdf",
    "fileType": "application/pdf",
    "fileSize": 1024000,
    "status": "processing",
    "createdAt": "2024-01-01T00:00:00Z"
  }
}
```

**示例：**
```bash
curl -X POST http://localhost:8080/api/documents/upload \
  -H "Authorization: Bearer {token}" \
  -F "file=@example.pdf" \
  -F "title=示例文档"
```

### 2. 获取文档列表

**GET** `/documents`

获取文档列表。

**查询参数：**
- `page` (int, 可选): 页码，默认 1
- `size` (int, 可选): 每页数量，默认 20
- `status` (string, 可选): 状态过滤（processing, completed, failed）
- `search` (string, 可选): 搜索关键词

**响应：**
```json
{
  "success": true,
  "data": {
    "documents": [
      {
        "id": "文档ID",
        "title": "文档标题",
        "fileName": "example.pdf",
        "fileType": "application/pdf",
        "fileSize": 1024000,
        "status": "completed",
        "chunkCount": 50,
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 100,
    "page": 1,
    "size": 20
  }
}
```

### 3. 获取文档详情

**GET** `/documents/{id}`

获取指定文档的详细信息。

**路径参数：**
- `id` (string): 文档ID

**响应：**
```json
{
  "success": true,
  "data": {
    "id": "文档ID",
    "title": "文档标题",
    "fileName": "example.pdf",
    "fileType": "application/pdf",
    "fileSize": 1024000,
    "content": "文档内容（可选）",
    "status": "completed",
    "chunkCount": 50,
    "chunks": [
      {
        "id": "块ID",
        "chunkIndex": 0,
        "content": "块内容",
        "startIndex": 0,
        "endIndex": 1000
      }
    ],
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 4. 删除文档

**DELETE** `/documents/{id}`

删除指定文档及其所有相关数据（包括向量）。

**路径参数：**
- `id` (string): 文档ID

**响应：**
```json
{
  "success": true,
  "message": "文档已删除",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 5. 获取文档处理状态

**GET** `/documents/{id}/status`

获取文档处理状态。

**路径参数：**
- `id` (string): 文档ID

**响应：**
```json
{
  "success": true,
  "data": {
    "documentId": "文档ID",
    "status": "processing",
    "progress": 75,
    "message": "正在处理中...",
    "chunksProcessed": 30,
    "totalChunks": 40
  }
}
```

## Agent API

### 1. Agent 对话

**POST** `/agent/chat`

使用 Agent 进行对话，支持工具调用。

**请求体：**
```json
{
  "message": "用户消息",
  "conversationId": "可选，对话ID",
  "tools": ["search", "calculator"],
  "temperature": 0.7
}
```

**响应：**
```json
{
  "success": true,
  "data": {
    "message": "AI 回复",
    "conversationId": "对话ID",
    "toolCalls": [
      {
        "toolName": "search",
        "parameters": {
          "query": "搜索关键词"
        },
        "result": "搜索结果"
      }
    ],
    "metadata": {
      "model": "gpt-4",
      "tokens": 200
    }
  }
}
```

### 2. 获取可用工具

**GET** `/agent/tools`

获取所有可用的工具列表。

**响应：**
```json
{
  "success": true,
  "data": {
    "tools": [
      {
        "name": "search",
        "description": "网络搜索工具",
        "parameters": {
          "query": {
            "type": "string",
            "description": "搜索关键词",
            "required": true
          }
        }
      },
      {
        "name": "calculator",
        "description": "数学计算工具",
        "parameters": {
          "expression": {
            "type": "string",
            "description": "数学表达式",
            "required": true
          }
        }
      }
    ]
  }
}
```

### 3. 执行工具

**POST** `/agent/tools/{name}/execute`

手动执行指定工具。

**路径参数：**
- `name` (string): 工具名称

**请求体：**
```json
{
  "parameters": {
    "query": "搜索关键词"
  }
}
```

**响应：**
```json
{
  "success": true,
  "data": {
    "toolName": "search",
    "result": "搜索结果",
    "executionTime": 0.5
  }
}
```

## MCP API

### 1. 创建上下文

**POST** `/mcp/contexts`

创建新的 MCP 上下文。

**请求体：**
```json
{
  "name": "上下文名称",
  "description": "上下文描述",
  "content": "上下文内容",
  "metadata": {
    "tags": ["标签1", "标签2"],
    "category": "分类"
  }
}
```

**响应：**
```json
{
  "success": true,
  "data": {
    "contextId": "上下文ID",
    "name": "上下文名称",
    "description": "上下文描述",
    "content": "上下文内容",
    "metadata": {
      "tags": ["标签1", "标签2"],
      "category": "分类"
    },
    "version": 1,
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 2. 获取上下文列表

**GET** `/mcp/contexts`

获取上下文列表。

**查询参数：**
- `page` (int, 可选): 页码
- `size` (int, 可选): 每页数量
- `search` (string, 可选): 搜索关键词
- `tags` (string[], 可选): 标签过滤

**响应：**
```json
{
  "success": true,
  "data": {
    "contexts": [
      {
        "contextId": "上下文ID",
        "name": "上下文名称",
        "description": "上下文描述",
        "metadata": {
          "tags": ["标签1"],
          "category": "分类"
        },
        "version": 1,
        "createdAt": "2024-01-01T00:00:00Z",
        "updatedAt": "2024-01-01T00:00:00Z"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 20
  }
}
```

### 3. 获取上下文详情

**GET** `/mcp/contexts/{id}`

获取指定上下文的详细信息。

**路径参数：**
- `id` (string): 上下文ID

**查询参数：**
- `version` (int, 可选): 版本号，不提供则返回最新版本

**响应：**
```json
{
  "success": true,
  "data": {
    "contextId": "上下文ID",
    "name": "上下文名称",
    "description": "上下文描述",
    "content": "上下文内容",
    "metadata": {
      "tags": ["标签1", "标签2"],
      "category": "分类"
    },
    "version": 1,
    "createdAt": "2024-01-01T00:00:00Z",
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 4. 更新上下文

**PUT** `/mcp/contexts/{id}`

更新指定上下文。

**路径参数：**
- `id` (string): 上下文ID

**请求体：**
```json
{
  "name": "更新后的名称",
  "description": "更新后的描述",
  "content": "更新后的内容",
  "metadata": {
    "tags": ["新标签"],
    "category": "新分类"
  }
}
```

**响应：**
```json
{
  "success": true,
  "data": {
    "contextId": "上下文ID",
    "name": "更新后的名称",
    "version": 2,
    "updatedAt": "2024-01-01T00:00:00Z"
  }
}
```

### 5. 删除上下文

**DELETE** `/mcp/contexts/{id}`

删除指定上下文。

**路径参数：**
- `id` (string): 上下文ID

**响应：**
```json
{
  "success": true,
  "message": "上下文已删除",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### 6. 搜索上下文

**POST** `/mcp/contexts/search`

使用向量搜索查找相关上下文。

**请求体：**
```json
{
  "query": "搜索查询",
  "topK": 10,
  "threshold": 0.7
}
```

**响应：**
```json
{
  "success": true,
  "data": {
    "contexts": [
      {
        "contextId": "上下文ID",
        "name": "上下文名称",
        "similarity": 0.95,
        "snippet": "相关片段..."
      }
    ]
  }
}
```

## 错误码

| 错误码 | 说明 |
|--------|------|
| `INVALID_REQUEST` | 请求参数无效 |
| `UNAUTHORIZED` | 未认证 |
| `FORBIDDEN` | 无权限 |
| `NOT_FOUND` | 资源不存在 |
| `DOCUMENT_PROCESSING_FAILED` | 文档处理失败 |
| `VECTOR_SEARCH_FAILED` | 向量搜索失败 |
| `AI_SERVICE_ERROR` | AI 服务错误 |
| `INTERNAL_ERROR` | 内部服务器错误 |

## 限流说明

- 默认限流：100 请求/分钟/用户
- 对话 API：10 请求/分钟/用户
- 文档上传：5 请求/分钟/用户

超过限流将返回 `429 Too Many Requests` 状态码。

