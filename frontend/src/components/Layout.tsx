/**
 * 布局组件
 *
 * @author ego
 * @date 2025-11-29
 */

import { ReactNode } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { MessageSquare, FileText, History, Sparkles } from 'lucide-react';

interface LayoutProps {
    children: ReactNode;
}

export default function Layout({ children }: LayoutProps) {
    const location = useLocation();

    const navItems = [
        { path: '/chat', icon: MessageSquare, label: '对话' },
        { path: '/documents', icon: FileText, label: '文档' },
        { path: '/history', icon: History, label: '历史' },
    ];

    return (
        <div className="flex h-screen bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50">
            {/* 侧边栏 */}
            <aside className="w-72 bg-white/80 backdrop-blur-xl border-r border-white/20 shadow-2xl flex flex-col relative overflow-hidden">
                {/* 背景装饰 */}
                <div className="absolute inset-0 bg-gradient-to-br from-primary-50/50 via-transparent to-indigo-50/30 pointer-events-none" />
                <div className="absolute top-0 right-0 w-64 h-64 bg-gradient-to-br from-primary-200/20 to-transparent rounded-full blur-3xl" />
                
                <div className="relative z-10">
                    {/* Logo 区域 */}
                    <div className="p-6 border-b border-gray-200/50 bg-gradient-to-r from-primary-500 to-indigo-600 bg-clip-text">
                        <div className="flex items-center gap-3 mb-2">
                            <div className="p-2 bg-gradient-to-br from-primary-500 to-indigo-600 rounded-xl shadow-lg">
                                <Sparkles className="h-6 w-6 text-white" />
                            </div>
                            <h1 className="text-3xl font-bold bg-gradient-to-r from-primary-600 to-indigo-600 bg-clip-text text-transparent">
                                AI Agent
                            </h1>
                        </div>
                        <p className="text-sm text-gray-600 ml-14 font-medium">智能对话助手</p>
                    </div>

                    {/* 导航菜单 */}
                    <nav className="flex-1 p-4">
                        <ul className="space-y-2">
                            {navItems.map((item, index) => {
                                const Icon = item.icon;
                                const isActive = location.pathname.startsWith(item.path);
                                return (
                                    <li key={item.path} className="animate-slide-in-right" style={{ animationDelay: `${index * 0.1}s` }}>
                                        <Link
                                            to={item.path}
                                            className={`group flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-300 relative overflow-hidden ${
                                                isActive
                                                    ? 'bg-gradient-to-r from-primary-500 to-indigo-600 text-white shadow-lg shadow-primary-500/50 scale-105'
                                                    : 'text-gray-700 hover:bg-gradient-to-r hover:from-primary-50 hover:to-indigo-50 hover:shadow-md hover:scale-102'
                                            }`}
                                        >
                                            {isActive && (
                                                <div className="absolute inset-0 bg-white/10 backdrop-blur-sm" />
                                            )}
                                            <Icon 
                                                size={22} 
                                                className={`relative z-10 transition-transform duration-300 ${
                                                    isActive ? 'scale-110' : 'group-hover:scale-110'
                                                }`}
                                            />
                                            <span className={`relative z-10 font-medium ${isActive ? 'text-white' : ''}`}>
                                                {item.label}
                                            </span>
                                            {isActive && (
                                                <div className="absolute right-2 w-2 h-2 bg-white rounded-full animate-pulse" />
                                            )}
                                        </Link>
                                    </li>
                                );
                            })}
                        </ul>
                    </nav>

                    {/* 底部信息 */}
                    <div className="p-4 border-t border-gray-200/50 bg-gradient-to-t from-white/50 to-transparent">
                        <div className="text-xs text-gray-600 space-y-1">
                            <p className="font-semibold">版本 1.0.0</p>
                            <p className="text-gray-500">基于 Spring AI</p>
                        </div>
                    </div>
                </div>
            </aside>

            {/* 主内容区 */}
            <main className="flex-1 overflow-hidden relative">
                {/* 背景装饰 */}
                <div className="absolute inset-0 bg-gradient-to-br from-transparent via-primary-50/30 to-indigo-50/20 pointer-events-none" />
                <div className="absolute top-0 right-0 w-96 h-96 bg-gradient-to-br from-primary-200/20 to-indigo-200/20 rounded-full blur-3xl" />
                <div className="absolute bottom-0 left-0 w-96 h-96 bg-gradient-to-tr from-indigo-200/20 to-primary-200/20 rounded-full blur-3xl" />
                
                <div className="relative z-10 h-full">
                    {children}
                </div>
            </main>
        </div>
    );
}

