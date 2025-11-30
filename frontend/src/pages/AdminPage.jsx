import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import { ChevronLeft, ChevronRight, Loader2 } from 'lucide-react';
import StepIdentity from './admin/StepIdentity';
import StepUpload from './admin/StepUpload';
import StepDeploy from './admin/StepDeploy';
import { Button } from '../components/ui/Button';

const API_BASE_URL = 'https://icas00-docchat.hf.space';

const AdminPage = () => {
    const navigate = useNavigate();
    const [step, setStep] = useState(1);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);

    // State
    const [clientData, setClientData] = useState(null);
    const [config, setConfig] = useState({
        name: '',
        welcomeMessage: '',
        color: '#0ea5e9',
        avatar: '🤖'
    });

    // Initialize Client on Mount (or check session)
    useEffect(() => {
        const initClient = async () => {
            const session = sessionStorage.getItem('docuChatSession');
            if (session) {
                setClientData(JSON.parse(session));
            } else {
                // Create new client automatically
                try {
                    const res = await fetch(`${API_BASE_URL}/api/clients/create`, { method: 'POST' });
                    const data = await res.json();
                    setClientData(data);
                    sessionStorage.setItem('docuChatSession', JSON.stringify(data));
                } catch (err) {
                    setError('Failed to initialize client. Please try again.');
                }
            }
        };
        initClient();
    }, []);

    const handleNext = async () => {
        if (step === 1) {
            // Save Settings
            setIsLoading(true);
            try {
                await fetch(`${API_BASE_URL}/api/clients/${clientData.clientId}/settings`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-Admin-Key': clientData.adminKey
                    },
                    body: JSON.stringify({
                        chatbotName: config.name,
                        welcomeMessage: config.welcomeMessage,
                        widgetColor: config.color
                    })
                });
                setStep(2);
            } catch (err) {
                setError('Failed to save settings.');
            } finally {
                setIsLoading(false);
            }
        } else if (step === 2) {
            // Indexing is triggered after upload in StepUpload
            setStep(3);
        }
    };

    const handleUpload = async (file) => {
        if (!clientData?.adminKey) {
            setError('Admin Key missing. Please reload the page.');
            return;
        }

        setIsLoading(true);
        setError(null);
        try {
            // 1. Upload
            const formData = new FormData();
            formData.append('file', file);

            const uploadRes = await fetch(`${API_BASE_URL}/api/clients/${clientData.clientId}/documents`, {
                method: 'POST',
                headers: { 'X-Admin-Key': clientData.adminKey },
                body: formData
            });

            if (!uploadRes.ok) {
                const errData = await uploadRes.json().catch(() => ({}));
                throw new Error(errData.message || 'Upload failed');
            }

            // 2. Index (Fire and forget / Handle Timeout)
            try {
                const indexRes = await fetch(`${API_BASE_URL}/api/clients/${clientData.clientId}/index`, {
                    method: 'POST',
                    headers: { 'X-Admin-Key': clientData.adminKey }
                });

                if (!indexRes.ok) {
                    console.warn('Indexing response not OK:', indexRes.status);
                    // If 403 or 504, we assume it's running in background or timed out
                    if (indexRes.status !== 403 && indexRes.status !== 504) {
                        const errData = await indexRes.json().catch(() => ({}));
                        throw new Error(errData.message || 'Indexing failed');
                    }
                }
            } catch (indexErr) {
                console.warn('Indexing warning (likely timeout):', indexErr);
                // We proceed anyway because the backend logs show it starts
            }

            // Success -> Move to next step
            // Add a small delay for effect
            setTimeout(() => setStep(3), 2000);

        } catch (err) {
            console.error(err);
            setError(err.message || 'Something went wrong');
            setIsLoading(false); // Only stop loading on actual error
        }
        // Note: We don't set isLoading(false) here if successful, 
        // because the component unmounts/transitions
    };

    if (!clientData) return (
        <div className="min-h-screen flex items-center justify-center">
            <Loader2 className="w-8 h-8 animate-spin text-aurora-500" />
        </div>
    );

    return (
        <div className="min-h-screen bg-midnight-900 text-white overflow-hidden flex flex-col">
            {/* Header */}
            <header className="px-12 py-8 flex items-center justify-between border-b border-white/5 bg-midnight-900/50 backdrop-blur-md sticky top-0 z-50">
                <div className="flex items-center gap-4 cursor-pointer group" onClick={() => navigate('/')}>
                    <div className="p-3 rounded-xl bg-white/5 group-hover:bg-white/10 transition-colors">
                        <ChevronLeft className="w-8 h-8 text-gray-300" />
                    </div>
                    <span className="font-serif font-bold text-4xl tracking-tight text-white">DocuChat</span>
                </div>

                {/* Progress Steps */}
                <div className="flex items-center gap-8">
                    {[1, 2, 3].map((s) => (
                        <div key={s} className="flex items-center gap-4">
                            <div className={`w-4 h-4 rounded-full transition-all duration-500 ${step >= s ? 'bg-pastel-mint scale-125 shadow-[0_0_15px_rgba(161,196,253,0.6)]' : 'bg-white/10'}`} />
                            {s < 3 && <div className={`w-16 h-1 rounded-full transition-all duration-500 ${step > s ? 'bg-pastel-mint' : 'bg-white/5'}`} />}
                        </div>
                    ))}
                </div>

                <Button variant="ghost" className="text-xl text-gray-300 hover:text-white font-medium" onClick={() => {
                    sessionStorage.removeItem('docuChatSession');
                    window.location.reload();
                }}>
                    Start Over
                </Button>
            </header>

            {/* Main Content */}
            <main className="flex-1 w-full px-12 py-12 relative overflow-hidden">
                {/* Ambient Background Elements to fill space */}
                <div className="absolute top-0 left-0 w-full h-full overflow-hidden -z-10 pointer-events-none">
                    <div className="absolute top-[10%] left-[10%] w-[40vw] h-[40vw] bg-aurora-500/5 rounded-full blur-[150px] animate-pulse-slow" />
                    <div className="absolute bottom-[10%] right-[10%] w-[40vw] h-[40vw] bg-coral-500/5 rounded-full blur-[150px] animate-pulse-slow delay-1000" />
                </div>
                <AnimatePresence mode="wait">
                    {step === 1 && (
                        <motion.div key="step1" exit={{ opacity: 0, x: -20 }} className="h-full">
                            <StepIdentity
                                config={config}
                                setConfig={setConfig}
                                onNext={handleNext}
                            />
                        </motion.div>
                    )}

                    {step === 2 && (
                        <motion.div key="step2" exit={{ opacity: 0, x: -20 }} className="h-full">
                            <StepUpload
                                onUpload={handleUpload}
                                isUploading={isLoading}
                                error={error}
                            />
                        </motion.div>
                    )}

                    {step === 3 && (
                        <motion.div key="step3" className="h-full">
                            <StepDeploy
                                config={config}
                                clientId={clientData.apiKey}
                            />
                        </motion.div>
                    )}
                </AnimatePresence>
            </main>
        </div>
    );
};

export default AdminPage;
