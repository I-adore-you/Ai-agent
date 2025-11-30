# Java 开发环境配置指南

## 当前检测到的环境

- ✅ **Java 21** 已安装：`/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home`
- ✅ **Java 8** 已安装：`/Library/Java/JavaVirtualMachines/jdk1.8.0_211.jdk/Contents/Home`
- ⚠️ **Maven** 未在 PATH 中找到

## 1. 设置 JAVA_HOME 环境变量

### 方法一：在 ~/.zshrc 中设置（推荐）

```bash
# 编辑配置文件
nano ~/.zshrc
# 或
vim ~/.zshrc

# 添加以下内容（使用 Java 21）
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 保存后重新加载配置
source ~/.zshrc

# 验证
echo $JAVA_HOME
java -version
```

### 方法二：使用 /usr/libexec/java_home（macOS 推荐）

```bash
# 在 ~/.zshrc 中添加
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
export PATH=$JAVA_HOME/bin:$PATH

# 重新加载
source ~/.zshrc
```

### 方法三：临时设置（仅当前终端会话）

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```

## 2. 安装 Maven

### 方法一：使用 Homebrew（推荐）

```bash
# 安装 Homebrew（如果未安装）
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装 Maven
brew install maven

# 验证安装
mvn -version
```

### 方法二：手动安装

1. **下载 Maven**
   ```bash
   cd ~/Downloads
   curl -O https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
   ```

2. **解压并移动到 /usr/local**
   ```bash
   tar -xzf apache-maven-3.9.6-bin.tar.gz
   sudo mv apache-maven-3.9.6 /usr/local/maven
   ```

3. **配置环境变量**
   在 `~/.zshrc` 中添加：
   ```bash
   export M2_HOME=/usr/local/maven
   export PATH=$M2_HOME/bin:$PATH
   ```

4. **重新加载配置**
   ```bash
   source ~/.zshrc
   mvn -version
   ```

## 3. 验证配置

运行以下命令验证所有配置：

```bash
# 检查 Java
java -version
echo $JAVA_HOME

# 检查 Maven
mvn -version

# 检查 Java 编译器
javac -version
```

预期输出示例：
```
java version "21.0.4" 2024-07-16 LTS
Java(TM) SE Runtime Environment (build 21.0.4+8-LTS-274)
Java HotSpot(TM) 64-Bit Server VM (build 21.0.4+8-LTS-274, mixed mode, sharing)

/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home

Apache Maven 3.9.6
Maven home: /opt/homebrew/Cellar/maven/3.9.6/libexec
Java version: 21.0.4, vendor: Oracle Corporation
Java home: /Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
```

## 4. Cursor/VS Code 配置说明

配置文件位置：`~/Library/Application Support/Cursor/User/settings.json`

### 已配置的内容：

1. **Java 路径**：自动使用 `JAVA_HOME` 环境变量
2. **Java 运行时**：配置了 Java 21（默认）和 Java 8
3. **Maven 配置**：自动检测 Maven 可执行文件
4. **代码格式化**：使用 Google Java Style
5. **自动保存**：保存后自动格式化
6. **代码补全**：启用智能补全和导入
7. **测试框架**：配置 JUnit 5

### 如果 Maven 不在标准路径：

如果 Maven 安装在其他位置，可以在 `settings.json` 中修改：

```json
"maven.executable.path": "/usr/local/maven/bin/mvn"
// 或
"maven.executable.path": "/opt/homebrew/bin/mvn"
```

## 5. 推荐的 VS Code/Cursor 扩展

安装以下扩展以获得最佳 Java 开发体验：

1. **Extension Pack for Java** (Microsoft)
   - 包含多个 Java 开发必需扩展

2. **Maven for Java** (Microsoft)
   - Maven 项目支持

3. **Spring Boot Extension Pack** (VMware)
   - Spring Boot 开发支持

4. **SonarLint** (SonarSource)
   - 代码质量检查

5. **Checkstyle for Java** (Sheng Chen)
   - 代码风格检查

## 6. 常见问题

### Q: Maven 命令找不到

**解决方案**：
1. 确认 Maven 已安装：`brew list maven` 或检查安装目录
2. 确认 PATH 包含 Maven：`echo $PATH`
3. 在 Cursor 设置中指定完整路径

### Q: Java 版本不匹配

**解决方案**：
1. 检查当前 Java 版本：`java -version`
2. 切换 Java 版本：
   ```bash
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)  # 使用 Java 21
   export JAVA_HOME=$(/usr/libexec/java_home -v 1.8)  # 使用 Java 8
   ```

### Q: Maven 项目无法识别

**解决方案**：
1. 在 Cursor 中按 `Cmd+Shift+P`，运行 "Java: Clean Java Language Server Workspace"
2. 重新加载窗口：`Cmd+Shift+P` -> "Developer: Reload Window"
3. 检查 `.vscode/settings.json` 中的 Java 配置

## 7. 测试配置

创建一个简单的 Maven 项目测试：

```bash
# 使用 Maven 创建项目
mvn archetype:generate \
  -DgroupId=com.example \
  -DartifactId=test-project \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

cd test-project

# 编译
mvn compile

# 运行测试
mvn test

# 打包
mvn package
```

如果以上命令都能成功执行，说明环境配置正确！

## 8. 下一步

配置完成后，您可以：

1. 在 Cursor 中打开 Java 项目
2. 使用 `Cmd+Shift+P` 运行 Java 命令
3. 使用 Maven 面板管理依赖和构建
4. 享受自动补全、代码检查和格式化功能

