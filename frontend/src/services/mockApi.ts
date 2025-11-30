/**
 * Mock API 服务 - 用于前端独立开发和展示
 *
 * @author ego
 * @date 2025-11-29
 */

import type { ApiResponse, ChatRequest, ChatResponse, Conversation, ConversationDetail, Document, PageResponse } from '../types';

// 模拟延迟
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// 模拟数据
let mockConversations: Conversation[] = [
    {
        id: 'conv-1',
        title: 'Spring AI 相关问题',
        type: 'rag',
        lastMessage: 'Spring AI 是一个用于集成 AI 功能的框架',
        messageCount: 5,
        createdAt: new Date(Date.now() - 86400000).toISOString(),
        updatedAt: new Date(Date.now() - 3600000).toISOString(),
    },
    {
        id: 'conv-2',
        title: 'MyBatis 使用指南',
        type: 'rag',
        lastMessage: 'MyBatis 是一个优秀的持久层框架',
        messageCount: 3,
        createdAt: new Date(Date.now() - 172800000).toISOString(),
        updatedAt: new Date(Date.now() - 7200000).toISOString(),
    },
];

let mockDocuments: Document[] = [
    {
        id: 'doc-1',
        title: 'Spring AI 官方文档',
        fileName: 'spring-ai-docs.pdf',
        fileType: 'application/pdf',
        fileSize: 2048000,
        status: 'completed',
        chunkCount: 45,
        createdAt: new Date(Date.now() - 259200000).toISOString(),
        updatedAt: new Date(Date.now() - 259200000).toISOString(),
    },
    {
        id: 'doc-2',
        title: 'MyBatis 最佳实践',
        fileName: 'mybatis-guide.docx',
        fileType: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        fileSize: 1536000,
        status: 'completed',
        chunkCount: 32,
        createdAt: new Date(Date.now() - 172800000).toISOString(),
        updatedAt: new Date(Date.now() - 172800000).toISOString(),
    },
    {
        id: 'doc-3',
        title: '项目架构设计',
        fileName: 'architecture.md',
        fileType: 'text/markdown',
        fileSize: 512000,
        status: 'processing',
        createdAt: new Date(Date.now() - 3600000).toISOString(),
        updatedAt: new Date(Date.now() - 1800000).toISOString(),
    },
];

/**
 * Mock 对话 API
 */
export const mockChatApi = {
    sendMessage: async (request: ChatRequest): Promise<ApiResponse<ChatResponse>> => {
        await delay(1000 + Math.random() * 1000); // 模拟网络延迟

        const conversationId = request.conversationId || `conv-${Date.now()}`;
        const mockResponses = [
            '这是一个很好的问题！根据您提供的上下文，我可以为您详细解答。',
            '基于检索到的文档，我了解到相关信息如下：\n\n1. 首先，...\n2. 其次，...\n3. 最后，...',
            '根据文档分析，这个问题的答案涉及多个方面。让我为您逐一说明。',
        ];

        const response: ApiResponse<ChatResponse> = {
            success: true,
            data: {
                message: mockResponses[Math.floor(Math.random() * mockResponses.length)],
                conversationId,
                messageId: `msg-${Date.now()}`,
                sources: request.useRAG ? [
                    {
                        documentId: 'doc-1',
                        documentTitle: 'Spring AI 官方文档',
                        chunkIndex: 0,
                        similarity: 0.92,
                    },
                    {
                        documentId: 'doc-2',
                        documentTitle: 'MyBatis 最佳实践',
                        chunkIndex: 5,
                        similarity: 0.85,
                    },
                ] : undefined,
                metadata: {
                    model: 'deepseek-coder',
                    tokens: 150 + Math.floor(Math.random() * 100),
                    responseTime: 1.2 + Math.random() * 0.5,
                },
            },
            timestamp: new Date().toISOString(),
        };

        return response;
    },

    getConversations: async (params?: { page?: number; size?: number; type?: string }): Promise<ApiResponse<PageResponse<Conversation>>> => {
        await delay(500);

        let filtered = [...mockConversations];
        if (params?.type) {
            filtered = filtered.filter(c => c.type === params.type);
        }

        const page = params?.page || 1;
        const size = params?.size || 20;
        const start = (page - 1) * size;
        const end = start + size;

        return {
            success: true,
            data: {
                items: filtered.slice(start, end),
                total: filtered.length,
                page,
                size,
            },
            timestamp: new Date().toISOString(),
        };
    },

    getConversation: async (id: string): Promise<ApiResponse<ConversationDetail>> => {
        await delay(500);

        const conv = mockConversations.find(c => c.id === id);
        if (!conv) {
            return {
                success: false,
                error: {
                    code: 'NOT_FOUND',
                    message: '对话不存在',
                },
                timestamp: new Date().toISOString(),
            };
        }

        const messages = [
            {
                id: 'msg-1',
                role: 'user' as const,
                content: '什么是 Spring AI？',
                createdAt: new Date(Date.now() - 3600000).toISOString(),
            },
            {
                id: 'msg-2',
                role: 'assistant' as const,
                content: 'Spring AI 是一个用于集成 AI 功能的框架，它提供了统一的 API 来访问各种 AI 模型。',
                sources: [
                    {
                        documentId: 'doc-1',
                        documentTitle: 'Spring AI 官方文档',
                        chunkIndex: 0,
                        similarity: 0.95,
                    },
                ],
                createdAt: new Date(Date.now() - 3600000).toISOString(),
            },
        ];

        return {
            success: true,
            data: {
                ...conv,
                messages,
            },
            timestamp: new Date().toISOString(),
        };
    },

    deleteConversation: async (id: string): Promise<ApiResponse> => {
        await delay(300);
        mockConversations = mockConversations.filter(c => c.id !== id);
        return {
            success: true,
            message: '对话已删除',
            timestamp: new Date().toISOString(),
        };
    },
};

/**
 * Mock 文档 API
 */
export const mockDocumentApi = {
    uploadDocument: async (file: File, title?: string): Promise<ApiResponse<Document>> => {
        await delay(1500);

        const newDoc: Document = {
            id: `doc-${Date.now()}`,
            title: title || file.name.replace(/\.[^/.]+$/, ''),
            fileName: file.name,
            fileType: file.type,
            fileSize: file.size,
            status: 'processing',
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
        };

        mockDocuments.push(newDoc);

        // 模拟处理完成
        setTimeout(() => {
            const doc = mockDocuments.find(d => d.id === newDoc.id);
            if (doc) {
                doc.status = 'completed';
                doc.chunkCount = Math.floor(Math.random() * 50) + 10;
                doc.updatedAt = new Date().toISOString();
            }
        }, 3000);

        return {
            success: true,
            data: newDoc,
            timestamp: new Date().toISOString(),
        };
    },

    getDocuments: async (params?: { page?: number; size?: number; status?: string; search?: string }): Promise<ApiResponse<PageResponse<Document>>> => {
        await delay(500);

        let filtered = [...mockDocuments];
        if (params?.status) {
            filtered = filtered.filter(d => d.status === params.status);
        }
        if (params?.search) {
            const search = params.search.toLowerCase();
            filtered = filtered.filter(d => 
                d.title.toLowerCase().includes(search) || 
                d.fileName.toLowerCase().includes(search)
            );
        }

        const page = params?.page || 1;
        const size = params?.size || 20;
        const start = (page - 1) * size;
        const end = start + size;

        return {
            success: true,
            data: {
                items: filtered.slice(start, end),
                total: filtered.length,
                page,
                size,
            },
            timestamp: new Date().toISOString(),
        };
    },

    getDocument: async (id: string): Promise<ApiResponse<Document>> => {
        await delay(300);
        const doc = mockDocuments.find(d => d.id === id);
        if (!doc) {
            return {
                success: false,
                error: {
                    code: 'NOT_FOUND',
                    message: '文档不存在',
                },
                timestamp: new Date().toISOString(),
            };
        }
        return {
            success: true,
            data: doc,
            timestamp: new Date().toISOString(),
        };
    },

    deleteDocument: async (id: string): Promise<ApiResponse> => {
        await delay(300);
        mockDocuments = mockDocuments.filter(d => d.id !== id);
        return {
            success: true,
            message: '文档已删除',
            timestamp: new Date().toISOString(),
        };
    },

    getDocumentStatus: async (id: string): Promise<ApiResponse<{ status: string; progress: number; message: string }>> => {
        await delay(300);
        const doc = mockDocuments.find(d => d.id === id);
        return {
            success: true,
            data: {
                status: doc?.status || 'processing',
                progress: doc?.status === 'completed' ? 100 : doc?.status === 'failed' ? 0 : 75,
                message: doc?.status === 'completed' ? '处理完成' : doc?.status === 'failed' ? '处理失败' : '正在处理中...',
            },
            timestamp: new Date().toISOString(),
        };
    },
};

