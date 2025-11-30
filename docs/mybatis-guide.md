# MyBatis 使用指南

本文档介绍如何在项目中使用 MyBatis 进行数据访问。

## 1. 依赖配置

### Maven

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>3.0.3</version>
</dependency>
```

### Gradle

```gradle
implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
```

## 2. 配置文件

在 `application.yml` 中配置 MyBatis：

```yaml
spring:
  mybatis:
    # Mapper XML 文件位置
    mapper-locations: classpath:mapper/*.xml
    # 实体类包路径（用于类型别名）
    type-aliases-package: com.aiagent.model
    configuration:
      # 开启驼峰命名转换（数据库下划线 -> Java 驼峰）
      map-underscore-to-camel-case: true
      # 日志实现（开发环境使用 StdOutImpl，生产环境使用 Slf4jImpl）
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

## 3. Mapper 接口

### 基本 Mapper 接口

```java
package com.aiagent.mapper;

import com.aiagent.model.Document;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface DocumentMapper {
    /**
     * 插入文档
     */
    void insert(Document document);
    
    /**
     * 更新文档
     */
    void update(Document document);
    
    /**
     * 根据 ID 查询
     */
    Document selectById(String id);
    
    /**
     * 查询所有文档
     */
    List<Document> selectAll();
    
    /**
     * 根据状态查询
     */
    List<Document> selectByStatus(String status);
    
    /**
     * 删除文档
     */
    void deleteById(String id);
}
```

### 使用注解的简单查询

```java
@Mapper
public interface DocumentMapper {
    @Select("SELECT * FROM documents WHERE id = #{id}")
    Document selectById(String id);
    
    @Insert("INSERT INTO documents (id, title, status) VALUES (#{id}, #{title}, #{status})")
    void insert(Document document);
    
    @Update("UPDATE documents SET title = #{title} WHERE id = #{id}")
    void update(Document document);
    
    @Delete("DELETE FROM documents WHERE id = #{id}")
    void deleteById(String id);
}
```

## 4. Mapper XML 文件

### 基本结构

创建 `src/main/resources/mapper/DocumentMapper.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" 
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.aiagent.mapper.DocumentMapper">
    
    <!-- ResultMap 定义 -->
    <resultMap id="DocumentResultMap" type="Document">
        <id property="id" column="id"/>
        <result property="title" column="title"/>
        <result property="fileName" column="file_name"/>
        <result property="fileType" column="file_type"/>
        <result property="fileSize" column="file_size"/>
        <result property="content" column="content"/>
        <result property="status" column="status"/>
        <result property="createdAt" column="created_at" 
                jdbcType="TIMESTAMP"/>
        <result property="updatedAt" column="updated_at" 
                jdbcType="TIMESTAMP"/>
    </resultMap>
    
    <!-- 插入 -->
    <insert id="insert" parameterType="Document">
        INSERT INTO documents (
            id, title, file_name, file_type, file_size,
            content, status, created_at, updated_at
        ) VALUES (
            #{id}, #{title}, #{fileName}, #{fileType}, #{fileSize},
            #{content}, #{status}, #{createdAt}, #{updatedAt}
        )
    </insert>
    
    <!-- 更新 -->
    <update id="update" parameterType="Document">
        UPDATE documents
        <set>
            <if test="title != null">title = #{title},</if>
            <if test="content != null">content = #{content},</if>
            <if test="status != null">status = #{status},</if>
            updated_at = #{updatedAt}
        </set>
        WHERE id = #{id}
    </update>
    
    <!-- 根据 ID 查询 -->
    <select id="selectById" resultMap="DocumentResultMap">
        SELECT * FROM documents WHERE id = #{id}
    </select>
    
    <!-- 查询所有 -->
    <select id="selectAll" resultMap="DocumentResultMap">
        SELECT * FROM documents ORDER BY created_at DESC
    </select>
    
    <!-- 根据状态查询 -->
    <select id="selectByStatus" resultMap="DocumentResultMap">
        SELECT * FROM documents 
        WHERE status = #{status}
        ORDER BY created_at DESC
    </select>
    
    <!-- 删除 -->
    <delete id="deleteById">
        DELETE FROM documents WHERE id = #{id}
    </delete>
</mapper>
```

## 5. 处理 PostgreSQL 特殊类型

### 处理 vector 类型

PostgreSQL 的 `vector` 类型需要特殊处理。创建自定义 TypeHandler：

```java
package com.aiagent.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.*;
import java.util.Arrays;
import java.util.stream.Collectors;

@MappedTypes(float[].class)
public class VectorTypeHandler extends BaseTypeHandler<float[]> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, 
                                    float[] parameter, JdbcType jdbcType) 
            throws SQLException {
        // 将 float[] 转换为 PostgreSQL vector 格式: [1.0,2.0,3.0]
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
    
    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) 
            throws SQLException {
        String vectorStr = rs.getString(columnIndex);
        return parseVector(vectorStr);
    }
    
    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) 
            throws SQLException {
        String vectorStr = cs.getString(columnIndex);
        return parseVector(vectorStr);
    }
    
    private float[] parseVector(String vectorStr) {
        if (vectorStr == null || vectorStr.isEmpty()) {
            return null;
        }
        // 移除方括号并分割
        String content = vectorStr.replaceAll("[\\[\\]]", "");
        String[] parts = content.split(",");
        float[] result = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Float.parseFloat(parts[i].trim());
        }
        return result;
    }
}
```

在 Mapper XML 中使用：

```xml
<insert id="insertChunk" parameterType="DocumentChunk">
    INSERT INTO document_chunks (
        id, document_id, chunk_index, content, embedding, created_at
    ) VALUES (
        #{id}, #{documentId}, #{chunkIndex}, #{content},
        #{embedding,typeHandler=com.aiagent.typehandler.VectorTypeHandler},
        #{createdAt}
    )
</insert>

<select id="selectChunkById" resultMap="ChunkResultMap">
    SELECT 
        id, document_id, chunk_index, content,
        embedding,
        created_at
    FROM document_chunks
    WHERE id = #{id}
</select>
```

### 处理 JSONB 类型

对于 JSONB 类型，可以使用 MyBatis 内置的 MapTypeHandler：

```xml
<resultMap id="MessageResultMap" type="ChatMessage">
    <id property="id" column="id"/>
    <result property="content" column="content"/>
    <result property="metadata" column="metadata" 
            typeHandler="org.apache.ibatis.type.MapTypeHandler"/>
</resultMap>
```

或者创建自定义 TypeHandler 处理复杂对象：

```java
@MappedTypes(Map.class)
public class JsonbTypeHandler extends BaseTypeHandler<Map<String, Object>> {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, 
                                    Map<String, Object> parameter, 
                                    JdbcType jdbcType) throws SQLException {
        try {
            String json = objectMapper.writeValueAsString(parameter);
            ps.setString(i, json);
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting Map to JSON", e);
        }
    }
    
    @Override
    public Map<String, Object> getNullableResult(ResultSet rs, 
                                                  String columnName) 
            throws SQLException {
        String json = rs.getString(columnName);
        return parseJson(json);
    }
    
    // ... 其他方法实现
}
```

### 处理数组类型

PostgreSQL 的数组类型（如 `TEXT[]`）：

```xml
<resultMap id="MessageResultMap" type="ChatMessage">
    <result property="sources" column="sources" 
            typeHandler="org.apache.ibatis.type.ArrayTypeHandler"/>
</resultMap>
```

或使用自定义 TypeHandler：

```java
@MappedTypes(String[].class)
public class StringArrayTypeHandler extends BaseTypeHandler<String[]> {
    
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, 
                                    String[] parameter, JdbcType jdbcType) 
            throws SQLException {
        Array array = ps.getConnection().createArrayOf("text", parameter);
        ps.setArray(i, array);
    }
    
    @Override
    public String[] getNullableResult(ResultSet rs, String columnName) 
            throws SQLException {
        Array array = rs.getArray(columnName);
        return array != null ? (String[]) array.getArray() : null;
    }
    
    // ... 其他方法实现
}
```

## 6. 动态 SQL

### if 条件

```xml
<select id="selectByCondition" resultMap="DocumentResultMap">
    SELECT * FROM documents
    <where>
        <if test="title != null and title != ''">
            AND title LIKE CONCAT('%', #{title}, '%')
        </if>
        <if test="status != null">
            AND status = #{status}
        </if>
        <if test="fileType != null">
            AND file_type = #{fileType}
        </if>
    </where>
    ORDER BY created_at DESC
</select>
```

### choose/when/otherwise

```xml
<select id="selectByType" resultMap="DocumentResultMap">
    SELECT * FROM documents
    <where>
        <choose>
            <when test="type == 'pdf'">
                AND file_type = 'application/pdf'
            </when>
            <when test="type == 'word'">
                AND file_type IN ('application/msword', 
                                  'application/vnd.openxmlformats-officedocument.wordprocessingml.document')
            </when>
            <otherwise>
                AND file_type IS NOT NULL
            </otherwise>
        </choose>
    </where>
</select>
```

### foreach 循环

```xml
<select id="selectByIds" resultMap="DocumentResultMap">
    SELECT * FROM documents
    WHERE id IN
    <foreach collection="ids" item="id" open="(" separator="," close=")">
        #{id}
    </foreach>
</select>
```

## 7. 分页查询

### 使用 PageHelper（推荐）

添加依赖：

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>1.4.7</version>
</dependency>
```

使用：

```java
@Service
public class DocumentService {
    
    @Autowired
    private DocumentMapper documentMapper;
    
    public PageInfo<Document> getDocuments(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Document> documents = documentMapper.selectAll();
        return new PageInfo<>(documents);
    }
}
```

### 手动分页

```xml
<select id="selectWithPagination" resultMap="DocumentResultMap">
    SELECT * FROM documents
    ORDER BY created_at DESC
    LIMIT #{pageSize} OFFSET #{offset}
</select>
```

## 8. 事务管理

MyBatis 与 Spring 事务管理集成：

```java
@Service
@Transactional
public class DocumentService {
    
    @Autowired
    private DocumentMapper documentMapper;
    
    @Autowired
    private DocumentChunkMapper chunkMapper;
    
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(String id) {
        // 删除文档块
        chunkMapper.deleteByDocumentId(id);
        // 删除文档
        documentMapper.deleteById(id);
    }
}
```

## 9. 最佳实践

1. **使用 ResultMap**：避免字段映射错误
2. **参数验证**：在 Service 层进行参数验证
3. **SQL 优化**：使用索引，避免全表扫描
4. **批量操作**：使用 `foreach` 进行批量插入/更新
5. **连接池配置**：合理配置 HikariCP 连接池
6. **日志级别**：生产环境使用 `Slf4jImpl`，开发环境使用 `StdOutImpl`

## 10. 常见问题

### Q: Mapper 接口找不到？

A: 确保：
1. 使用 `@Mapper` 注解或在主类上使用 `@MapperScan`
2. Mapper XML 文件路径配置正确
3. Mapper 接口和 XML 的 namespace 一致

### Q: 字段映射失败？

A: 
1. 检查 `map-underscore-to-camel-case` 是否开启
2. 使用 `ResultMap` 明确指定映射关系
3. 检查数据库字段名和 Java 属性名

### Q: 如何处理复杂查询？

A: 
1. 使用动态 SQL
2. 使用 `<include>` 标签复用 SQL 片段
3. 考虑使用存储过程（不推荐）

### Q: 性能优化？

A:
1. 使用索引
2. 避免 N+1 查询问题
3. 使用批量操作
4. 合理使用缓存

## 参考资源

- [MyBatis 官方文档](https://mybatis.org/mybatis-3/)
- [MyBatis Spring Boot Starter](https://mybatis.org/spring-boot-starter/mybatis-spring-boot-autoconfigure/)
- [PostgreSQL JDBC 驱动](https://jdbc.postgresql.org/)







