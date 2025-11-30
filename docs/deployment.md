# 部署文档

## 部署概述

本文档介绍如何将 AI 对话应用部署到生产环境。

## 部署架构

### 推荐架构

```
┌─────────────────────────────────────────┐
│         负载均衡器 (Nginx/ALB)            │
└─────────────────────────────────────────┘
                    │
        ┌───────────┴───────────┐
        │                       │
┌───────▼──────┐      ┌────────▼────────┐
│  前端服务器   │      │   后端服务器     │
│  (Nginx)     │      │  (Spring Boot)  │
└──────────────┘      └─────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
            ┌───────▼──────┐    ┌──────▼──────┐
            │  PostgreSQL  │    │    Redis    │
            │  + pgvector  │    │   (缓存)    │
            └──────────────┘    └─────────────┘
```

## 环境要求

### 服务器要求

**后端服务器：**
- CPU: 4 核或以上
- 内存: 8GB 或以上
- 磁盘: 100GB 或以上（SSD 推荐）
- Java 21+

**数据库服务器：**
- CPU: 4 核或以上
- 内存: 16GB 或以上
- 磁盘: 500GB 或以上（SSD 必需）
- PostgreSQL 15+

**前端服务器：**
- CPU: 2 核或以上
- 内存: 2GB 或以上
- Nginx 或类似 Web 服务器

## 部署步骤

### 1. 数据库部署

#### 1.1 安装 PostgreSQL

**Ubuntu/Debian:**
```bash
sudo apt update
sudo apt install postgresql-15 postgresql-contrib-15
```

**CentOS/RHEL:**
```bash
sudo yum install postgresql15-server postgresql15-contrib
```

#### 1.2 安装 pgvector 扩展

```bash
# 克隆 pgvector 仓库
git clone --branch v0.5.1 https://github.com/pgvector/pgvector.git
cd pgvector

# 编译安装
make
sudo make install
```

#### 1.3 配置 PostgreSQL

编辑 `/etc/postgresql/15/main/postgresql.conf`:

```conf
# 性能优化
shared_buffers = 4GB
effective_cache_size = 12GB
maintenance_work_mem = 1GB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1
effective_io_concurrency = 200
work_mem = 20MB
min_wal_size = 1GB
max_wal_size = 4GB
max_connections = 200
```

编辑 `/etc/postgresql/15/main/pg_hba.conf`:

```conf
# 允许应用服务器连接
host    all             all             10.0.0.0/24           md5
```

#### 1.4 创建数据库和用户

```sql
-- 创建数据库
CREATE DATABASE ai_agent_db;

-- 创建用户
CREATE USER ai_agent_user WITH PASSWORD 'strong_password';

-- 授权
GRANT ALL PRIVILEGES ON DATABASE ai_agent_db TO ai_agent_user;

-- 连接到数据库
\c ai_agent_db

-- 启用扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 授权给用户
GRANT ALL ON SCHEMA public TO ai_agent_user;
```

### 2. 后端部署

#### 2.1 构建应用

```bash
cd backend
mvn clean package -DskipTests
# 或
./gradlew clean build -x test
```

#### 2.2 创建应用用户

```bash
sudo useradd -r -s /bin/false aiagent
```

#### 2.3 部署应用文件

```bash
sudo mkdir -p /opt/ai-agent
sudo cp target/ai-agent-1.0.0.jar /opt/ai-agent/
sudo chown -R aiagent:aiagent /opt/ai-agent
```

#### 2.4 创建 systemd 服务

创建 `/etc/systemd/system/ai-agent.service`:

```ini
[Unit]
Description=AI Agent Application
After=network.target postgresql.service

[Service]
Type=simple
User=aiagent
WorkingDirectory=/opt/ai-agent
ExecStart=/usr/bin/java -jar -Xms2g -Xmx4g \
  -Dspring.profiles.active=prod \
  /opt/ai-agent/ai-agent-1.0.0.jar
Restart=always
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=ai-agent

[Install]
WantedBy=multi-user.target
```

#### 2.5 配置应用

创建 `/opt/ai-agent/application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db-server:5432/ai_agent_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
  
  # MyBatis 配置
  mybatis:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.aiagent.model
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.slf4j.Slf4jImpl

spring.ai:
  # 使用 OpenAI（可选）
  openai:
    api-key: ${OPENAI_API_KEY}
    chat:
      options:
        model: gpt-4
        temperature: 0.7
  
  # 使用 Ollama（本地模型，推荐）
  ollama:
    base-url: ${OLLAMA_BASE_URL:http://localhost:11434}
    chat:
      options:
        model: ${OLLAMA_MODEL:llama2}
        temperature: 0.7
        num-predict: 2000

server:
  port: 8080
  compression:
    enabled: true

logging:
  level:
    root: INFO
    com.aiagent: INFO
  file:
    name: /var/log/ai-agent/application.log
    max-size: 100MB
    max-history: 30
```

创建环境变量文件 `/opt/ai-agent/.env`:

**使用 OpenAI：**
```bash
DB_USERNAME=ai_agent_user
DB_PASSWORD=strong_password
OPENAI_API_KEY=your_api_key
```

**使用 Ollama（本地模型）：**
```bash
DB_USERNAME=ai_agent_user
DB_PASSWORD=strong_password
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=llama2
```

#### 2.6 启动服务

```bash
sudo systemctl daemon-reload
sudo systemctl enable ai-agent
sudo systemctl start ai-agent
sudo systemctl status ai-agent
```

### 3. 前端部署

#### 3.1 构建前端

```bash
cd frontend
npm install
npm run build
```

#### 3.2 部署到 Nginx

```bash
sudo cp -r dist/* /var/www/ai-agent/
```

#### 3.3 配置 Nginx

创建 `/etc/nginx/sites-available/ai-agent`:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    root /var/www/ai-agent;
    index index.html;

    # API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_cache_bypass $http_upgrade;
        
        # 超时设置
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # 前端路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript 
               application/x-javascript application/xml+rss 
               application/json application/javascript;
}
```

启用配置：

```bash
sudo ln -s /etc/nginx/sites-available/ai-agent /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 4. Redis 部署（可选，用于缓存）

#### 4.1 安装 Redis

```bash
sudo apt install redis-server
# 或
sudo yum install redis
```

#### 4.2 配置 Redis

编辑 `/etc/redis/redis.conf`:

```conf
maxmemory 2gb
maxmemory-policy allkeys-lru
```

#### 4.3 启动 Redis

```bash
sudo systemctl enable redis
sudo systemctl start redis
```

## Docker 部署

### 1. Docker Compose 配置

创建 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: pgvector/pgvector:pg15
    container_name: ai-agent-postgres
    environment:
      POSTGRES_DB: ai_agent_db
      POSTGRES_USER: ai_agent_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - ai-agent-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ai_agent_user"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: ai-agent-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - ai-agent-network
    command: redis-server --appendonly yes --maxmemory 2gb --maxmemory-policy allkeys-lru

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
    # 可选：预下载模型
    # environment:
    #   - OLLAMA_KEEP_ALIVE=24h

  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: ai-agent-backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ai_agent_db
      SPRING_DATASOURCE_USERNAME: ai_agent_user
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      # 使用 OpenAI（可选）
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      # 使用 Ollama（本地模型）
      SPRING_AI_OLLAMA_BASE_URL: http://ollama:11434
      SPRING_AI_OLLAMA_CHAT_OPTIONS_MODEL: ${OLLAMA_MODEL:llama2}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started
      ollama:
        condition: service_started
    networks:
      - ai-agent-network
    restart: unless-stopped

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: ai-agent-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - ai-agent-network
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: ai-agent-nginx
    ports:
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - frontend
      - backend
    networks:
      - ai-agent-network
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:
  ollama_data:

networks:
  ai-agent-network:
    driver: bridge
```

### 2. 后端 Dockerfile

创建 `backend/Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/ai-agent-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Xms2g", "-Xmx4g", "app.jar"]
```

### 3. 前端 Dockerfile

创建 `frontend/Dockerfile`:

```dockerfile
# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# 运行阶段
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

### 4. 部署命令

```bash
# 构建并启动
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止
docker-compose down

# 停止并删除数据
docker-compose down -v
```

## 云平台部署

### AWS 部署

#### 使用 Elastic Beanstalk

1. 打包应用
2. 创建 Elastic Beanstalk 环境
3. 配置 RDS PostgreSQL（启用 pgvector）
4. 配置环境变量
5. 部署应用

#### 使用 ECS/Fargate

1. 构建 Docker 镜像并推送到 ECR
2. 创建 ECS 任务定义
3. 创建 RDS 数据库实例
4. 配置负载均衡器
5. 部署服务

### Azure 部署

1. 创建 Azure App Service
2. 创建 Azure Database for PostgreSQL（配置 pgvector）
3. 配置应用设置
4. 部署应用

### 阿里云部署

1. 创建 ECS 实例
2. 创建 RDS PostgreSQL 实例
3. 配置安全组
4. 部署应用

## Ollama 配置说明

### 使用 Docker 安装的 Ollama

如果您已经在 macOS 上通过 Docker 安装了 Ollama，可以按以下方式配置：

#### 1. 验证 Ollama 运行状态

```bash
# 检查 Ollama 容器是否运行
docker ps | grep ollama

# 测试 Ollama API
curl http://localhost:11434/api/tags
```

#### 2. 下载模型

```bash
# 如果 Ollama 在 Docker 容器中
docker exec -it <ollama-container-name> ollama pull llama2

# 或者如果 Ollama 直接安装在主机上
ollama pull llama2

# 其他推荐的模型：
# ollama pull mistral      # Mistral 7B
# ollama pull codellama    # Code Llama
# ollama pull llama2:13b   # Llama 2 13B（更大模型）
```

#### 3. 配置 Spring AI

在 `application.yml` 中：

```yaml
spring.ai:
  ollama:
    base-url: http://localhost:11434  # Docker 映射的端口
    chat:
      options:
        model: llama2
        temperature: 0.7
        num-predict: 2000
  
  # 如果使用 Ollama 的 embedding 模型
  embedding:
    ollama:
      base-url: http://localhost:11434
      options:
        model: nomic-embed-text  # 下载：ollama pull nomic-embed-text
```

#### 4. Docker Compose 集成

如果使用 Docker Compose，确保 Ollama 服务在同一个网络中：

```yaml
services:
  ollama:
    image: ollama/ollama:latest
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama
    networks:
      - ai-agent-network

  backend:
    environment:
      SPRING_AI_OLLAMA_BASE_URL: http://ollama:11434  # 使用服务名
    depends_on:
      - ollama
```

#### 5. 性能优化建议

- **模型选择**：根据硬件资源选择合适的模型大小
  - `llama2` (7B): 需要 ~4GB RAM
  - `llama2:13b` (13B): 需要 ~8GB RAM
  - `mistral` (7B): 性能优秀，推荐
- **GPU 支持**：如果 Docker 支持 GPU，可以启用 GPU 加速
- **内存管理**：Ollama 会缓存模型，注意内存使用

#### 6. 常见问题

**Q: 如何查看已下载的模型？**
```bash
curl http://localhost:11434/api/tags
```

**Q: 如何删除模型？**
```bash
docker exec -it <ollama-container> ollama rm llama2
```

**Q: Ollama 响应慢怎么办？**
- 检查系统资源（CPU/内存）
- 考虑使用更小的模型
- 启用 GPU 加速（如果可用）

## 监控和日志

### 应用监控

推荐使用：
- **Prometheus** + **Grafana**：指标监控
- **ELK Stack**：日志聚合
- **Sentry**：错误追踪
- **New Relic** 或 **Datadog**：APM

### 日志配置

确保日志文件轮转：

```yaml
logging:
  file:
    name: /var/log/ai-agent/application.log
    max-size: 100MB
    max-history: 30
    total-size-cap: 3GB
```

### 健康检查

应用提供健康检查端点：

```bash
curl http://localhost:8080/actuator/health
```

## 备份和恢复

### 数据库备份

```bash
# 备份
pg_dump -U ai_agent_user -d ai_agent_db > backup_$(date +%Y%m%d).sql

# 恢复
psql -U ai_agent_user -d ai_agent_db < backup_20240101.sql
```

### 自动备份脚本

```bash
#!/bin/bash
BACKUP_DIR="/backup/postgres"
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -U ai_agent_user -d ai_agent_db | gzip > $BACKUP_DIR/backup_$DATE.sql.gz
# 保留最近 30 天的备份
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete
```

添加到 crontab：

```bash
0 2 * * * /path/to/backup.sh
```

## 安全建议

1. **使用 HTTPS**：配置 SSL/TLS 证书
2. **防火墙**：只开放必要端口
3. **数据库安全**：使用强密码，限制访问 IP
4. **API 密钥**：使用环境变量或密钥管理服务
5. **定期更新**：保持系统和依赖更新
6. **访问控制**：实施认证和授权
7. **日志审计**：记录所有重要操作

## 性能优化

1. **数据库索引**：确保向量索引正确创建
2. **连接池**：优化数据库连接池配置
3. **缓存**：使用 Redis 缓存常用数据
4. **CDN**：静态资源使用 CDN
5. **负载均衡**：多实例部署
6. **异步处理**：文档处理使用异步任务

## 故障排查

### 常见问题

1. **应用无法启动**
   - 检查日志：`journalctl -u ai-agent -f`
   - 检查端口占用
   - 检查数据库连接

2. **数据库连接失败**
   - 检查防火墙规则
   - 检查 PostgreSQL 配置
   - 验证用户权限

3. **向量搜索慢**
   - 检查索引是否创建
   - 优化 HNSW 参数
   - 检查服务器资源

4. **内存不足**
   - 增加 JVM 堆内存
   - 优化批处理大小
   - 检查内存泄漏

## 回滚策略

1. 保留之前的版本
2. 使用蓝绿部署
3. 数据库迁移回滚脚本
4. 快速回滚流程文档

