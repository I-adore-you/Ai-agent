# AI Agent Frontend

基于 React + TypeScript + Vite + Tailwind CSS 的前端应用。

## 技术栈

- **React 18**
- **TypeScript**
- **Vite 5**
- **Tailwind CSS 3**
- **React Router 6**
- **TanStack Query (React Query)**
- **Axios**
- **Lucide React** (图标库)

## 项目结构

```
frontend/
├── src/
│   ├── components/          # 组件
│   │   └── Layout.tsx      # 布局组件
│   ├── pages/              # 页面
│   │   ├── ChatPage.tsx   # 对话页面
│   │   ├── DocumentsPage.tsx  # 文档管理页面
│   │   └── HistoryPage.tsx    # 历史记录页面
│   ├── services/           # API 服务
│   │   └── api.ts         # API 客户端
│   ├── types/             # TypeScript 类型定义
│   │   └── index.ts
│   ├── hooks/             # 自定义 Hooks
│   ├── utils/             # 工具函数
│   ├── App.tsx            # 主应用组件
│   ├── main.tsx           # 入口文件
│   └── index.css          # 全局样式
├── public/                # 静态资源
├── package.json
├── vite.config.ts
├── tsconfig.json
└── tailwind.config.js
```

## 快速开始

### 1. 安装依赖

```bash
cd frontend
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

前端将在 `http://localhost:3000` 运行。

### 3. 构建生产版本

```bash
npm run build
```

构建产物在 `dist/` 目录。

## 功能页面

### 1. 对话页面 (`/chat`)
- 实时对话界面
- 支持 RAG 模式切换
- 显示消息来源引用
- 流式响应（待实现）

### 2. 文档管理页面 (`/documents`)
- 文档上传（支持 PDF、Word、TXT、Markdown）
- 文档列表展示
- 文档状态查看
- 文档删除

### 3. 历史记录页面 (`/history`)
- 对话历史列表
- 快速跳转到历史对话
- 对话删除

## API 配置

API 基础 URL 通过 Vite 代理配置：
- 开发环境：`/api` -> `http://localhost:8080/api`
- 生产环境：需要配置环境变量或构建时设置

## 环境变量

创建 `.env` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

