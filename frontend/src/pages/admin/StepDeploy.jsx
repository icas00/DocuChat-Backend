import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { Check, Copy, ExternalLink, Code } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';
import { WidgetPreview } from '../../components/WidgetPreview';

const StepDeploy = ({ config, clientId }) => {
    const [copied, setCopied] = useState(false);

    const scriptCode = `<script 
  src="${window.location.origin}/widget.js"
  data-api-key="${clientId}"
  defer>
</script>`;

    const handleCopy = () => {
        navigator.clipboard.writeText(scriptCode);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-32 xl:gap-40 h-full items-start max-w-[1600px] mx-auto">
            {/* Left: Success & Code */}
            <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                className="relative z-10"
            >
                <Card className="p-10 bg-glass-100 border-glass-200 backdrop-blur-xl shadow-2xl">
                    <div className="mb-8">
                        <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-green-500/20 text-green-400 text-sm mb-6 border border-green-500/30 font-medium tracking-wide">
                            <Check className="w-4 h-4" />
                            <span>DEPLOYMENT READY</span>
                        </div>
                        <h2 className="text-6xl md:text-7xl font-serif mb-6 leading-tight text-white">It's alive!</h2>
                        <p className="text-xl text-gray-300 font-light">Your AI assistant is trained and ready to be embedded.</p>
                    </div>

                    <Card className="bg-midnight-900/50 border-white/10 mb-8 overflow-hidden">
                        <div className="flex items-center justify-between p-4 border-b border-white/5 bg-white/5">
                            <div className="flex items-center gap-2 text-sm text-gray-300 font-medium">
                                <Code className="w-4 h-4" />
                                <span>Embed Code</span>
                            </div>
                            <Button
                                variant="ghost"
                                className="h-8 text-xs hover:bg-white/10"
                                onClick={handleCopy}
                            >
                                {copied ? (
                                    <span className="text-green-400 flex items-center gap-1">
                                        <Check className="w-3 h-3" /> Copied
                                    </span>
                                ) : (
                                    <span className="flex items-center gap-1 text-gray-300">
                                        <Copy className="w-3 h-3" /> Copy Code
                                    </span>
                                )}
                            </Button>
                        </div>
                        <div className="p-6 overflow-x-auto">
                            <pre className="font-mono text-sm text-blue-300 leading-relaxed">{scriptCode}</pre>
                        </div>
                    </Card>

                    <div className="flex gap-6">
                        <Button className="flex-1 h-14 text-lg bg-white text-midnight-900 hover:bg-gray-100 font-semibold shadow-lg shadow-white/10" onClick={() => window.open(`/test-client?clientId=${clientId}`, '_blank')}>
                            Open Full Preview <ExternalLink className="w-5 h-5 ml-2" />
                        </Button>
                        <Button variant="secondary" className="flex-1 h-14 text-lg border-white/20 hover:bg-white/5" onClick={() => window.location.reload()}>
                            Create Another
                        </Button>
                    </div>
                </Card>
            </motion.div>

            {/* Right: Final Preview */}
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
                {/* Large decorative background */}
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[140%] h-[140%] bg-gradient-to-tr from-aurora-500/10 to-coral-500/10 blur-[100px] -z-10 rounded-full animate-pulse-slow" />

                {/* Scaled up preview */}
                <div className="transform scale-[1.35] origin-center">
                    <WidgetPreview config={config} isOpen={true} />
                </div>
            </motion.div>
        </div>
    );
};

export default StepDeploy;
