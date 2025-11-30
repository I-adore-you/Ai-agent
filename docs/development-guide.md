# 开发指南

## 开发环境设置

### 1. 前置要求

- **JDK 21+**
- **Maven 3.8+** 或 **Gradle 7+**
- **PostgreSQL 15+**（本地开发）
- **Node.js 18+** 和 **npm/yarn**（前端开发）
- **IDE**：IntelliJ IDEA、Eclipse 或 VS Code

### 2. 本地数据库设置

#### 安装 PostgreSQL 和 pgvector

**macOS:**
```bash
brew install postgresql@15
brew install pgvector
```

**Ubuntu/Debian:**
```bash
sudo apt install postgresql-15 postgresql-contrib-15
# 安装 pgvector 扩展
```

**Windows:**
下载 PostgreSQL 安装包，然后安装 pgvector 扩展。

#### 创建开发数据库

```sql
CREATE DATABASE ai_agent_dev;
CREATE EXTENSION IF NOT EXISTS vector;
```

### 3. 项目初始化

#### 克隆项目

```bash
git clone <repository-url>
cd Ai-agent
```

#### 后端设置

```bash
cd backend
mvn clean install
# 或
./gradlew build
```

#### 前端设置

```bash
cd frontend
npm install
# 或
yarn install
```

### 4. 配置文件

#### 后端配置

创建 `backend/src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ai_agent_dev
    username: postgres
    password: postgres
  
  # MyBatis 配置
  mybatis:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.aiagent.model
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

spring.ai:
  openai:
    api-key: ${OPENAI_API_KEY:your-dev-key}
    chat:
      options:
        model: gpt-3.5-turbo
        temperature: 0.7

logging:
  level:
    root: INFO
    com.aiagent: DEBUG
```

#### 前端配置

创建 `frontend/.env.development`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_WS_URL=ws://localhost:8080/ws
```

### 5. 运行应用

#### 启动后端

```bash
cd backend
mvn spring-boot:run
# 或
./gradlew bootRun
```

后端将在 `http://localhost:8080` 运行。

#### 启动前端

```bash
cd frontend
npm run dev
# 或
yarn dev
```

前端将在 `http://localhost:3000` 运行。

## 项目结构说明

### 后端结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/aiagent/
│   │   │       ├── AiAgentApplication.java      # 主应用类
│   │   │       ├── config/                      # 配置类
│   │   │       │   ├── AiConfig.java
│   │   │       │   ├── VectorStoreConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   └── WebConfig.java
│   │   │       ├── controller/                  # 控制器
│   │   │       │   ├── ChatController.java
│   │   │       │   ├── DocumentController.java
│   │   │       │   ├── AgentController.java
│   │   │       │   └── MCPController.java
│   │   │       ├── service/                     # 服务层
│   │   │       │   ├── RAGService.java
│   │   │       │   ├── AgentService.java
│   │   │       │   ├── MCPService.java
│   │   │       │   ├── DocumentService.java
│   │   │       │   ├── EmbeddingService.java
│   │   │       │   └── VectorSearchService.java
│   │   │       ├── mapper/                      # MyBatis Mapper 接口
│   │   │       │   ├── DocumentMapper.java
│   │   │       │   ├── ChatHistoryMapper.java
│   │   │       │   └── VectorMapper.java
│   │   │       └── resources/
│   │   │           └── mapper/                  # MyBatis XML 映射文件
│   │   │               ├── DocumentMapper.xml
│   │   │               ├── ChatHistoryMapper.xml
│   │   │               └── VectorMapper.xml
│   │   │       ├── model/                       # 实体类
│   │   │       │   ├── Document.java
│   │   │       │   ├── DocumentChunk.java
│   │   │       │   ├── ChatConversation.java
│   │   │       │   └── ChatMessage.java
│   │   │       ├── dto/                         # 数据传输对象
│   │   │       │   ├── request/
│   │   │       │   └── response/
│   │   │       ├── exception/                   # 异常处理
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── CustomException.java
│   │   │       └── util/                        # 工具类
│   │   │           ├── DocumentParser.java
│   │   │           ├── TextChunker.java
│   │   │           └── EmbeddingUtil.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       └── mapper/                          # MyBatis XML 映射文件
│   │           ├── DocumentMapper.xml
│   │           ├── ChatHistoryMapper.xml
│   │           └── VectorMapper.xml
│   └── test/                                    # 测试
│       ├── java/
│       └── resources/
└── pom.xml 或 build.gradle
```

### 前端结构（React 示例）

```
frontend/
├── src/
│   ├── components/              # 组件
│   │   ├── Chat/
│   │   │   ├── ChatWindow.tsx
│   │   │   ├── MessageList.tsx
│   │   │   └── MessageInput.tsx
│   │   ├── Document/
│   │   │   ├── DocumentList.tsx
│   │   │   ├── DocumentUpload.tsx
│   │   │   └── DocumentViewer.tsx
│   │   └── common/
│   │       ├── Button.tsx
│   │       ├── Input.tsx
│   │       └── Modal.tsx
│   ├── pages/                  # 页面
│   │   ├── HomePage.tsx
│   │   ├── ChatPage.tsx
│   │   └── DocumentPage.tsx
│   ├── services/               # API 服务
│   │   ├── api.ts
│   │   ├── chatService.ts
│   │   └── documentService.ts
│   ├── hooks/                  # 自定义 Hooks
│   │   ├── useChat.ts
│   │   ├── useDocuments.ts
│   │   └── useWebSocket.ts
│   ├── store/                  # 状态管理
│   │   ├── chatStore.ts
│   │   └── documentStore.ts
│   ├── utils/                  # 工具函数
│   │   ├── constants.ts
│   │   └── helpers.ts
│   ├── types/                  # TypeScript 类型
│   │   ├── chat.ts
│   │   └── document.ts
│   ├── styles/                 # 样式
│   │   └── globals.css
│   └── App.tsx
├── public/
├── package.json
└── vite.config.ts
```

## MyBatis 配置和使用

### MyBatis 配置

#### 1. 添加依赖（Maven）

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>3.0.3</version>
</dependency>
```

#### 2. 配置文件

在 `application.yml` 中配置 MyBatis：

```yaml
spring:
  mybatis:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.aiagent.model
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

#### 3. Mapper 接口示例

```java
@Mapper
public interface DocumentMapper {
    void insert(Document document);
    void update(Document document);
    Document selectById(String id);
    List<Document> selectAll();
    void deleteById(String id);
}
```

#### 4. Mapper XML 示例

创建 `src/main/resources/mapper/DocumentMapper.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.aiagent.mapper.DocumentMapper">
    
    <resultMap id="DocumentResultMap" type="Document">
        <id property="id" column="id"/>
        <result property="title" column="title"/>
        <result property="fileName" column="file_name"/>
        <result property="fileType" column="file_type"/>
        <result property="fileSize" column="file_size"/>
        <result property="content" column="content"/>
        <result property="status" column="status"/>
        <result property="createdAt" column="created_at"/>
        <result property="updatedAt" column="updated_at"/>
    </resultMap>
    
    <insert id="insert" parameterType="Document">
        INSERT INTO documents (id, title, file_name, file_type, file_size, 
                             content, status, created_at, updated_at)
        VALUES (#{id}, #{title}, #{fileName}, #{fileType}, #{fileSize},
                #{content}, #{status}, #{createdAt}, #{updatedAt})
    </insert>
    
    <update id="update" parameterType="Document">
        UPDATE documents
        SET title = #{title},
            content = #{content},
            status = #{status},
            updated_at = #{updatedAt}
        WHERE id = #{id}
    </update>
    
    <select id="selectById" resultMap="DocumentResultMap">
        SELECT * FROM documents WHERE id = #{id}
    </select>
    
    <select id="selectAll" resultMap="DocumentResultMap">
        SELECT * FROM documents ORDER BY created_at DESC
    </select>
    
    <delete id="deleteById">
        DELETE FROM documents WHERE id = #{id}
    </delete>
</mapper>
```

#### 5. 处理 PostgreSQL 特殊类型

**处理 vector 类型：**

```xml
<!-- 在 Mapper XML 中处理向量类型 -->
<insert id="insertChunk" parameterType="DocumentChunk">
    INSERT INTO document_chunks (id, document_id, chunk_index, content, 
                                 embedding, created_at)
    VALUES (#{id}, #{documentId}, #{chunkIndex}, #{content},
            #{embedding,typeHandler=com.aiagent.typehandler.VectorTypeHandler},
            #{createdAt})
</insert>
```

创建自定义 TypeHandler：

```java
@MappedTypes(float[].class)
public class VectorTypeHandler extends BaseTypeHandler<float[]> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, 
                                    float[] parameter, JdbcType jdbcType) 
            throws SQLException {
        // 将 float[] 转换为 PostgreSQL vector 格式
        String vectorStr = "[" + Arrays.stream(parameter)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(",")) + "]";
        ps.setString(i, vectorStr);
    }
    
    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) 
            throws SQLException {
        String vectorStr = rs.getString(columnName);
        return parseVector(vectorStr);
    }
    
    // ... 其他方法实现
}
```

**处理 JSONB 类型：**

```xml
<result property="metadata" column="metadata" 
        typeHandler="org.apache.ibatis.type.MapTypeHandler"/>
```

## 核心功能实现

### 1. RAG 服务实现

#### DocumentService 示例

```java
@Service
public class DocumentService {
    
    @Autowired
    private DocumentMapper documentMapper;
    
    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    private TextChunker textChunker;
    
    public Document uploadDocument(MultipartFile file, String title) {
        // 1. 保存文件
        String content = parseDocument(file);
        
        // 2. 创建文档记录
        Document document = new Document();
        document.setId(UUID.randomUUID().toString());
        document.setTitle(title);
        document.setContent(content);
        document.setStatus("processing");
        document.setCreatedAt(LocalDateTime.now());
        documentMapper.insert(document);
        
        // 3. 异步处理文档
        processDocumentAsync(document);
        
        return document;
    }
    
    @Async
    private void processDocumentAsync(Document document) {
        try {
            // 1. 文档分块
            List<String> chunks = textChunker.chunk(document.getContent());
            
            // 2. 批量向量化
            List<float[]> embeddings = embeddingService.embedBatch(chunks);
            
            // 3. 保存文档块和向量
            for (int i = 0; i < chunks.size(); i++) {
                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocumentId(document.getId());
                chunk.setContent(chunks.get(i));
                chunk.setEmbedding(embeddings.get(i));
                chunk.setChunkIndex(i);
                // 保存到数据库
            }
            
            // 4. 更新文档状态
            document.setStatus("completed");
            document.setUpdatedAt(LocalDateTime.now());
            documentMapper.update(document);
        } catch (Exception e) {
            document.setStatus("failed");
            document.setUpdatedAt(LocalDateTime.now());
            documentMapper.update(document);
        }
    }
}
```

#### RAGService 示例

```java
@Service
public class RAGService {
    
    @Autowired
    private VectorSearchService vectorSearchService;
    
    @Autowired
    private ChatClient chatClient;
    
    public ChatResponse chatWithRAG(String message, String conversationId) {
        // 1. 向量化用户查询
        float[] queryEmbedding = embeddingService.embed(message);
        
        // 2. 检索相关文档块
        List<DocumentChunk> relevantChunks = vectorSearchService
            .search(queryEmbedding, 5);
        
        // 3. 构建上下文
        String context = buildContext(relevantChunks);
        
        // 4. 构建 Prompt
        String prompt = buildPrompt(message, context);
        
        // 5. 调用 LLM
        String response = chatClient.call(prompt);
        
        // 6. 构建响应
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setMessage(response);
        chatResponse.setSources(extractSources(relevantChunks));
        
        return chatResponse;
    }
    
    private String buildContext(List<DocumentChunk> chunks) {
        return chunks.stream()
            .map(DocumentChunk::getContent)
            .collect(Collectors.joining("\n\n"));
    }
    
    private String buildPrompt(String message, String context) {
        return String.format("""
            基于以下上下文回答问题。如果上下文中没有相关信息，请说明。
            
            上下文：
            %s
            
            问题：%s
            
            回答：
            """, context, message);
    }
}
```

### 2. Agent 服务实现

```java
@Service
public class AgentService {
    
    @Autowired
    private ToolRegistry toolRegistry;
    
    @Autowired
    private ChatClient chatClient;
    
    public ChatResponse chatWithAgent(String message, String conversationId) {
        // 1. 分析是否需要工具
        ToolDecision decision = analyzeToolNeeds(message);
        
        if (decision.needsTool()) {
            // 2. 执行工具
            ToolResult toolResult = executeTool(decision.getToolName(), 
                                                decision.getParameters());
            
            // 3. 整合工具结果生成回答
            String response = generateResponseWithTool(message, toolResult);
            
            return new ChatResponse(response, toolResult);
        } else {
            // 4. 直接生成回答
            String response = chatClient.call(message);
            return new ChatResponse(response);
        }
    }
    
    private ToolDecision analyzeToolNeeds(String message) {
        // 使用 LLM 分析是否需要工具
        // 返回工具名称和参数
    }
    
    private ToolResult executeTool(String toolName, Map<String, Object> params) {
        Tool tool = toolRegistry.getTool(toolName);
        return tool.execute(params);
    }
}
```

### 3. 前端实现示例

#### Chat 组件（React）

```typescript
import { useState, useEffect } from 'react';
import { chatService } from '../services/chatService';

export function ChatWindow() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [conversationId, setConversationId] = useState<string | null>(null);

  const sendMessage = async () => {
    if (!input.trim() || loading) return;

    const userMessage: Message = {
      role: 'user',
      content: input,
      timestamp: new Date(),
    };

    setMessages(prev => [...prev, userMessage]);
    setInput('');
    setLoading(true);

    try {
      const response = await chatService.sendMessage({
        message: input,
        conversationId,
        useRAG: true,
      });

      const assistantMessage: Message = {
        role: 'assistant',
        content: response.message,
        sources: response.sources,
        timestamp: new Date(),
      };

      setMessages(prev => [...prev, assistantMessage]);
      setConversationId(response.conversationId);
    } catch (error) {
      console.error('发送消息失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-col h-full">
      <div className="flex-1 overflow-y-auto p-4">
        {messages.map((msg, idx) => (
          <MessageBubble key={idx} message={msg} />
        ))}
      </div>
      <div className="border-t p-4">
        <div className="flex gap-2">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
            className="flex-1 px-4 py-2 border rounded-lg"
            placeholder="输入消息..."
          />
          <button
            onClick={sendMessage}
            disabled={loading}
            className="px-6 py-2 bg-blue-500 text-white rounded-lg disabled:opacity-50"
          >
            发送
          </button>
        </div>
      </div>
    </div>
  );
}
```

## 测试

### 后端测试

#### 单元测试示例

```java
@SpringBootTest
class RAGServiceTest {
    
    @Autowired
    private RAGService ragService;
    
    @MockBean
    private VectorSearchService vectorSearchService;
    
    @Test
    void testChatWithRAG() {
        // Given
        String message = "什么是 Spring AI？";
        List<DocumentChunk> chunks = createMockChunks();
        when(vectorSearchService.search(any(), eq(5)))
            .thenReturn(chunks);
        
        // When
        ChatResponse response = ragService.chatWithRAG(message, null);
        
        // Then
        assertNotNull(response);
        assertNotNull(response.getMessage());
        assertFalse(response.getSources().isEmpty());
    }
}
```

#### 集成测试

```java
@SpringBootTest
@AutoConfigureMockMvc
class ChatControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testSendMessage() throws Exception {
        ChatRequest request = new ChatRequest();
        request.setMessage("测试消息");
        request.setUseRAG(true);
        
        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.message").exists());
    }
}
```

### 前端测试

#### 组件测试（React Testing Library）

```typescript
import { render, screen, fireEvent } from '@testing-library/react';
import { ChatWindow } from './ChatWindow';

describe('ChatWindow', () => {
  it('应该发送消息', async () => {
    render(<ChatWindow />);
    
    const input = screen.getByPlaceholderText('输入消息...');
    const button = screen.getByText('发送');
    
    fireEvent.change(input, { target: { value: '测试消息' } });
    fireEvent.click(button);
    
    expect(await screen.findByText('测试消息')).toBeInTheDocument();
  });
});
```

## 代码规范

### Java 代码规范

- 遵循 Google Java Style Guide
- 使用 Lombok 减少样板代码
- 使用 Spring 注解进行依赖注入
- 异常处理使用自定义异常类

### TypeScript 代码规范

- 使用 ESLint 和 Prettier
- 遵循 Airbnb TypeScript Style Guide
- 使用函数式组件和 Hooks
- 类型定义要完整

## 调试技巧

### 后端调试

1. **日志调试**：使用 SLF4J + Logback
2. **断点调试**：IDE 断点
3. **API 测试**：使用 Postman 或 curl
4. **数据库查询**：直接查询 PostgreSQL

### 前端调试

1. **浏览器 DevTools**：Console、Network、React DevTools
2. **日志**：使用 console.log 或日志库
3. **状态检查**：React DevTools 查看状态

## 性能优化

### 后端优化

1. **批量操作**：批量向量化、批量插入
2. **异步处理**：文档处理使用异步
3. **缓存**：缓存常用查询结果
4. **连接池**：优化数据库连接池

### 前端优化

1. **代码分割**：路由级别的代码分割
2. **懒加载**：组件和图片懒加载
3. **虚拟滚动**：长列表使用虚拟滚动
4. **防抖节流**：输入和滚动事件

## 常见问题

### Q: 向量搜索很慢？

A: 
1. 检查索引是否创建
2. 优化 HNSW 参数
3. 减少 topK 数量
4. 使用批量查询

### Q: 文档处理失败？

A:
1. 检查文件格式是否支持
2. 检查文件大小限制
3. 查看错误日志
4. 验证 Embedding API 是否正常

### Q: 前端无法连接后端？

A:
1. 检查 CORS 配置
2. 检查 API 地址配置
3. 检查网络连接
4. 查看浏览器控制台错误

## 下一步

- 阅读 [架构文档](architecture.md) 了解系统设计
- 阅读 [API 文档](api.md) 了解接口规范
- 阅读 [部署文档](deployment.md) 了解部署流程

