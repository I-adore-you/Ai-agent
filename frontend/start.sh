#!/bin/bash

# 前端启动脚本

echo "检查 Node.js 环境..."

if ! command -v node &> /dev/null; then
    echo "❌ Node.js 未安装"
    echo ""
    echo "请先安装 Node.js："
    echo "  方式 1: brew install node"
    echo "  方式 2: 访问 https://nodejs.org/ 下载安装"
    exit 1
fi

echo "✅ Node.js 版本: $(node --version)"
echo "✅ npm 版本: $(npm --version)"
echo ""

cd "$(dirname "$0")"

# 检查 node_modules
if [ ! -d "node_modules" ]; then
    echo "📦 安装依赖..."
    npm install
fi

echo "🚀 启动开发服务器..."
echo "前端地址: http://localhost:3000"
echo "API 代理: http://localhost:8080/api"
echo ""
echo "按 Ctrl+C 停止服务器"
echo ""

npm run dev

