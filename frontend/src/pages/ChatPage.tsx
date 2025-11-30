/**
 * 对话页面
 *
 * @author ego
 * @date 2025-11-29
 */

import { useState, useRef, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useMutation, useQuery } from '@tanstack/react-query';
import { Send, Loader2, Bot, User } from 'lucide-react';
import { chatApi } from '../services/api';
import type { ChatMessage, ChatRequest } from '../types';

export default function ChatPage() {
    const { conversationId } = useParams();
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [input, setInput] = useState('');
    const [useRAG, setUseRAG] = useState(true);
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const textareaRef = useRef<HTMLTextAreaElement>(null);

    // 获取对话历史
    const { data: conversationResponse } = useQuery({
        queryKey: ['conversation', conversationId],
        queryFn: () => chatApi.getConversation(conversationId!),
        enabled: !!conversationId,
    });

    // 处理对话历史数据
    useEffect(() => {
        if (conversationResponse?.success && conversationResponse.data) {
            setMessages(conversationResponse.data.messages);
        }
    }, [conversationResponse]);

    // 发送消息
    const sendMutation = useMutation({
        mutationFn: (request: ChatRequest) => chatApi.sendMessage(request),
        onSuccess: (response) => {
            if (response.success && response.data) {
                const userMessage: ChatMessage = {
                    id: Date.now().toString(),
                    role: 'user',
                    content: input,
                    createdAt: new Date().toISOString(),
                };
                const aiMessage: ChatMessage = {
                    id: response.data.messageId,
                    role: 'assistant',
                    content: response.data.message,
                    sources: response.data.sources,
                    createdAt: new Date().toISOString(),
                };
                setMessages((prev) => [...prev, userMessage, aiMessage]);
                setInput('');
            }
        },
    });

    const handleSend = () => {
        if (!input.trim() || sendMutation.isPending) return;

        const request: ChatRequest = {
            message: input.trim(),
            conversationId: conversationId,
            useRAG,
            useAgent: false,
        };

        sendMutation.mutate(request);
    };

    const handleKeyPress = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    };

    // 自动滚动到底部
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    // 自动调整输入框高度
    useEffect(() => {
        if (textareaRef.current) {
            textareaRef.current.style.height = 'auto';
            textareaRef.current.style.height = `${textareaRef.current.scrollHeight}px`;
        }
    }, [input]);

    return (
        <div className="flex flex-col h-full relative">
            {/* 消息区域 */}
            <div className="flex-1 overflow-y-auto p-6 space-y-6 scroll-smooth">
                {messages.length === 0 ? (
                    <div className="flex items-center justify-center h-full relative overflow-hidden">
                        {/* 背景图片 */}
                        <div 
                            className="absolute inset-0 pointer-events-none z-0"
                            style={{
                                backgroundImage: 'url(/images/chat-bg.jpg)',
                                backgroundSize: 'cover',
                                backgroundPosition: 'center',
                                backgroundRepeat: 'no-repeat',
                                opacity: 0.6,
                                filter: 'blur(0.5px) brightness(1.2) saturate(1.15)'
                            }}
                        >
                            <div className="absolute inset-0 bg-gradient-to-b from-white/40 via-white/25 to-white/50" />
                        </div>
                        
                        <div className="text-center animate-fade-in relative z-10 px-6">
                            <div className="relative inline-block mb-6 animate-float">
                                <div className="absolute inset-0 bg-gradient-to-br from-primary-400 to-indigo-500 rounded-full blur-2xl opacity-50 animate-pulse-slow" />
                                <div className="relative p-6 bg-gradient-to-br from-primary-500 to-indigo-600 rounded-full shadow-2xl shadow-primary-500/50">
                                    <Bot className="h-16 w-16 text-white" />
                                </div>
                            </div>
                            <h2 className="text-3xl font-bold bg-gradient-to-r from-primary-600 to-indigo-600 bg-clip-text text-transparent mb-3">
                                开始对话
                            </h2>
                            <p className="text-gray-600 text-lg font-medium">输入您的问题，AI 助手将为您解答</p>
                            <div className="mt-6 flex items-center justify-center gap-2 text-sm text-gray-500">
                                <div className="w-2 h-2 bg-primary-500 rounded-full animate-pulse" />
                                <span>智能助手已就绪</span>
                            </div>
                        </div>
                    </div>
                ) : (
                    messages.map((message, index) => (
                        <div
                            key={message.id}
                            className={`flex gap-4 animate-slide-up ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
                            style={{ animationDelay: `${index * 0.1}s` }}
                        >
                            {message.role === 'assistant' && (
                                <div className="flex-shrink-0 relative">
                                    <div className="absolute inset-0 bg-gradient-to-br from-primary-400 to-indigo-500 rounded-full blur-lg opacity-50 animate-pulse-slow" />
                                    <div className="relative w-10 h-10 rounded-full bg-gradient-to-br from-primary-500 to-indigo-600 flex items-center justify-center shadow-lg">
                                        <Bot className="h-5 w-5 text-white" />
                                    </div>
                                </div>
                            )}
                            <div
                                className={`max-w-3xl rounded-2xl px-5 py-4 shadow-lg transition-all duration-300 hover:shadow-xl ${message.role === 'user'
                                    ? 'bg-gradient-to-br from-primary-500 to-indigo-600 text-white shadow-primary-500/30'
                                    : 'bg-white/90 backdrop-blur-sm text-gray-800 border border-gray-200/50'
                                    }`}
                            >
                                <div className="whitespace-pre-wrap leading-relaxed">{message.content}</div>
                                {message.sources && message.sources.length > 0 && (
                                    <div className="mt-3 pt-3 border-t border-gray-300/50">
                                        <p className="text-xs font-semibold mb-2 text-gray-600 uppercase tracking-wide">参考来源</p>
                                        <ul className="space-y-2">
                                            {message.sources.map((source, idx) => (
                                                <li
                                                    key={idx}
                                                    className="flex items-center gap-2 text-xs bg-gradient-to-r from-primary-50 to-indigo-50 px-3 py-2 rounded-lg border border-primary-100"
                                                >
                                                    <div className="w-1.5 h-1.5 bg-primary-500 rounded-full" />
                                                    <span className="font-medium text-gray-700">{source.documentTitle}</span>
                                                    <span className="ml-auto px-2 py-0.5 bg-primary-100 text-primary-700 rounded-full font-semibold">
                                                        {(source.similarity * 100).toFixed(1)}%
                                                    </span>
                                                </li>
                                            ))}
                                        </ul>
                                    </div>
                                )}
                            </div>
                            {message.role === 'user' && (
                                <div className="flex-shrink-0 relative">
                                    <div className="absolute inset-0 bg-gradient-to-br from-gray-300 to-gray-400 rounded-full blur-lg opacity-30" />
                                    <div className="relative w-10 h-10 rounded-full bg-gradient-to-br from-gray-400 to-gray-500 flex items-center justify-center shadow-lg">
                                        <User className="h-5 w-5 text-white" />
                                    </div>
                                </div>
                            )}
                        </div>
                    ))
                )}
                {sendMutation.isPending && (
                    <div className="flex gap-4 justify-start animate-slide-up">
                        <div className="flex-shrink-0 relative">
                            <div className="absolute inset-0 bg-gradient-to-br from-primary-400 to-indigo-500 rounded-full blur-lg opacity-50 animate-pulse-slow" />
                            <div className="relative w-10 h-10 rounded-full bg-gradient-to-br from-primary-500 to-indigo-600 flex items-center justify-center shadow-lg">
                                <Bot className="h-5 w-5 text-white" />
                            </div>
                        </div>
                        <div className="bg-white/90 backdrop-blur-sm rounded-2xl px-5 py-4 shadow-lg border border-gray-200/50">
                            <div className="flex items-center gap-3">
                                <Loader2 className="h-5 w-5 animate-spin text-primary-600" />
                                <span className="text-gray-600 text-sm">AI 正在思考...</span>
                            </div>
                        </div>
                    </div>
                )}
                <div ref={messagesEndRef} />
            </div>

            {/* 输入区域 */}
            <div className="border-t border-gray-200/50 p-6 bg-white/80 backdrop-blur-xl shadow-2xl relative">
                {/* 背景装饰 */}
                <div className="absolute inset-0 bg-gradient-to-t from-white via-primary-50/30 to-transparent pointer-events-none" />

                <div className="relative z-10 max-w-5xl mx-auto">
                    <div className="flex items-center gap-3 mb-4">
                        <label className="flex items-center gap-2 text-sm font-medium text-gray-700 cursor-pointer group">
                            <div className="relative">
                                <input
                                    type="checkbox"
                                    checked={useRAG}
                                    onChange={(e) => setUseRAG(e.target.checked)}
                                    className="sr-only"
                                />
                                <div className={`w-11 h-6 rounded-full transition-all duration-300 ${useRAG
                                    ? 'bg-gradient-to-r from-primary-500 to-indigo-600'
                                    : 'bg-gray-300'
                                    }`}>
                                    <div className={`w-5 h-5 bg-white rounded-full shadow-md transform transition-transform duration-300 mt-0.5 ${useRAG ? 'translate-x-5' : 'translate-x-0.5'
                                        }`} />
                                </div>
                            </div>
                            <span className="group-hover:text-primary-600 transition-colors">
                                启用 RAG（检索增强生成）
                            </span>
                            {useRAG && (
                                <span className="px-2 py-0.5 bg-primary-100 text-primary-700 rounded-full text-xs font-semibold animate-fade-in">
                                    ACTIVE
                                </span>
                            )}
                        </label>
                    </div>
                    <div className="flex gap-3">
                        <div className="flex-1 relative">
                            <textarea
                                ref={textareaRef}
                                value={input}
                                onChange={(e) => setInput(e.target.value)}
                                onKeyPress={handleKeyPress}
                                placeholder="输入您的问题..."
                                className="w-full resize-none rounded-2xl border-2 border-gray-200 px-5 py-4 pr-14 focus:outline-none focus:ring-4 focus:ring-primary-500/20 focus:border-primary-500 transition-all duration-300 bg-white/90 backdrop-blur-sm shadow-lg"
                                rows={1}
                            />
                            <div className="absolute right-4 top-1/2 -translate-y-1/2 text-xs text-gray-400">
                                Enter 发送，Shift+Enter 换行
                            </div>
                        </div>
                        <button
                            onClick={handleSend}
                            disabled={!input.trim() || sendMutation.isPending}
                            className="px-8 py-4 bg-gradient-to-r from-primary-500 to-indigo-600 text-white rounded-2xl hover:from-primary-600 hover:to-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2 transition-all duration-300 shadow-lg hover:shadow-xl hover:scale-105 disabled:hover:scale-100 font-semibold"
                        >
                            {sendMutation.isPending ? (
                                <>
                                    <Loader2 className="h-5 w-5 animate-spin" />
                                    <span>发送中...</span>
                                </>
                            ) : (
                                <>
                                    <Send className="h-5 w-5" />
                                    <span>发送</span>
                                </>
                            )}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
}

