# 文档索引

欢迎使用 AI 对话应用文档！本文档索引帮助你快速找到需要的信息。

## 📚 文档导航

### 🚀 开始使用

- **[快速开始指南](quick-start.md)** ⭐ 推荐新手
  - 5 分钟快速启动项目
  - 常见问题快速解决
  - 开发工具推荐

- **[README.md](../README.md)** - 项目主文档
  - 项目概述和功能特性
  - 技术栈介绍
  - 快速开始步骤
  - 项目结构说明

### 🏗️ 架构和设计

- **[架构设计文档](architecture.md)**
  - 系统架构概述
  - 核心模块设计（RAG、Agent、MCP）
  - 数据模型设计
  - API 设计规范
  - 安全设计
  - 性能优化策略

### 💻 开发指南

- **[开发指南](development-guide.md)**
  - 开发环境设置
  - 项目结构说明
  - 核心功能实现示例
  - 测试指南
  - 代码规范
  - 调试技巧

- **[MyBatis 使用指南](mybatis-guide.md)**
  - MyBatis 配置
  - Mapper 接口和 XML
  - PostgreSQL 特殊类型处理
  - 动态 SQL
  - 分页查询
  - 最佳实践

### 📡 API 文档

- **[API 文档](api.md)**
  - 对话 API
  - 文档 API
  - Agent API
  - MCP API
  - 错误码说明
  - 请求/响应示例

### 🚢 部署文档

- **[部署文档](deployment.md)**
  - 部署架构
  - 环境要求
  - 部署步骤（传统部署、Docker、云平台）
  - 监控和日志
  - 备份和恢复
  - 安全建议

## 📖 按角色阅读

### 我是新手开发者

1. 先读 [快速开始指南](quick-start.md)
2. 然后读 [开发指南](development-guide.md)
3. 需要了解架构时读 [架构设计文档](architecture.md)

### 我是前端开发者

1. [开发指南](development-guide.md) - 前端部分
2. [API 文档](api.md) - 了解后端接口
3. [快速开始指南](quick-start.md) - 环境设置

### 我是后端开发者

1. [开发指南](development-guide.md) - 后端部分
2. [架构设计文档](architecture.md) - 系统设计
3. [API 文档](api.md) - API 规范

### 我是 DevOps/运维

1. [部署文档](deployment.md) - 完整部署流程
2. [架构设计文档](architecture.md) - 了解系统架构
3. [API 文档](api.md) - 了解接口规范

### 我是架构师/技术负责人

1. [架构设计文档](architecture.md) - 系统架构
2. [README.md](../README.md) - 项目概述
3. [部署文档](deployment.md) - 部署架构

## 🔍 按主题查找

### RAG（检索增强生成）

- [架构设计文档 - RAG 模块](architecture.md#21-rag-模块)
- [开发指南 - RAG 服务实现](development-guide.md#1-rag-服务实现)
- [API 文档 - 对话 API](api.md#对话-api)

### Agent（智能代理）

- [架构设计文档 - Agent 模块](architecture.md#22-agent-模块)
- [开发指南 - Agent 服务实现](development-guide.md#2-agent-服务实现)
- [API 文档 - Agent API](api.md#agent-api)

### MCP（模型上下文协议）

- [架构设计文档 - MCP 模块](architecture.md#23-mcp-模块)
- [API 文档 - MCP API](api.md#mcp-api)
- [LLM、Agent、MCP 关系解析](llm-agent-mcp-relationship.md) - 理解三者关系

### 向量数据库

- [架构设计文档 - 数据模型设计](architecture.md#3-数据模型设计)
- [部署文档 - 数据库部署](deployment.md#1-数据库部署)
- [开发指南 - 开发环境设置](development-guide.md#2-本地数据库设置)

### 前端开发

- [开发指南 - 前端结构](development-guide.md#前端结构react-示例)
- [开发指南 - 前端实现示例](development-guide.md#3-前端实现示例)

### 部署

- [部署文档](deployment.md) - 完整部署指南
- [快速开始指南](quick-start.md) - 本地开发环境

### 对比分析

- **[Dify vs 当前项目对比](dify-comparison.md)**
  - 产品定位对比
  - 功能实现方式对比
  - 使用场景对比
  - 技术架构对比

### 概念解析

- **[LLM、Agent、MCP 关系解析](llm-agent-mcp-relationship.md)**
  - LLM、Agent、MCP 核心概念
  - 三者之间的关系
  - 项目定位分析（Agent 应用 vs MCP Server）
  - 实际应用示例

- **[Spring AI 角色解析](spring-ai-role.md)**
  - Spring AI 的定位和作用
  - Spring AI vs Agent 框架的区别
  - Spring AI 在项目中的实际使用
  - 核心组件和功能

- **[MCP 配置文件解析](mcp-config-explanation.md)**
  - mcp.json 配置文件详解
  - 字段说明和配置示例
  - 安全注意事项

- **[MCP 在 Cursor 中的使用指南](cursor-mcp-usage-guide.md)** ⭐ 实用指南
  - 如何配置 MCP 服务器
  - 如何在 Cursor 中使用 MCP
  - 实际使用场景和示例
  - 与你的项目集成

- **[Cursor 模式解析与 Agent 对比](cursor-modes-and-agent-comparison.md)** ⭐ 重要对比
  - Cursor 三个模式（Ask、Plan、Agent）详解
  - Cursor Agent vs 你的项目 Agent 对比
  - 本质分析和应用场景
  - 功能对比表

## 📝 文档更新日志

- **2024-01-01**: 初始版本发布
  - 创建所有核心文档
  - 包含 RAG、Agent、MCP 功能说明
  - 提供完整的开发和部署指南

## 🤝 贡献文档

如果你发现文档有错误或需要改进，欢迎：

1. 提交 Issue 报告问题
2. 提交 Pull Request 改进文档
3. 在讨论区提出建议

## 📧 获取帮助

- 📚 查看本文档索引
- 🐛 提交 Issue
- 💬 加入讨论群
- 📖 阅读相关技术文档

---

**提示**: 建议收藏本文档索引，方便随时查阅！

