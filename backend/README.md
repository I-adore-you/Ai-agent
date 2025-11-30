# AI Agent Backend

基于 Spring AI 的 AI 对话应用后端服务。

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── aiagent/
│   │   │           ├── AiAgentApplication.java    # 主启动类
│   │   │           ├── config/                    # 配置类
│   │   │           ├── controller/                # 控制器
│   │   │           ├── service/                   # 服务层
│   │   │           ├── mapper/                    # MyBatis Mapper 接口
│   │   │           ├── model/                     # 实体类
│   │   │           └── dto/                       # 数据传输对象
│   │   └── resources/
│   │       ├── application.yml                    # 应用配置
│   │       ├── mapper/                            # MyBatis XML 映射文件
│   │       ├── static/                            # 静态资源
│   │       └── templates/                         # 模板文件
│   └── test/                                      # 测试代码
├── pom.xml                                        # Maven 配置
└── README.md                                      # 项目说明
```

## 技术栈

- **Spring Boot 3.2.0**
- **Spring AI 1.0.0-M4**
- **MyBatis 3.0.3**
- **PostgreSQL + pgvector**
- **Ollama** (本地 AI 模型)

## 快速开始

### 1. 前置要求

- JDK 21+
- Maven 3.8+
- PostgreSQL 15+ (已安装 pgvector 扩展)
- Ollama (已安装并运行)

### 2. 配置数据库

```sql
CREATE DATABASE ai_agent_db;
CREATE EXTENSION IF NOT EXISTS vector;
```

### 3. 配置应用

编辑 `src/main/resources/application.yml`，修改数据库连接和 Ollama 配置。

### 4. 运行项目

```bash
mvn spring-boot:run
```

或

```bash
mvn clean package
java -jar target/backend-1.0.0-SNAPSHOT.jar
```

### 5. 验证

访问健康检查端点：
```bash
curl http://localhost:8080/actuator/health
```

## 开发说明

项目已创建基础骨架，包含：

- ✅ Spring Boot 主类
- ✅ 基础包结构 (config, controller, service, mapper, model, dto)
- ✅ 配置文件 (application.yml)
- ✅ Maven 依赖配置（MyBatis）
- ✅ CORS 配置
- ✅ MyBatis 配置（mapper 目录已创建）

后续开发可以在此基础上添加具体功能实现。

## MyBatis 使用说明

### Mapper 接口
在 `com.aiagent.mapper` 包下创建 Mapper 接口：
```java
@Mapper
public interface UserMapper {
    User selectById(Long id);
}
```

### XML 映射文件
在 `src/main/resources/mapper/` 目录下创建对应的 XML 文件：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.aiagent.mapper.UserMapper">
    <select id="selectById" resultType="com.aiagent.model.User">
        SELECT * FROM users WHERE id = #{id}
    </select>
</mapper>
```



