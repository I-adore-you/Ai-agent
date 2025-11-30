package com.aiagent.service;

import org.springframework.ai.chat.tools.Tool;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Spring AI MCP 工具服务类
 * 
 * 使用 @Tool 注解标记可调用的工具方法
 * 
 * @author ego
 * @date 2025-11-30
 */
@Service
public class McpToolService {

    /**
     * 获取系统信息工具
     */
    @Tool(name = "systemInfo", description = "获取系统资源信息，包括操作系统、内存、磁盘等信息")
    public Map<String, Object> getSystemInfo() {
        try {
            // 获取系统属性
            Properties props = System.getProperties();
            Map<String, Object> systemInfo = new HashMap<>();

            // 基本系统信息
            systemInfo.put("os.name", props.getProperty("os.name"));
            systemInfo.put("os.version", props.getProperty("os.version"));
            systemInfo.put("os.arch", props.getProperty("os.arch"));
            systemInfo.put("java.version", props.getProperty("java.version"));
            systemInfo.put("user.name", props.getProperty("user.name"));
            systemInfo.put("user.home", props.getProperty("user.home"));
            systemInfo.put("user.dir", props.getProperty("user.dir"));

            // 内存信息
            Runtime runtime = Runtime.getRuntime();
            systemInfo.put("totalMemory", runtime.totalMemory());
            systemInfo.put("freeMemory", runtime.freeMemory());
            systemInfo.put("maxMemory", runtime.maxMemory());
            systemInfo.put("availableProcessors", runtime.availableProcessors());

            // 磁盘信息
            File root = new File("/");
            systemInfo.put("disk.totalSpace", root.getTotalSpace());
            systemInfo.put("disk.freeSpace", root.getFreeSpace());
            systemInfo.put("disk.usableSpace", root.getUsableSpace());

            return systemInfo;
        } catch (Exception e) {
            throw new RuntimeException("获取系统信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建文件工具
     */
    @Tool(name = "createFile", description = "创建文件，参数包括filePath(文件路径)和content(文件内容)")
    public Map<String, Object> createFile(String filePath, String content) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                throw new IllegalArgumentException("文件路径不能为空");
            }

            // 创建文件路径
            Path path = Paths.get(filePath);

            // 创建父目录
            Files.createDirectories(path.getParent());

            // 写入文件内容
            Files.writeString(path, content != null ? content : "");

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "文件创建成功");
            result.put("filePath", filePath);
            result.put("fileSize", Files.size(path));

            return result;
        } catch (Exception e) {
            throw new RuntimeException("创建文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 读取文件工具
     */
    @Tool(name = "readFile", description = "读取文件内容，参数为filePath(文件路径)")
    public Map<String, Object> readFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                throw new IllegalArgumentException("文件路径不能为空");
            }

            // 读取文件内容
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new IOException("文件不存在: " + filePath);
            }

            String content = Files.readString(path);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("filePath", filePath);
            result.put("fileSize", Files.size(path));
            result.put("content", content);

            return result;
        } catch (Exception e) {
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        }
    }
}
