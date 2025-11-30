# Spring AI MCP 开发指南

## 1. MCP 基本概念

MCP（Model Context Protocol，模型上下文协议）是一种用于在AI模型和外部工具之间传递上下文信息的协议，允许AI模型调用外部工具来完成复杂任务。

## 2. Spring AI 中的 MCP 实现

### 2.1 本地 MCP 工具

#### 2.1.1 实现方式
使用 `@Tool` 注解标记方法，Spring AI 会自动将其注册为可调用的 MCP 工具：

```java
@Service
public class McpToolService {

    @Tool(name = "systemInfo", description = "获取系统资源信息")
    public Map<String, Object> getSystemInfo() {
        // 实现获取系统信息的逻辑
    }

    @Tool(name = "createFile", description = "创建文件，参数包括filePath(文件路径)和content(文件内容)")
    public Map<String, Object> createFile(String filePath, String content) {
        // 实现创建文件的逻辑
    }
}
```

#### 2.1.2 工作原理
1. **自动注册**：Spring AI 在应用启动时扫描所有带有 `@Tool` 注解的方法，并将它们注册到本地工具注册表中
2. **工具信息收集**：收集工具名称、描述、参数信息等元数据
3. **工具调用**：ChatClient 在调用大模型时，会将所有工具信息发送给大模型，大模型根据需要生成工具调用请求

### 2.2 远程 MCP Server

#### 2.2.1 概念
MCP Server 是指远程提供工具服务的服务器，通过网络协议（如HTTP）提供工具调用服务，支持跨语言、跨平台调用。

#### 2.2.2 工作原理
1. **工具发现**：远程 MCP Server 暴露工具发现端点（通常是 `/mcp/tools`），返回所有可用工具的元数据
2. **自动注册**：Spring AI 的 `McpClient` 在应用启动时调用远程工具发现端点，获取所有远程工具的元数据，并注册到本地工具注册表中
3. **工具调用**：
   - ChatClient 调用大模型时，将所有工具（本地和远程）的信息发送给大模型
   - 大模型生成工具调用请求
   - ChatClient 根据工具名称判断是本地工具还是远程工具，分别调用

## 3. 大模型如何知道需要调用哪些工具

### 3.1 工具信息发送
在调用大模型时，Spring AI 会：
1. 创建一个系统提示（System Prompt）
2. 将**所有可用工具的信息**作为系统提示的一部分发送给大模型
3. 同时发送用户请求

### 3.2 大模型决策过程
1. 大模型收到系统提示（包含工具信息）和用户请求
2. 分析用户请求
3. 查看可用的工具列表
4. 决定是否需要调用工具
5. 如果需要，选择最合适的工具并生成工具调用请求

### 3.3 工具调用请求格式
大模型生成的工具调用请求通常包含：
```json
{
  "tool_calls": [
    {
      "name": "toolName",
      "arguments": {
        "param1": "value1",
        "param2": "value2"
      }
    }
  ]
}
```

## 4. 本地 MCP 工具 vs 远程 MCP Server

| 特性 | 本地 MCP 工具 | 远程 MCP Server |
|------|---------------|-----------------|
| 部署方式 | 与应用同进程 | 独立部署 |
| 调用方式 | 直接方法调用 | 网络请求 |
| 网络开销 | 无 | 有 |
| 跨语言支持 | 不支持 | 支持 |
| 扩展性 | 受应用进程限制 | 可独立扩展 |
| 资源隔离 | 无 | 有 |
| 适合场景 | 简单工具、高性能要求 | 复杂工具、资源密集型工具、需要共享的工具 |

## 5. 配置与使用

### 5.1 本地 MCP 工具配置

#### 5.1.1 依赖
```xml
<!-- Spring AI Ollama 依赖 -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-ollama-spring-boot-starter</artifactId>
    <version>${spring-ai.version}</version>
</dependency>
```

#### 5.1.2 实现工具服务
如 2.1.1 节所示，使用 `@Tool` 注解标记方法即可。

### 5.2 远程 MCP Server 配置

#### 5.2.1 版本要求
Spring AI 1.1.0+ 版本原生支持远程 MCP Server 调用。

#### 5.2.2 依赖
```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-mcp-client</artifactId>
    <version>${spring-ai.version}</version>
</dependency>
```

#### 5.2.3 配置
```yaml
spring:
  ai:
    mcp:
      client:
        base-url: http://remote-mcp-server:8080  # 远程 MCP Server 地址
        discovery:
          enabled: true  # 启用工具自动发现（默认 true）
```

#### 5.2.4 使用
无需修改现有代码，ChatClient 会自动处理远程工具：

```java
@Autowired
private ChatClient chatClient;

public String chat(String message) {
    return chatClient.prompt(message).call().content();
}
```

## 6. 工具调用流程

### 6.1 本地工具调用流程
1. 用户发送请求
2. ChatClient 调用大模型，将所有工具信息发送给大模型
3. 大模型生成工具调用请求
4. ChatClient 解析请求，调用本地工具方法
5. 将工具执行结果返回给大模型
6. 大模型生成最终响应
7. 返回给用户

### 6.2 远程工具调用流程
1. 用户发送请求
2. ChatClient 调用大模型，将所有工具信息发送给大模型
3. 大模型生成工具调用请求
4. ChatClient 解析请求，通过 McpClient 调用远程 MCP Server
5. 远程 MCP Server 执行工具方法
6. 将工具执行结果返回给 ChatClient
7. ChatClient 将结果返回给大模型
8. 大模型生成最终响应
9. 返回给用户

## 7. 测试 MCP 工具

### 7.1 直接测试工具方法
可以通过创建测试控制器直接测试工具方法：

```java
@RestController
@RequestMapping("/api/mcp/test")
public class McpTestController {

    @Autowired
    private McpToolService mcpToolService;

    @PostMapping("/system-info")
    public ApiResponse<Map<String, Object>> testSystemInfo() {
        Map<String, Object> systemInfo = mcpToolService.getSystemInfo();
        return ApiResponse.success(systemInfo);
    }
}
```

### 7.2 通过 ChatClient 测试
```java
@PostMapping("/chat")
public ApiResponse<String> testMcpChat(@RequestBody Map<String, String> request) {
    String message = request.get("message");
    PromptTemplate promptTemplate = new PromptTemplate(message);
    Prompt prompt = promptTemplate.create();
    String aiResponse = chatClient.prompt(prompt).call().content();
    return ApiResponse.success(aiResponse);
}
```

## 8. 最佳实践

### 8.1 工具设计原则
1. **单一职责**：每个工具只负责一个功能
2. **清晰命名**：工具名称应清晰反映其功能
3. **详细描述**：工具描述应详细说明其用途和参数
4. **类型安全**：使用明确的参数类型和返回值类型
5. **错误处理**：妥善处理异常，返回清晰的错误信息

### 8.2 性能优化
1. **本地工具优先**：对于简单工具或对性能要求高的工具，优先使用本地工具
2. **远程工具缓存**：对于不经常变化的远程工具结果，可以考虑缓存
3. **异步调用**：对于耗时较长的工具，可以考虑异步调用

### 8.3 安全性考虑
1. **权限控制**：对敏感工具添加权限控制
2. **输入验证**：对工具参数进行严格验证，防止注入攻击
3. **输出过滤**：对工具返回结果进行过滤，防止泄露敏感信息
4. **日志记录**：记录所有工具调用，便于审计和调试

## 9. 常见问题

### 9.1 工具没有被调用
- 检查工具名称是否正确
- 检查工具描述是否清晰，大模型是否能理解
- 检查用户请求是否明确，大模型是否能判断需要调用工具

### 9.2 远程工具无法调用
- 检查远程 MCP Server 地址是否正确
- 检查远程 MCP Server 是否正常运行
- 检查网络连接是否正常
- 检查 CORS 配置是否允许跨域请求

### 9.3 工具参数不匹配
- 检查工具方法的参数名称和类型是否与大模型生成的参数一致
- 检查工具描述中的参数信息是否准确

## 10. Agent 与 MCP 的关系

### 10.1 核心概念
Agent（智能代理）是指能够接收用户请求，根据请求内容和可用工具，自主决策并执行任务的系统。在 Spring AI 生态中，Agent 与 MCP 紧密结合，共同实现智能决策和工具调用。

### 10.2 Agent 的工作原理
Agent 的典型工作流程如下：

1. **接收用户请求**：接收用户的自然语言请求
2. **预处理**：
   - 收集本地 MCP 工具的元数据（通过 `@Tool` 注解自动扫描）
   - 发现并收集远程 MCP Server 的工具元数据（通过工具发现端点）
   - 将所有工具元数据整合到本地工具注册表
3. **与大模型交互**：
   - 将用户请求和所有工具元数据发送给大模型
   - 大模型分析请求，决定是否需要调用工具
   - 大模型生成工具调用请求
4. **工具执行**：
   - Agent 解析工具调用请求
   - 根据工具名称判断是本地工具还是远程工具
   - 执行工具调用，获取结果
5. **结果处理**：
   - 将工具执行结果返回给大模型
   - 大模型生成最终响应
6. **返回给用户**：将最终响应返回给用户

### 10.3 Agent 与 MCP 的协作优势

| 优势 | 描述 |
|------|------|
| **智能决策** | 大模型能够根据用户请求和工具信息，智能选择最合适的工具 |
| **自动执行** | Agent 自动处理工具调用流程，无需人工干预 |
| **扩展能力** | 通过 MCP 可以轻松扩展 Agent 的能力，添加新的工具 |
| **跨平台支持** | 远程 MCP Server 支持跨语言、跨平台调用 |
| **资源隔离** | 远程工具运行在独立服务器上，不会影响 Agent 性能 |
| **复用性** | 工具可以在多个 Agent 之间共享，提高开发效率 |

### 10.4 实际应用场景

1. **智能助手**：能够查询天气、预订机票、发送邮件等
2. **代码助手**：能够生成代码、调试代码、运行测试等
3. **数据分析助手**：能够查询数据库、生成图表、分析数据等
4. **系统管理助手**：能够监控系统、执行命令、管理资源等

## 11. 大模型上下文与Agent上下文管理

### 11.1 核心概念区分

在AI系统中，"上下文"有两个不同但相关的概念：

#### 11.1.1 大模型的临时上下文窗口
大模型的**临时上下文窗口**是模型**本身固有的限制**，指的是模型在**单次调用**时能够处理的最大token数量。

**特点**：
- **临时的**：只在**单次API调用**中有效，调用结束后立即消失
- **有限的**：不同模型有不同的窗口大小（如GPT-3.5是4k/16k，GPT-4是8k/32k/128k）
- **被动的**：模型只是被动接收和处理这些上下文，不会主动存储
- **包含内容**：通常包含**当前请求** + **相关的历史对话**

#### 11.1.2 Agent的上下文管理能力
Agent的**上下文管理**是Agent层提供的**主动能力**，用于管理和优化发送给大模型的上下文。

**核心功能**：
- **历史对话管理**：存储、检索和管理跨会话的对话历史
- **上下文裁剪**：当历史对话接近模型窗口限制时，智能裁剪不重要的内容
- **相关信息提取**：从大量历史中提取与当前请求相关的信息
- **记忆持久化**：将重要信息持久化存储，跨会话可用

### 11.2 上下文窗口的实际应用

#### 11.2.1 上下文使用率
在实际应用中，我们经常会看到类似"上下文使用率 67% of 184K"的显示：
- 184K是该大模型的**最大上下文窗口**（能一次处理184,000个tokens）
- 67%是**当前调用**已经使用的上下文比例
- "压缩"功能用于**智能裁剪上下文**，确保不超过窗口限制

#### 11.2.2 对话示例
假设您与GPT-4（32k窗口）进行多轮对话：
- 第1轮：您发送"你好"（1 token），模型回复"你好！"（2 tokens）→ 总使用：3 tokens
- 第2轮：您发送"帮我写一首诗"（5 tokens）→ 模型需要看到："你好" + "你好！" + "帮我写一首诗" → 总使用：3 + 5 = 8 tokens
- 随着对话继续，历史对话累积，直到接近32k的限制

### 11.3 两者的协作关系

| 阶段 | 大模型上下文 | Agent上下文管理 |
|------|--------------|-----------------|
| 1. 用户请求 | 无 | 接收用户请求 |
| 2. 历史检索 | 无 | 从持久存储中检索相关历史 |
| 3. 上下文构建 | 无 | 拼接当前请求 + 相关历史，形成完整上下文 |
| 4. 上下文裁剪 | 无 | 确保上下文不超过模型窗口限制 |
| 5. 模型调用 | 接收Agent构建的上下文，处理并生成响应 | 发送构建好的上下文给模型 |
| 6. 结果处理 | 返回响应 | 接收模型响应，更新持久化存储 |

### 11.4 实际应用中的例子

#### 11.4.1 ChatGPT的工作流程
- 当你在ChatGPT中进行多轮对话：
  - **Agent层**（OpenAI的服务）管理你的对话历史
  - 每次请求时，Agent会：
    1. 从数据库中获取你的**相关历史对话**
    2. 将历史对话与**当前请求**拼接
    3. 智能裁剪，确保不超过GPT模型的上下文窗口
    4. 将处理后的上下文发送给GPT模型
    5. 模型生成响应，Agent更新你的对话历史

#### 11.4.2 本地AI应用的工作流程
- 在本地部署的AI应用中：
  - 应用的**Agent层**负责管理对话历史
  - 可能使用本地数据库存储对话记录
  - 发送请求时，Agent会将相关的历史对话拼接成prompt

### 11.5 记忆的分类

| 特性 | 大模型的临时上下文窗口 | Agent的上下文管理能力 |
|------|------------------------|------------------------|
| **性质** | 模型固有限制 | Agent主动能力 |
| **有效期** | 单次调用 | 跨会话持久化 |
| **管理方式** | 被动接收 | 主动管理和优化 |
| **包含内容** | 当前请求 + 相关历史 | 完整的对话历史和用户信息 |
| **限制** | 固定的token数量 | 理论上无限制（受存储限制） |
| **作用** | 决定模型一次能处理多少信息 | 决定哪些信息应该被包含在模型调用中 |

### 11.6 总结

简单来说：
- **大模型的临时上下文**是一个"容器"，决定了一次能装多少东西
- **Agent的上下文管理**是一个"管理员"，决定了哪些东西应该被放进这个容器，以及如何高效利用容器空间

## 12. 工具元数据过大问题的解决方案

### 12.1 问题分析

当 Agent 有大量工具时，工具元数据（名称、描述、参数信息等）可能会变得非常大，导致以下问题：

1. **占用上下文窗口**：大模型的上下文窗口是有限的，过多的工具元数据会占用宝贵的上下文空间
2. **影响 AI 理解**：可能导致用户的实际请求和对话历史被截断，影响 AI 的理解和响应
3. **增加 API 调用成本**：更多的 tokens 意味着更高的 API 调用成本
4. **降低响应速度**：更大的数据量会增加网络传输时间和模型处理时间

### 11.2 解决方案

#### 11.2.1 工具选择/过滤
**原理**：只向大模型发送与用户请求相关的工具元数据

**实现**：
- 分析用户请求的关键词
- 匹配工具描述中的关键词
- 只发送匹配度高的工具

**Spring AI 实现示例**：
```java
@Service
public class SmartToolSelector {

    @Autowired
    private ToolRegistry toolRegistry;

    public List<ToolDefinition> selectRelevantTools(String userRequest) {
        List<ToolDefinition> allTools = toolRegistry.getAllTools();
        List<ToolDefinition> relevantTools = new ArrayList<>();
        
        // 简单的关键词匹配，实际可以使用更复杂的算法
        String[] requestKeywords = userRequest.toLowerCase().split("\\s+");
        
        for (ToolDefinition tool : allTools) {
            String toolDescription = tool.getDescription().toLowerCase();
            boolean isRelevant = false;
            
            for (String keyword : requestKeywords) {
                if (toolDescription.contains(keyword)) {
                    isRelevant = true;
                    break;
                }
            }
            
            if (isRelevant) {
                relevantTools.add(tool);
            }
        }
        
        // 如果没有匹配到工具，返回所有工具
        return relevantTools.isEmpty() ? allTools : relevantTools;
    }
}
```

#### 11.2.2 工具元数据压缩
**原理**：优化工具描述，使用更简洁的语言

**实现**：
- 简化工具描述，只保留核心信息
- 使用标准化的描述格式
- 移除冗余信息

**示例**：
- **原始描述**："创建文件，参数包括filePath(文件路径)和content(文件内容)，可以创建各种类型的文件，支持文本文件、配置文件等"
- **压缩后**："创建文件，参数：filePath(文件路径), content(文件内容)"

#### 11.2.3 分层工具注册
**原理**：将工具分类，只发送相关类别的工具

**实现**：
- 将工具分为不同的类别（如文件操作、系统信息、网络请求等）
- 分析用户请求，确定相关的工具类别
- 只发送相关类别的工具

**Spring AI 实现示例**：
```java
@Tool(name = "createFile", description = "创建文件", category = "file")
public Map<String, Object> createFile(String filePath, String content) { /* ... */ }

@Tool(name = "systemInfo", description = "获取系统信息", category = "system")
public Map<String, Object> getSystemInfo() { /* ... */ }
```

#### 11.2.4 动态工具发现
**原理**：根据用户请求动态发现和加载相关工具

**实现**：
- 维护一个工具索引
- 根据用户请求查询相关工具
- 动态加载和注册这些工具

#### 11.2.5 工具摘要
**原理**：为每个工具生成简洁的摘要，而不是完整描述

**实现**：
- 为每个工具生成 1-2 句话的简洁摘要
- 只向大模型发送摘要信息
- 保留完整描述用于本地工具调用

#### 11.2.6 上下文管理
**原理**：智能管理上下文窗口，优先保留重要信息

**实现**：
- 监控上下文窗口的使用情况
- 当上下文窗口接近上限时，移除不重要的工具元数据
- 优先保留与用户请求最相关的工具

### 11.3 Spring AI 中的实际应用

Spring AI 1.1.0+ 版本提供了**工具选择器**（Tool Selector）机制，可以帮助解决工具元数据过大的问题：

#### 11.3.1 配置工具选择器
```yaml
spring:
  ai:
    chat:
      client:
        tool-selector:
          type: relevance # 基于相关性的工具选择
          relevance-threshold: 0.5 # 相关性阈值
```

#### 11.3.2 自定义工具选择器
```java
@Component
public class CustomToolSelector implements ToolSelector {

    @Override
    public List<ToolDefinition> selectTools(String userRequest, List<ToolDefinition> allTools) {
        // 实现自定义的工具选择逻辑
        // ...
    }
}
```

### 11.4 最佳实践

1. **保持工具描述简洁**：每个工具的描述控制在 1-2 句话内
2. **使用标准化格式**：统一工具描述的格式，便于解析和压缩
3. **定期清理工具**：移除不再使用的工具，保持工具列表的精简
4. **使用工具分类**：将工具分类，便于按需加载
5. **监控上下文使用**：定期监控上下文窗口的使用情况，优化工具元数据

## 13. 总结

Spring AI 提供了完整的 MCP 支持，包括本地 MCP 工具和远程 MCP Server 调用。通过 `@Tool` 注解和自动注册机制，开发者可以轻松实现 MCP 工具，让 AI 模型能够调用外部工具来完成复杂任务。

Agent 与 MCP 的结合，使得 AI 应用能够：
- 接收用户的自然语言请求
- 智能决策需要调用的工具
- 自动执行工具调用
- 整合工具执行结果生成最终响应

使用 MCP 和 Agent 可以显著扩展 AI 模型的能力，使其能够访问外部资源、执行复杂计算、与其他系统交互，从而实现更强大、更智能的 AI 应用。

## 14. 不同 AI Agent 应用的比较

### 14.1 核心原理的共性

#### 14.1.1 基本工作流程
几乎所有 AI Agent 应用都遵循以下核心流程：
- **接收请求**：接收用户的自然语言请求或其他输入
- **工具管理**：收集、注册和管理可用工具
- **大模型交互**：将请求和工具信息发送给大模型，获取决策
- **工具执行**：执行大模型指定的工具调用
- **结果处理**：整合工具执行结果，生成最终响应

#### 14.1.2 关键组件
- **Agent 核心**：协调各个组件的工作流程
- **工具注册表**：存储所有可用工具的元数据
- **大模型接口**：与大模型交互的桥梁
- **工具执行器**：执行本地或远程工具调用
- **结果处理器**：处理工具执行结果，生成最终响应

### 14.2 不同应用的差异与扩展

#### 14.2.1 工具数量和复杂度
- **简单 Agent**：可能只有几个简单工具（如天气查询、计算器等）
- **复杂 Agent**：可能有上百个复杂工具，涵盖多个领域（如代码生成、数据分析、系统管理等）
- **AI IDE**：集成了大量与代码相关的工具（代码生成、调试、运行、测试等）

#### 14.2.2 决策机制
- **基础 Agent**：单次决策，直接执行工具调用
- **复杂 Agent**：多轮决策，可能需要多次与大模型交互，逐步解决复杂问题
- **AI IDE**：结合了规则引擎和大模型决策，处理代码相关的复杂场景

#### 14.2.3 状态管理
- **无状态 Agent**：每次请求都是独立的，不保存对话历史
- **有状态 Agent**：保存对话历史，支持多轮对话
- **AI IDE**：管理复杂的 IDE 状态（打开的文件、光标位置、项目结构等）

#### 14.2.4 部署架构
- **单进程 Agent**：所有组件运行在同一个进程中
- **分布式 Agent**：工具服务部署在独立服务器上，通过网络调用
- **AI IDE**：与本地 IDE 深度集成，结合本地和远程服务

#### 14.2.5 安全机制
- **基础 Agent**：简单的输入验证
- **企业级 Agent**：严格的权限控制、审计日志、数据加密等
- **AI IDE**：代码安全检查、依赖分析、漏洞扫描等

### 14.3 当前 AI IDE 的底层原理

当前这个 AI IDE（Trae IDE）的底层原理在核心流程上与其他 AI Agent 类似，但有以下**特殊扩展**：

#### 14.3.1 深度代码理解
- 集成了代码解析引擎，能够理解代码结构、语法和语义
- 支持多种编程语言（Java、Python、JavaScript 等）
- 能够分析代码依赖、调用关系和执行流程

#### 14.3.2 实时交互能力
- 支持实时代码补全、错误提示和重构建议
- 能够与用户进行多轮对话，逐步完善代码
- 支持代码预览和即时执行

#### 14.3.3 IDE 集成
- 与本地 IDE 深度集成，访问文件系统、项目结构和 IDE API
- 支持打开、编辑、保存文件等 IDE 操作
- 能够运行和调试代码，查看执行结果

#### 14.3.4 多模态交互
- 支持文本输入（自然语言请求）
- 支持代码输入（直接编辑代码）
- 支持图形界面交互（点击、拖拽等）

#### 14.3.5 智能代码生成
- 基于大模型和代码理解，生成高质量代码
- 支持上下文感知，生成符合项目风格的代码
- 能够修复代码错误和优化代码结构

### 14.4 总结

**核心原理是一致的**：所有 AI Agent 应用都基于"接收请求→工具管理→大模型交互→工具执行→结果处理"的基本流程。

**差异在于扩展和优化**：不同应用会根据自身需求，在工具数量、决策机制、状态管理、部署架构和安全机制等方面进行扩展和优化。

**当前 AI IDE 的特殊性**：它在核心流程的基础上，深度集成了代码理解、实时交互、IDE 集成、多模态交互和智能代码生成等能力，专门针对软件开发场景进行了优化。

这种核心原理的一致性，使得 AI Agent 技术具有很强的通用性和扩展性，可以应用于各种不同的场景，从简单的智能助手到复杂的 AI IDE，再到企业级的自动化系统。

## 15. 参考资料

- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [MCP 协议规范](https://github.com/modelcontextprotocol/specification)
- [Spring AI MCP 实现](https://github.com/spring-projects/spring-ai/tree/main/spring-ai-mcp)
