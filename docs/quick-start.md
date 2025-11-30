# 快速开始指南

本文档提供最快速的启动指南，帮助你在 10 分钟内运行起项目。

## 前置检查清单

- [ ] JDK 21+ 已安装
- [ ] Maven 或 Gradle 已安装
- [ ] PostgreSQL 15+ 已安装
- [ ] pgvector 扩展已安装
- [ ] Node.js 18+ 已安装
- [ ] OpenAI API Key 已获取

## 5 分钟快速启动

### 步骤 1: 数据库准备（2 分钟）

```bash
# 连接到 PostgreSQL
psql -U postgres

# 执行以下 SQL
CREATE DATABASE ai_agent_dev;
\c ai_agent_dev
CREATE EXTENSION IF NOT EXISTS vector;
\q
```

### 步骤 2: 后端配置（1 分钟）

创建 `backend/src/main/resources/application.yml`:

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

spring.ai:
  openai:
    api-key: YOUR_OPENAI_API_KEY
    chat:
      options:
        model: gpt-3.5-turbo

server:
  port: 8080
```

### 步骤 3: 启动后端（1 分钟）

```bash
cd backend
mvn spring-boot:run
```

等待看到 `Started AiAgentApplication` 表示启动成功。

### 步骤 4: 前端配置（1 分钟）

```bash
cd frontend
npm install
```

创建 `frontend/.env`:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

### 步骤 5: 启动前端（1 分钟）

```bash
npm run dev
```

访问 `http://localhost:3000` 即可使用！

## 验证安装

### 测试后端 API

```bash
curl http://localhost:8080/actuator/health
```

应该返回：
```json
{"status":"UP"}
```

### 测试对话 API

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "你好",
    "useRAG": false
  }'
```

## 常见问题快速解决

### 问题 1: 数据库连接失败

**错误**: `Connection refused`

**解决**:
```bash
# 检查 PostgreSQL 是否运行
sudo systemctl status postgresql

# 启动 PostgreSQL
sudo systemctl start postgresql
```

### 问题 2: pgvector 扩展未找到

**错误**: `extension "vector" does not exist`

**解决**:
```bash
# 安装 pgvector（Ubuntu/Debian）
sudo apt install postgresql-15-pgvector

# 或从源码安装
git clone https://github.com/pgvector/pgvector.git
cd pgvector
make
sudo make install
```

### 问题 3: OpenAI API 错误

**错误**: `401 Unauthorized`

**解决**: 检查 API Key 是否正确设置，确保有足够的余额。

### 问题 4: 前端无法连接后端

**错误**: `CORS error` 或 `Network error`

**解决**: 
1. 检查后端是否运行在 8080 端口
2. 检查 `.env` 文件中的 API 地址
3. 检查后端 CORS 配置

## 下一步

完成快速启动后，建议：

1. 📖 阅读 [开发指南](development-guide.md) 了解详细开发流程
2. 🏗️ 阅读 [架构文档](architecture.md) 了解系统设计
3. 📡 阅读 [API 文档](api.md) 了解接口详情
4. 🚀 阅读 [部署文档](deployment.md) 准备生产部署

## 开发工具推荐

### IDE
- **IntelliJ IDEA**（推荐，对 Spring Boot 支持最好）
- **VS Code**（轻量级，适合前端）

### API 测试
- **Postman**
- **Insomnia**
- **curl**（命令行）

### 数据库工具
- **pgAdmin**
- **DBeaver**
- **DataGrip**

### 前端工具
- **React DevTools**（浏览器扩展）
- **Vue DevTools**（浏览器扩展）

## 获取帮助

- 📚 查看完整文档
- 🐛 提交 Issue
- 💬 加入讨论群

