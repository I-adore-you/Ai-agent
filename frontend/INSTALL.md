# 前端安装和启动指南

## 前置要求

需要安装 **Node.js 18+** 和 **npm**。

## 安装 Node.js

### macOS 方式 1：使用 Homebrew（推荐）

```bash
# 安装 Homebrew（如果未安装）
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# 安装 Node.js
brew install node

# 验证安装
node --version
npm --version
```

### macOS 方式 2：从官网下载

1. 访问 https://nodejs.org/
2. 下载 LTS 版本（推荐）
3. 运行安装包
4. 验证安装：`node --version`

## 启动前端

### 方式 1：使用启动脚本（推荐）

```bash
cd frontend
./start.sh
```

### 方式 2：手动启动

```bash
cd frontend

# 安装依赖（首次运行）
npm install

# 启动开发服务器
npm run dev
```

## 访问应用

启动成功后，访问：
- **前端地址**: http://localhost:3000
- **API 代理**: 已配置为 http://localhost:8080/api

## 常见问题

### Q: npm install 很慢怎么办？

**解决方案**：使用国内镜像

```bash
npm config set registry https://registry.npmmirror.com
npm install
```

### Q: 端口 3000 被占用？

**解决方案**：修改 `vite.config.ts` 中的端口号

```typescript
server: {
  port: 3001,  // 改为其他端口
}
```

### Q: API 请求失败？

**解决方案**：
1. 确保后端服务已启动（http://localhost:8080）
2. 检查 `vite.config.ts` 中的代理配置
3. 查看浏览器控制台的错误信息

