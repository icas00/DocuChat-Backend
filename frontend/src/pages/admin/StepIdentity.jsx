import React from 'react';
import { motion } from 'framer-motion';
import { Input } from '../../components/ui/Input';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { WidgetPreview } from '../../components/WidgetPreview';

const AVATAR_OPTIONS = ['🤖', '👩‍💼', '⚡', '🧠', '👾'];
const COLOR_OPTIONS = ['#0ea5e9', '#f43f5e', '#8b5cf6', '#10b981', '#f59e0b'];

const StepIdentity = ({ config, setConfig, onNext }) => {
    return (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-32 xl:gap-40 h-full items-center max-w-[1600px] mx-auto">
            {/* Left: Form */}
            <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                className="relative z-10"
            >
                <Card className="p-10 bg-glass-100 border-glass-200 backdrop-blur-xl shadow-2xl">
                    <div className="mb-8">
                        <h2 className="text-6xl md:text-7xl font-serif mb-6 leading-tight text-white">
                            Give it a <span className="text-gradient">Personality</span>.
                        </h2>
                        <p className="text-xl text-gray-300 font-light">
                            Customize how your assistant looks and talks.
                        </p>
                    </div>

                    <div className="space-y-8">
                        <div>
                            <label className="block text-lg font-medium text-gray-200 mb-3">Name</label>
                            <Input
                                value={config.name}
                                onChange={(e) => setConfig({ ...config, name: e.target.value })}
                                placeholder="e.g. Support Bot"
                                className="text-lg py-4"
                            />
                        </div>

                        <div>
                            <label className="block text-lg font-medium text-gray-200 mb-3">Welcome Message</label>
                            <Input
                                value={config.welcomeMessage}
                                onChange={(e) => setConfig({ ...config, welcomeMessage: e.target.value })}
                                placeholder="e.g. Hi! How can I help?"
                                className="text-lg py-4"
                            />
                        </div>

                        <div>
                            <label className="block text-lg font-medium text-gray-200 mb-4">Avatar</label>
                            <div className="flex gap-4">
                                {AVATAR_OPTIONS.map((emoji) => (
                                    <button
                                        key={emoji}
                                        onClick={() => setConfig({ ...config, avatar: emoji })}
                                        className={`w-16 h-16 rounded-2xl flex items-center justify-center text-3xl transition-all ${config.avatar === emoji
                                            ? 'bg-glass-200 border-2 border-aurora-500 scale-110 shadow-lg shadow-aurora-500/20'
                                            : 'bg-glass-100 border border-glass-200 hover:bg-glass-200'
                                            }`}
                                    >
                                        {emoji}
                                    </button>
                                ))}
                            </div>
                        </div>

                        <div>
                            <label className="block text-lg font-medium text-gray-200 mb-4">Accent Color</label>
                            <div className="flex gap-4">
                                {COLOR_OPTIONS.map((color) => (
                                    <button
                                        key={color}
                                        onClick={() => setConfig({ ...config, color })}
                                        className={`w-10 h-10 rounded-full transition-all ${config.color === color
                                            ? 'ring-4 ring-offset-4 ring-offset-midnight-900 ring-white scale-110'
                                            : 'hover:scale-110 ring-2 ring-transparent hover:ring-white/20'
                                            }`}
                                        style={{ backgroundColor: color }}
                                    />
                                ))}
                            </div>
                        </div>
                    </div>

                    <Button onClick={onNext} className="w-full mt-10 text-lg h-14 bg-gradient-to-r from-aurora-500 to-blue-500 hover:from-aurora-400 hover:to-blue-400 border-none shadow-lg shadow-blue-500/20">
                        Continue
                    </Button>
                </Card>
            </motion.div>

            {/* Right: Preview */}
            <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1, y: [0, -15, 0] }}
                transition={{
                    delay: 0.2,
                    y: {
                        duration: 6,
                        repeat: Infinity,
                        ease: "easeInOut"
                    }
                }}
                className="relative flex justify-center items-center"
            >
                {/* Large decorative background to fill space */}
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[140%] h-[140%] bg-gradient-to-tr from-aurora-500/10 to-coral-500/10 blur-[100px] -z-10 rounded-full animate-pulse-slow" />

                {/* Scaled up preview */}
                <div className="transform scale-[1.35] origin-center">
                    <WidgetPreview config={config} />
                </div>
            </motion.div>
        </div>
    );
};

export default StepIdentity;
