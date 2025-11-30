/**
 * API 服务
 *
 * @author ego
 * @date 2025-11-29
 */

import axios from 'axios';
import type { ApiResponse, ChatRequest, ChatResponse, Conversation, ConversationDetail, Document, PageResponse } from '../types';

// 使用 Mock API（前端独立开发模式）
const USE_MOCK_API = false; // 设置为 false 时使用真实 API

// Mock API 导入
import { mockChatApi, mockDocumentApi } from './mockApi';

const api = axios.create({
    baseURL: '/api',
    timeout: 60000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// 请求拦截器
api.interceptors.request.use(
    (config) => {
        // 可以在这里添加 token 等
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// 响应拦截器
api.interceptors.response.use(
    (response) => {
        return response.data;
    },
    (error) => {
        console.error('API Error:', error);
        return Promise.reject(error);
    }
);

/**
 * 对话 API
 */
export const chatApi = {
    /**
     * 发送消息
     */
    sendMessage: (request: ChatRequest): Promise<ApiResponse<ChatResponse>> => {
        if (USE_MOCK_API) {
            return mockChatApi.sendMessage(request);
        }
        return api.post('/chat', request);
    },

    /**
     * 获取对话列表
     */
    getConversations: (params?: { page?: number; size?: number; type?: string }): Promise<ApiResponse<PageResponse<Conversation>>> => {
        if (USE_MOCK_API) {
            return mockChatApi.getConversations(params);
        }
        return api.get('/chat/conversations', { params });
    },

    /**
     * 获取对话详情
     */
    getConversation: (id: string): Promise<ApiResponse<ConversationDetail>> => {
        if (USE_MOCK_API) {
            return mockChatApi.getConversation(id);
        }
        return api.get(`/chat/conversations/${id}`);
    },

    /**
     * 删除对话
     */
    deleteConversation: (id: string): Promise<ApiResponse> => {
        if (USE_MOCK_API) {
            return mockChatApi.deleteConversation(id);
        }
        return api.delete(`/chat/conversations/${id}`);
    },
};

/**
 * 文档 API
 */
export const documentApi = {
    /**
     * 上传文档
     */
    uploadDocument: (file: File, title?: string): Promise<ApiResponse<Document>> => {
        if (USE_MOCK_API) {
            return mockDocumentApi.uploadDocument(file, title);
        }
        const formData = new FormData();
        formData.append('file', file);
        if (title) {
            formData.append('title', title);
        }
        return api.post('/documents/upload', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    },

    /**
     * 获取文档列表
     */
    getDocuments: (params?: { page?: number; size?: number; status?: string; search?: string }): Promise<ApiResponse<PageResponse<Document>>> => {
        if (USE_MOCK_API) {
            return mockDocumentApi.getDocuments(params);
        }
        return api.get('/documents', { params });
    },

    /**
     * 获取文档详情
     */
    getDocument: (id: string): Promise<ApiResponse<Document>> => {
        if (USE_MOCK_API) {
            return mockDocumentApi.getDocument(id);
        }
        return api.get(`/documents/${id}`);
    },

    /**
     * 删除文档
     */
    deleteDocument: (id: string): Promise<ApiResponse> => {
        if (USE_MOCK_API) {
            return mockDocumentApi.deleteDocument(id);
        }
        return api.delete(`/documents/${id}`);
    },

    /**
     * 获取文档处理状态
     */
    getDocumentStatus: (id: string): Promise<ApiResponse<{ status: string; progress: number; message: string }>> => {
        if (USE_MOCK_API) {
            return mockDocumentApi.getDocumentStatus(id);
        }
        return api.get(`/documents/${id}/status`);
    },
};

export default api;

