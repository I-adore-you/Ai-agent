/**
 * API 响应类型
 *
 * @author ego
 * @date 2025-11-29
 */

export interface ApiResponse<T = any> {
    success: boolean;
    data?: T;
    message?: string;
    error?: {
        code: string;
        message: string;
        details?: any;
    };
    timestamp: string;
}

/**
 * 对话消息
 */
export interface ChatMessage {
    id: string;
    role: 'user' | 'assistant' | 'system';
    content: string;
    sources?: MessageSource[];
    createdAt: string;
}

/**
 * 消息来源
 */
export interface MessageSource {
    documentId: string;
    documentTitle: string;
    chunkIndex: number;
    similarity: number;
}

/**
 * 对话请求
 */
export interface ChatRequest {
    message: string;
    conversationId?: string;
    useRAG?: boolean;
    useAgent?: boolean;
    temperature?: number;
    maxTokens?: number;
}

/**
 * 对话响应
 */
export interface ChatResponse {
    message: string;
    conversationId: string;
    messageId: string;
    sources?: MessageSource[];
    metadata?: {
        model: string;
        tokens: number;
        responseTime: number;
    };
}

/**
 * 对话
 */
export interface Conversation {
    id: string;
    title: string;
    type: 'rag' | 'agent' | 'mcp';
    lastMessage?: string;
    messageCount: number;
    createdAt: string;
    updatedAt: string;
}

/**
 * 对话详情
 */
export interface ConversationDetail extends Conversation {
    messages: ChatMessage[];
}

/**
 * 文档
 */
export interface Document {
    id: string;
    title: string;
    fileName: string;
    fileType: string;
    fileSize: number;
    status: 'processing' | 'completed' | 'failed';
    chunkCount?: number;
    createdAt: string;
    updatedAt: string;
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
    items: T[];
    total: number;
    page: number;
    size: number;
}

