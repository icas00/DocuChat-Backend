import React from 'react';
import { motion } from 'framer-motion';
import { MessageSquare, X, Send } from 'lucide-react';
import { cn } from '../utils/cn';

export const WidgetPreview = ({ config, isOpen = true }) => {
    const { name, welcomeMessage, color, avatar } = config;

    return (
        <div className="relative w-full max-w-sm mx-auto h-[600px] bg-transparent flex items-end justify-end p-4">
            {/* Widget Window */}
            <motion.div
                initial={{ opacity: 0, scale: 0.9, y: 20 }}
                animate={{ opacity: isOpen ? 1 : 0, scale: isOpen ? 1 : 0.9, y: isOpen ? 0 : 20 }}
                className="w-full bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col h-[500px] border border-gray-100"
            >
                {/* Header */}
                <div
                    className="p-4 flex items-center justify-between text-white"
                    style={{ background: color || '#0ea5e9' }}
                >
                    <div className="flex items-center gap-3">
                        <div className="w-8 h-8 rounded-full bg-white/20 flex items-center justify-center text-lg backdrop-blur-sm">
                            {avatar || '🤖'}
                        </div>
                        <div>
                            <h3 className="font-medium text-sm">{name || 'AI Assistant'}</h3>
                            <p className="text-xs opacity-80">Online</p>
                        </div>
                    </div>
                    <X className="w-5 h-5 opacity-80 cursor-pointer hover:opacity-100" />
                </div>

                {/* Chat Area */}
                <div className="flex-1 bg-gray-50 p-4 overflow-y-auto space-y-4">
                    <div className="flex gap-3">
                        <div
                            className="w-8 h-8 rounded-full flex items-center justify-center text-sm shrink-0"
                            style={{ background: color || '#0ea5e9', color: 'white' }}
                        >
                            {avatar || '🤖'}
                        </div>
                        <div className="bg-white p-3 rounded-2xl rounded-tl-none shadow-sm text-sm text-gray-700 max-w-[80%]">
                            {welcomeMessage || 'Hi! How can I help you today?'}
                        </div>
                    </div>
                </div>

                {/* Input Area */}
                <div className="p-4 bg-white border-t border-gray-100">
                    <div className="relative">
                        <input
                            type="text"
                            placeholder="Type a message..."
                            className="w-full bg-gray-50 border border-gray-200 rounded-full px-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-offset-1 pr-10 text-gray-800"
                            style={{ '--tw-ring-color': color || '#0ea5e9' }}
                        />
                        <button
                            className="absolute right-2 top-1/2 -translate-y-1/2 p-1.5 rounded-full transition-colors hover:bg-gray-100"
                            style={{ color: color || '#0ea5e9' }}
                        >
                            <Send className="w-4 h-4" />
                        </button>
                    </div>
                    <div className="text-center mt-2">
                        <span className="text-[10px] text-gray-400">Powered by DocuChat</span>
                    </div>
                </div>
            </motion.div>
        </div>
    );
};
