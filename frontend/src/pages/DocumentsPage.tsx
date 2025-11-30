/**
 * 文档管理页面
 *
 * @author ego
 * @date 2025-11-29
 */

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Upload, FileText, Trash2, Loader2, CheckCircle, XCircle, Clock } from 'lucide-react';
import { documentApi } from '../services/api';
import type { Document } from '../types';

export default function DocumentsPage() {
    const [selectedFile, setSelectedFile] = useState<File | null>(null);
    const [title, setTitle] = useState('');
    const queryClient = useQueryClient();

    // 获取文档列表
    const { data, isLoading } = useQuery({
        queryKey: ['documents'],
        queryFn: () => documentApi.getDocuments({ page: 1, size: 50 }),
    });

    // 上传文档
    const uploadMutation = useMutation({
        mutationFn: (file: File) => documentApi.uploadDocument(file, title || undefined),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['documents'] });
            setSelectedFile(null);
            setTitle('');
        },
    });

    // 删除文档
    const deleteMutation = useMutation({
        mutationFn: (id: string) => documentApi.deleteDocument(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['documents'] });
        },
    });

    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setSelectedFile(file);
            if (!title) {
                setTitle(file.name.replace(/\.[^/.]+$/, ''));
            }
        }
    };

    const handleUpload = () => {
        if (selectedFile) {
            uploadMutation.mutate(selectedFile);
        }
    };

    const getStatusIcon = (status: string) => {
        switch (status) {
            case 'completed':
                return <CheckCircle className="h-5 w-5 text-green-500" />;
            case 'processing':
                return <Loader2 className="h-5 w-5 text-blue-500 animate-spin" />;
            case 'failed':
                return <XCircle className="h-5 w-5 text-red-500" />;
            default:
                return <Clock className="h-5 w-5 text-gray-400" />;
        }
    };

    const formatFileSize = (bytes: number) => {
        if (bytes === 0) return '0 Bytes';
        const k = 1024;
        const sizes = ['Bytes', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
    };

    const documents = data?.data?.items || [];

    return (
        <div className="h-full overflow-y-auto p-6">
            <div className="max-w-6xl mx-auto">
                <div className="mb-8 animate-fade-in">
                    <h1 className="text-4xl font-bold bg-gradient-to-r from-primary-600 to-indigo-600 bg-clip-text text-transparent mb-2">
                        文档管理
                    </h1>
                    <p className="text-gray-600">上传和管理您的知识库文档</p>
                </div>

                {/* 上传区域 */}
                <div className="bg-white/90 backdrop-blur-sm rounded-2xl border border-gray-200/50 shadow-xl p-8 mb-6 animate-slide-up">
                    <h2 className="text-xl font-semibold text-gray-800 mb-6 flex items-center gap-2">
                        <div className="p-2 bg-gradient-to-br from-primary-500 to-indigo-600 rounded-lg">
                            <Upload className="h-5 w-5 text-white" />
                        </div>
                        上传文档
                    </h2>
                    <div className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                选择文件
                            </label>
                            <div className="flex items-center gap-4">
                                <label className="flex-1 cursor-pointer">
                                    <input
                                        type="file"
                                        onChange={handleFileSelect}
                                        accept=".pdf,.doc,.docx,.txt,.md"
                                        className="hidden"
                                    />
                                    <div className="border-2 border-dashed border-gray-300 rounded-xl p-8 text-center hover:border-primary-500 hover:bg-gradient-to-br hover:from-primary-50/50 hover:to-indigo-50/50 transition-all duration-300 cursor-pointer group">
                                        {selectedFile ? (
                                            <div className="flex items-center justify-center gap-3 animate-fade-in">
                                                <div className="p-3 bg-gradient-to-br from-primary-500 to-indigo-600 rounded-xl shadow-lg">
                                                    <FileText className="h-8 w-8 text-white" />
                                                </div>
                                                <div className="text-left">
                                                    <span className="text-gray-800 font-medium block">{selectedFile.name}</span>
                                                    <span className="text-sm text-gray-500">{formatFileSize(selectedFile.size)}</span>
                                                </div>
                                            </div>
                                        ) : (
                                            <div className="group-hover:scale-105 transition-transform duration-300">
                                                <div className="relative inline-block mb-4">
                                                    <div className="absolute inset-0 bg-gradient-to-br from-primary-400 to-indigo-500 rounded-full blur-xl opacity-30 group-hover:opacity-50 transition-opacity" />
                                                    <div className="relative p-4 bg-gradient-to-br from-primary-500 to-indigo-600 rounded-2xl shadow-lg">
                                                        <Upload className="h-10 w-10 text-white" />
                                                    </div>
                                                </div>
                                                <p className="text-gray-700 font-medium text-lg">点击或拖拽文件到此处</p>
                                                <p className="text-sm text-gray-500 mt-2">支持 PDF, Word, TXT, Markdown</p>
                                            </div>
                                        )}
                                    </div>
                                </label>
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-gray-700 mb-2">
                                文档标题（可选）
                            </label>
                            <input
                                type="text"
                                value={title}
                                onChange={(e) => setTitle(e.target.value)}
                                placeholder="输入文档标题"
                                className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-4 focus:ring-primary-500/20 focus:border-primary-500 transition-all duration-300 bg-white/90 backdrop-blur-sm"
                            />
                        </div>

                        <button
                            onClick={handleUpload}
                            disabled={!selectedFile || uploadMutation.isPending}
                            className="w-full px-6 py-4 bg-gradient-to-r from-primary-500 to-indigo-600 text-white rounded-xl hover:from-primary-600 hover:to-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 font-semibold shadow-lg hover:shadow-xl hover:scale-[1.02] transition-all duration-300 disabled:hover:scale-100"
                        >
                            {uploadMutation.isPending ? (
                                <>
                                    <Loader2 className="h-5 w-5 animate-spin" />
                                    <span>上传中...</span>
                                </>
                            ) : (
                                <>
                                    <Upload className="h-5 w-5" />
                                    <span>上传文档</span>
                                </>
                            )}
                        </button>
                    </div>
                </div>

                {/* 文档列表 */}
                <div className="bg-white/90 backdrop-blur-sm rounded-2xl border border-gray-200/50 shadow-xl overflow-hidden">
                    <div className="p-6 border-b border-gray-200/50 bg-gradient-to-r from-primary-50/50 to-indigo-50/50">
                        <h2 className="text-xl font-semibold text-gray-800 flex items-center gap-2">
                            <FileText className="h-5 w-5 text-primary-600" />
                            文档列表
                        </h2>
                    </div>

                    {isLoading ? (
                        <div className="p-12 text-center">
                            <div className="relative inline-block mb-4">
                                <div className="absolute inset-0 bg-gradient-to-br from-primary-400 to-indigo-500 rounded-full blur-xl opacity-30 animate-pulse-slow" />
                                <Loader2 className="relative h-10 w-10 animate-spin text-primary-600" />
                            </div>
                            <p className="text-gray-600 font-medium">加载中...</p>
                        </div>
                    ) : documents.length === 0 ? (
                        <div className="p-12 text-center">
                            <div className="relative inline-block mb-6">
                                <div className="absolute inset-0 bg-gradient-to-br from-gray-300 to-gray-400 rounded-full blur-2xl opacity-30" />
                                <div className="relative p-6 bg-gradient-to-br from-gray-100 to-gray-200 rounded-2xl">
                                    <FileText className="h-16 w-16 text-gray-400" />
                                </div>
                            </div>
                            <p className="text-gray-600 font-medium text-lg">暂无文档，请先上传文档</p>
                        </div>
                    ) : (
                        <div className="divide-y divide-gray-200/50">
                            {documents.map((doc: Document, index) => (
                                <div 
                                    key={doc.id} 
                                    className="p-6 hover:bg-gradient-to-r hover:from-primary-50/30 hover:to-indigo-50/30 transition-all duration-300 animate-slide-up group"
                                    style={{ animationDelay: `${index * 0.05}s` }}
                                >
                                    <div className="flex items-center justify-between">
                                        <div className="flex items-center gap-4 flex-1">
                                            <div className="flex-shrink-0 relative">
                                                <div className="absolute inset-0 bg-gradient-to-br from-primary-200 to-indigo-200 rounded-full blur-md opacity-50 group-hover:opacity-75 transition-opacity" />
                                                <div className="relative">
                                                    {getStatusIcon(doc.status)}
                                                </div>
                                            </div>
                                            <div className="flex-1 min-w-0">
                                                <h3 className="text-lg font-semibold text-gray-800 truncate group-hover:text-primary-600 transition-colors">
                                                    {doc.title || doc.fileName}
                                                </h3>
                                                <div className="flex items-center gap-3 mt-2 text-sm text-gray-500 flex-wrap">
                                                    <span className="px-2 py-1 bg-gray-100 rounded-md">{doc.fileName}</span>
                                                    <span className="text-gray-300">•</span>
                                                    <span>{formatFileSize(doc.fileSize)}</span>
                                                    {doc.chunkCount && (
                                                        <>
                                                            <span className="text-gray-300">•</span>
                                                            <span className="px-2 py-1 bg-primary-100 text-primary-700 rounded-md font-medium">
                                                                {doc.chunkCount} 个块
                                                            </span>
                                                        </>
                                                    )}
                                                </div>
                                            </div>
                                        </div>
                                        <div className="flex items-center gap-3">
                                            <span className={`px-4 py-1.5 rounded-full text-xs font-semibold shadow-sm ${
                                                doc.status === 'completed'
                                                    ? 'bg-gradient-to-r from-green-100 to-emerald-100 text-green-700 border border-green-200'
                                                    : doc.status === 'processing'
                                                    ? 'bg-gradient-to-r from-blue-100 to-cyan-100 text-blue-700 border border-blue-200 animate-pulse'
                                                    : 'bg-gradient-to-r from-red-100 to-rose-100 text-red-700 border border-red-200'
                                            }`}>
                                                {doc.status === 'completed' ? '✓ 已完成' : doc.status === 'processing' ? '⏳ 处理中' : '✗ 失败'}
                                            </span>
                                            <button
                                                onClick={() => deleteMutation.mutate(doc.id)}
                                                disabled={deleteMutation.isPending}
                                                className="p-2.5 text-red-600 hover:bg-red-50 rounded-xl transition-all duration-300 hover:scale-110 disabled:opacity-50 disabled:hover:scale-100"
                                            >
                                                <Trash2 className="h-5 w-5" />
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

