/**
 * 对话历史页面
 *
 * @author ego
 * @date 2025-11-29
 */

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { MessageSquare, Trash2, Loader2, Calendar } from 'lucide-react';
import { chatApi } from '../services/api';
import type { Conversation } from '../types';

export default function HistoryPage() {
    const queryClient = useQueryClient();

    // 获取对话列表
    const { data, isLoading } = useQuery({
        queryKey: ['conversations'],
        queryFn: () => chatApi.getConversations({ page: 1, size: 50 }),
    });

    // 删除对话
    const deleteMutation = useMutation({
        mutationFn: (id: string) => chatApi.deleteConversation(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['conversations'] });
        },
    });

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        const now = new Date();
        const diff = now.getTime() - date.getTime();
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));

        if (days === 0) {
            return '今天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        } else if (days === 1) {
            return '昨天 ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' });
        } else if (days < 7) {
            return `${days} 天前`;
        } else {
            return date.toLocaleDateString('zh-CN');
        }
    };

    const getTypeLabel = (type: string) => {
        const labels: Record<string, string> = {
            rag: 'RAG',
            agent: 'Agent',
            mcp: 'MCP',
        };
        return labels[type] || type;
    };

    const conversations = data?.data?.items || [];

    return (
        <div className="h-full overflow-y-auto p-6">
            <div className="max-w-4xl mx-auto">
                <div className="mb-8 animate-fade-in">
                    <h1 className="text-4xl font-bold bg-gradient-to-r from-primary-600 to-indigo-600 bg-clip-text text-transparent mb-2">
                        对话历史
                    </h1>
                    <p className="text-gray-600">查看和管理您的对话记录</p>
                </div>

                {isLoading ? (
                    <div className="bg-white/90 backdrop-blur-sm rounded-2xl border border-gray-200/50 shadow-xl p-12 text-center">
                        <div className="relative inline-block mb-4">
                            <div className="absolute inset-0 bg-gradient-to-br from-primary-400 to-indigo-500 rounded-full blur-xl opacity-30 animate-pulse-slow" />
                            <Loader2 className="relative h-10 w-10 animate-spin text-primary-600" />
                        </div>
                        <p className="text-gray-600 font-medium">加载中...</p>
                    </div>
                ) : conversations.length === 0 ? (
                    <div className="bg-white/90 backdrop-blur-sm rounded-2xl border border-gray-200/50 shadow-xl p-12 text-center">
                        <div className="relative inline-block mb-6 animate-float">
                            <div className="absolute inset-0 bg-gradient-to-br from-gray-300 to-gray-400 rounded-full blur-2xl opacity-30" />
                            <div className="relative p-6 bg-gradient-to-br from-gray-100 to-gray-200 rounded-2xl">
                                <MessageSquare className="h-16 w-16 text-gray-400" />
                            </div>
                        </div>
                        <p className="text-gray-600 font-medium text-lg mb-4">暂无对话记录</p>
                        <Link
                            to="/chat"
                            className="inline-block px-6 py-3 bg-gradient-to-r from-primary-500 to-indigo-600 text-white rounded-xl hover:from-primary-600 hover:to-indigo-700 transition-all duration-300 shadow-lg hover:shadow-xl hover:scale-105 font-semibold"
                        >
                            开始新对话
                        </Link>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {conversations.map((conversation: Conversation, index) => (
                            <div
                                key={conversation.id}
                                className="bg-white/90 backdrop-blur-sm rounded-2xl border border-gray-200/50 p-6 hover:shadow-xl hover:border-primary-200 transition-all duration-300 animate-slide-up group"
                                style={{ animationDelay: `${index * 0.05}s` }}
                            >
                                <div className="flex items-center justify-between">
                                    <Link
                                        to={`/chat/${conversation.id}`}
                                        className="flex-1 min-w-0 group-hover:text-primary-600 transition-colors"
                                    >
                                        <div className="flex items-center gap-4">
                                            <div className="relative flex-shrink-0">
                                                <div className="absolute inset-0 bg-gradient-to-br from-primary-200 to-indigo-200 rounded-xl blur-md opacity-50 group-hover:opacity-75 transition-opacity" />
                                                <div className="relative p-3 bg-gradient-to-br from-primary-500 to-indigo-600 rounded-xl shadow-lg">
                                                    <MessageSquare className="h-5 w-5 text-white" />
                                                </div>
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <h3 className="text-lg font-semibold text-gray-800 truncate group-hover:text-primary-600 transition-colors">
                                                    {conversation.title || '未命名对话'}
                                                </h3>
                                                {conversation.lastMessage && (
                                                    <p className="text-sm text-gray-500 truncate mt-2 line-clamp-2">
                                                        {conversation.lastMessage}
                                                    </p>
                                                )}
                                                <div className="flex items-center gap-3 mt-3 flex-wrap">
                                                    <span className="flex items-center gap-1.5 px-2.5 py-1 bg-gray-100 rounded-lg text-xs text-gray-600">
                                                        <Calendar className="h-3.5 w-3.5" />
                                                        {formatDate(conversation.updatedAt)}
                                                    </span>
                                                    <span className="px-2.5 py-1 bg-blue-100 text-blue-700 rounded-lg text-xs font-medium">
                                                        {conversation.messageCount} 条消息
                                                    </span>
                                                    <span className="px-2.5 py-1 bg-gradient-to-r from-primary-100 to-indigo-100 text-primary-700 rounded-lg text-xs font-semibold border border-primary-200">
                                                        {getTypeLabel(conversation.type)}
                                                    </span>
                                                </div>
                                            </div>
                                        </div>
                                    </Link>
                                    <button
                                        onClick={() => deleteMutation.mutate(conversation.id)}
                                        disabled={deleteMutation.isPending}
                                        className="ml-4 p-2.5 text-red-600 hover:bg-red-50 rounded-xl transition-all duration-300 hover:scale-110 disabled:opacity-50 disabled:hover:scale-100"
                                    >
                                        <Trash2 className="h-5 w-5" />
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

