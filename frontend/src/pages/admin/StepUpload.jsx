import React, { useState, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Upload, FileText, CheckCircle, Loader2, AlertCircle, Zap } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { cn } from '../../utils/cn';
import NeuralNetwork from '../../components/NeuralNetwork';

const StepUpload = ({ onUpload, isUploading, error }) => {
    const [isDragging, setIsDragging] = useState(false);
    const [file, setFile] = useState(null);
    const [uploadStep, setUploadStep] = useState('select'); // 'select', 'ready', 'training'

    const handleDragOver = useCallback((e) => {
        e.preventDefault();
        setIsDragging(true);
    }, []);

    const handleDragLeave = useCallback((e) => {
        e.preventDefault();
        setIsDragging(false);
    }, []);

    const handleDrop = useCallback((e) => {
        e.preventDefault();
        setIsDragging(false);
        const droppedFile = e.dataTransfer.files[0];
        if (droppedFile) {
            setFile(droppedFile);
            setUploadStep('ready');
        }
    }, []);

    const handleFileSelect = (e) => {
        if (e.target.files[0]) {
            setFile(e.target.files[0]);
            setUploadStep('ready');
        }
    };

    const handleTrain = () => {
        if (file) {
            setUploadStep('training');
            onUpload(file);
        }
    };

    return (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-32 xl:gap-40 h-full items-center relative max-w-[1600px] mx-auto">
            {/* Left: Upload Zone */}
            <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                className="relative z-10"
            >
                <Card className="p-10 bg-glass-100 border-glass-200 backdrop-blur-xl shadow-2xl">
                    <div className="mb-8">
                        <h2 className="text-6xl md:text-7xl font-serif mb-6 leading-tight text-white">
                            Feed the <span className="text-gradient">Brain</span>.
                        </h2>
                        <p className="text-xl text-gray-300 font-light max-w-lg">
                            Upload your knowledge base to train your assistant.
                        </p>
                    </div>

                    <div className="space-y-8">
                        {/* File Drop Zone */}
                        <div
                            onDragOver={handleDragOver}
                            onDragLeave={handleDragLeave}
                            onDrop={handleDrop}
                            className={cn(
                                "relative h-80 border-2 border-dashed rounded-3xl flex flex-col items-center justify-center transition-all duration-300 cursor-pointer overflow-hidden group backdrop-blur-sm",
                                isDragging
                                    ? "border-pastel-mint bg-pastel-mint/10 scale-[1.02]"
                                    : "border-glass-200 hover:border-pastel-mint/50 hover:bg-glass-100",
                                uploadStep === 'training' && "opacity-50 pointer-events-none"
                            )}
                        >
                            <input
                                type="file"
                                className="absolute inset-0 opacity-0 cursor-pointer z-20"
                                onChange={handleFileSelect}
                                accept=".pdf,.txt,.md,.csv"
                                disabled={uploadStep === 'training'}
                            />

                            <AnimatePresence mode="wait">
                                {!file ? (
                                    <motion.div
                                        key="empty"
                                        initial={{ opacity: 0, y: 10 }}
                                        animate={{ opacity: 1, y: 0 }}
                                        exit={{ opacity: 0, y: -10 }}
                                        className="text-center p-6"
                                    >
                                        <div className="w-24 h-24 rounded-full bg-glass-200 flex items-center justify-center mx-auto mb-6 group-hover:scale-110 transition-transform duration-300 shadow-lg shadow-pastel-mint/10">
                                            <Upload className="w-12 h-12 text-pastel-mint" />
                                        </div>
                                        <p className="text-2xl font-medium mb-2 text-white">Drop your file here</p>
                                        <p className="text-lg text-gray-400">or click to browse (PDF, TXT, MD)</p>
                                    </motion.div>
                                ) : (
                                    <motion.div
                                        key="file"
                                        initial={{ opacity: 0, scale: 0.8 }}
                                        animate={{ opacity: 1, scale: 1 }}
                                        exit={{ opacity: 0, scale: 0.8 }}
                                        className="text-center p-6 z-10"
                                    >
                                        <div className="w-24 h-24 rounded-full bg-pastel-mint/20 flex items-center justify-center mx-auto mb-6 shadow-lg shadow-pastel-mint/20">
                                            <FileText className="w-12 h-12 text-pastel-mint" />
                                        </div>
                                        <p className="text-2xl font-medium mb-2 truncate max-w-[300px] text-white">{file.name}</p>
                                        <p className="text-lg text-gray-400 mb-6">{(file.size / 1024).toFixed(2)} KB</p>
                                        <Button
                                            variant="ghost"
                                            className="text-base text-red-400 hover:text-red-300 hover:bg-red-400/10 px-6 py-2"
                                            onClick={(e) => {
                                                e.preventDefault();
                                                e.stopPropagation();
                                                setFile(null);
                                                setUploadStep('select');
                                            }}
                                        >
                                            Remove File
                                        </Button>
                                    </motion.div>
                                )}
                            </AnimatePresence>
                        </div>

                        {/* Error Message */}
                        {error && (
                            <div className="flex items-center gap-3 text-red-300 bg-red-500/10 p-4 rounded-xl text-lg border border-red-500/20">
                                <AlertCircle className="w-6 h-6 shrink-0" />
                                {error}
                            </div>
                        )}

                        {/* Train Button */}
                        <AnimatePresence>
                            {uploadStep === 'ready' && (
                                <motion.div
                                    initial={{ opacity: 0, y: 20 }}
                                    animate={{ opacity: 1, y: 0 }}
                                    exit={{ opacity: 0, y: 10 }}
                                >
                                    <Button
                                        onClick={handleTrain}
                                        className="w-full h-20 text-2xl bg-pastel-mint text-twilight-900 hover:bg-pastel-mint/90 font-bold shadow-xl shadow-pastel-mint/20 rounded-2xl"
                                    >
                                        <Zap className="w-8 h-8 mr-3 fill-current" />
                                        Start Training
                                    </Button>
                                </motion.div>
                            )}

                            {uploadStep === 'training' && (
                                <motion.div
                                    initial={{ opacity: 0 }}
                                    animate={{ opacity: 1 }}
                                    className="w-full h-20 bg-glass-100 rounded-2xl flex items-center justify-center text-xl font-medium text-pastel-mint border border-pastel-mint/20"
                                >
                                    <Loader2 className="w-8 h-8 animate-spin mr-4" />
                                    Training Neural Network...
                                </motion.div>
                            )}
                        </AnimatePresence>
                    </div>
                </Card>
            </motion.div>

            {/* Right: Visualization */}
            <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 1 }}
                className="absolute inset-0 lg:relative lg:inset-auto h-full w-full pointer-events-none flex items-center justify-center"
            >
                {/* Full screen neural network container with background glow */}
                <div className="w-full h-full lg:h-[800px] relative">
                    <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[120%] h-[120%] bg-pastel-mint/5 blur-[120px] -z-10 rounded-full" />
                    <NeuralNetwork isActive={uploadStep === 'training'} />

                    {/* Overlay Text */}
                    <div className="absolute bottom-10 left-10 text-base text-pastel-mint/50 font-mono tracking-[0.2em] uppercase">
                        System Status: {uploadStep === 'training' ? 'PROCESSING DATA STREAM...' : 'AWAITING INPUT'}
                        <br />
                        Neural Nodes: {uploadStep === 'training' ? 'ACTIVE' : 'STANDBY'}
                    </div>
                </div>
            </motion.div>
        </div>
    );
};

export default StepUpload;
