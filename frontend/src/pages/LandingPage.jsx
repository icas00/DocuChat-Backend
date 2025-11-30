import React from 'react';
import { useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { ArrowRight, Sparkles, Bot, Zap } from 'lucide-react';
import { Button } from '../components/ui/Button';
import { Card } from '../components/ui/Card';

const LandingPage = () => {
    const navigate = useNavigate();

    return (
        <div className="min-h-screen relative overflow-hidden bg-twilight-900 text-white selection:bg-pastel-pink selection:text-twilight-900">
            {/* Background Elements */}
            <div className="absolute top-0 left-0 w-full h-full overflow-hidden -z-10">
                <div className="absolute top-[-10%] left-[-10%] w-[50%] h-[50%] bg-twilight-800/40 rounded-full blur-[120px] animate-blob" />
                <div className="absolute top-[20%] right-[-10%] w-[40%] h-[40%] bg-pastel-pink/10 rounded-full blur-[100px] animate-blob animation-delay-2000" />
                <div className="absolute bottom-[-10%] left-[20%] w-[50%] h-[50%] bg-twilight-700/30 rounded-full blur-[120px] animate-blob animation-delay-4000" />
            </div>

            {/* Navbar */}
            <nav className="flex items-center justify-between px-8 md:px-16 py-8 w-full max-w-[1800px] mx-auto">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-xl bg-gradient-to-br from-pastel-pink to-pastel-mint flex items-center justify-center shadow-lg shadow-pastel-pink/20">
                        <Bot className="w-6 h-6 text-twilight-900" />
                    </div>
                    <span className="text-2xl font-serif font-bold tracking-tight">DocuChat</span>
                </div>
                <Button variant="ghost" className="text-base text-gray-300 hover:text-white">Sign In</Button>
            </nav>

            {/* Hero Section */}
            <main className="w-full max-w-[1800px] mx-auto px-8 md:px-16 pt-12 pb-32">
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-20 items-center">

                    {/* Left Content */}
                    <motion.div
                        initial={{ opacity: 0, x: -50 }}
                        animate={{ opacity: 1, x: 0 }}
                        transition={{ duration: 0.8, ease: "easeOut" }}
                        className="max-w-4xl"
                    >
                        <div className="inline-flex items-center gap-2 px-5 py-2.5 rounded-full bg-white/5 border border-white/10 mb-8 backdrop-blur-md">
                            <Sparkles className="w-4 h-4 text-pastel-mint" />
                            <span className="text-sm font-medium text-pastel-mint tracking-wide uppercase">AI-Powered Knowledge Base</span>
                        </div>

                        <h1 className="text-7xl md:text-9xl font-serif font-medium leading-[0.9] mb-8 tracking-tight">
                            Turn docs into <br />
                            <span className="text-gradient">conversations.</span>
                        </h1>

                        <p className="text-2xl text-gray-300 mb-12 max-w-xl leading-relaxed font-light">
                            Upload your documents and instantly create a custom AI chatbot that understands your business.
                        </p>

                        <div className="flex items-center gap-6">
                            <Button onClick={() => navigate('/admin')} className="group h-16 px-10 text-lg bg-white text-twilight-900 hover:bg-pastel-mint transition-colors">
                                Create Chatbot
                                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
                            </Button>
                            <Button variant="secondary" className="h-16 px-10 text-lg border-white/20 hover:bg-white/10">View Demo</Button>
                        </div>
                    </motion.div>

                    {/* Right Visual */}
                    <motion.div
                        initial={{ opacity: 0, scale: 0.8 }}
                        animate={{ opacity: 1, scale: 1 }}
                        transition={{ duration: 0.8, delay: 0.2 }}
                        className="relative hidden lg:block"
                    >
                        <Card className="relative z-10 backdrop-blur-3xl border-white/10 bg-twilight-800/50 p-8 rounded-[2.5rem]">
                            <div className="flex items-center gap-5 mb-8 border-b border-white/5 pb-6">
                                <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-pastel-pink to-pastel-mint flex items-center justify-center shadow-xl shadow-pastel-pink/10">
                                    <Bot className="w-8 h-8 text-twilight-900" />
                                </div>
                                <div>
                                    <h3 className="text-xl font-medium text-white">AI Assistant</h3>
                                    <p className="text-sm text-pastel-mint/80">Online • Reply time: Instant</p>
                                </div>
                            </div>

                            <div className="space-y-6">
                                <div className="flex gap-4">
                                    <div className="bg-white/5 rounded-3xl rounded-tl-none p-6 max-w-[85%] text-lg text-gray-200 leading-relaxed border border-white/5">
                                        Hello! I've analyzed your documents. Ask me anything about your project.
                                    </div>
                                </div>
                                <div className="flex gap-4 justify-end">
                                    <div className="bg-gradient-to-r from-pastel-pink to-pastel-peach rounded-3xl rounded-tr-none p-6 max-w-[85%] text-lg text-twilight-900 font-medium shadow-lg shadow-pastel-pink/10">
                                        What is the pricing model?
                                    </div>
                                </div>
                                <div className="flex gap-4">
                                    <div className="bg-white/5 rounded-3xl rounded-tl-none p-6 max-w-[85%] text-lg text-gray-200 border border-white/5">
                                        <div className="flex gap-2">
                                            <div className="w-2 h-2 rounded-full bg-pastel-mint animate-bounce" />
                                            <div className="w-2 h-2 rounded-full bg-pastel-mint animate-bounce delay-100" />
                                            <div className="w-2 h-2 rounded-full bg-pastel-mint animate-bounce delay-200" />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </Card>

                        {/* Floating Elements */}
                        <motion.div
                            animate={{ y: [0, -20, 0] }}
                            transition={{ duration: 4, repeat: Infinity, ease: "easeInOut" }}
                            className="absolute -top-12 -right-12 bg-twilight-700/80 p-6 rounded-3xl border border-white/10 backdrop-blur-md shadow-2xl"
                        >
                            <Zap className="w-8 h-8 text-pastel-mint" />
                        </motion.div>
                    </motion.div>
                </div>
            </main>
        </div>
    );
};

export default LandingPage;
