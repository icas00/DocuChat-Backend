import React, { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { ArrowLeft, ExternalLink } from 'lucide-react';
import { Button } from '../components/ui/Button';

const API_BASE_URL = 'https://icas00-docchat.hf.space';

const TestClientPage = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();
    const scriptRef = useRef(null);
    const [apiKey, setApiKey] = useState(null);

    useEffect(() => {
        // Get API Key from URL or Session
        const urlApiKey = searchParams.get('clientId'); // I used clientId param in StepDeploy
        const sessionData = sessionStorage.getItem('docuChatSession');
        let key = urlApiKey;

        if (!key && sessionData) {
            key = JSON.parse(sessionData).apiKey;
        }

        if (key) {
            setApiKey(key);
            // Inject Widget Script
            if (!document.getElementById('docuchat-widget-script')) {
                const script = document.createElement('script');
                script.id = 'docuchat-widget-script';
                script.src = '/widget.js';
                script.setAttribute('data-api-key', key);
                script.defer = true;
                document.body.appendChild(script);
                scriptRef.current = script;
            }
        } else {
            navigate('/');
        }

        return () => {
            // Cleanup
            const script = document.getElementById('docuchat-widget-script');
            const host = document.getElementById('docuchat-widget-host');
            if (script) script.remove();
            if (host) host.remove();
        };
    }, [searchParams, navigate]);

    if (!apiKey) return null;

    return (
        <div className="min-h-screen bg-white text-gray-900 font-sans">
            {/* Fake Website Header */}
            <header className="border-b border-gray-200 sticky top-0 bg-white/90 backdrop-blur-md z-40">
                <div className="w-full max-w-[1800px] mx-auto px-8 md:px-12 h-20 flex items-center justify-between">
                    <div className="flex items-center gap-12">
                        <div className="font-bold text-2xl tracking-tight flex items-center gap-2">
                            <div className="w-8 h-8 bg-blue-600 rounded-lg"></div>
                            Acme Corp
                        </div>
                        <nav className="hidden md:flex gap-8 text-base font-medium text-gray-600">
                            <a href="#" className="hover:text-blue-600 transition-colors">Products</a>
                            <a href="#" className="hover:text-blue-600 transition-colors">Solutions</a>
                            <a href="#" className="hover:text-blue-600 transition-colors">Pricing</a>
                            <a href="#" className="hover:text-blue-600 transition-colors">Resources</a>
                        </nav>
                    </div>
                    <div className="flex gap-4">
                        <Button variant="ghost" className="text-gray-600 hover:bg-gray-100" onClick={() => navigate('/admin')}>
                            <ArrowLeft className="w-4 h-4 mr-2" /> Back to Admin
                        </Button>
                        <Button className="bg-blue-600 text-white hover:bg-blue-700 shadow-lg shadow-blue-600/20 px-6">Sign Up</Button>
                    </div>
                </div>
            </header>

            {/* Fake Website Hero */}
            <main className="w-full">
                {/* Hero Section */}
                <div className="relative bg-gray-50 overflow-hidden border-b border-gray-200">
                    <div className="absolute inset-0 bg-[url('https://grainy-gradients.vercel.app/noise.svg')] opacity-20"></div>
                    <div className="absolute top-0 right-0 w-[800px] h-[800px] bg-blue-100/50 rounded-full blur-3xl -translate-y-1/2 translate-x-1/3"></div>
                    <div className="absolute bottom-0 left-0 w-[600px] h-[600px] bg-purple-100/50 rounded-full blur-3xl translate-y-1/3 -translate-x-1/4"></div>

                    <div className="max-w-[1800px] mx-auto px-8 md:px-12 py-32 relative z-10">
                        <div className="max-w-4xl">
                            <div className="inline-flex items-center gap-2 px-4 py-2 rounded-full bg-blue-100 text-blue-700 text-sm font-medium mb-8">
                                <span className="relative flex h-2 w-2">
                                    <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-blue-400 opacity-75"></span>
                                    <span className="relative inline-flex rounded-full h-2 w-2 bg-blue-500"></span>
                                </span>
                                New Features Available
                            </div>
                            <h1 className="text-7xl md:text-8xl font-bold tracking-tight mb-8 text-gray-900 leading-[1.1]">
                                Build faster with <br />
                                <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-600">intelligent tools.</span>
                            </h1>
                            <p className="text-2xl text-gray-600 mb-12 leading-relaxed max-w-2xl">
                                This is a demo page to test your AI chatbot. The widget should appear in the bottom-right corner.
                                Try asking it questions about the document you just uploaded.
                            </p>
                            <div className="flex flex-wrap gap-6">
                                <button className="px-10 py-5 bg-blue-600 text-white rounded-full text-lg font-semibold hover:bg-blue-700 transition-all shadow-xl shadow-blue-600/20 hover:scale-105">
                                    Start Free Trial
                                </button>
                                <button className="px-10 py-5 bg-white text-gray-900 border border-gray-200 rounded-full text-lg font-semibold hover:bg-gray-50 transition-all hover:border-gray-300">
                                    View Documentation
                                </button>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Fake Content Grid */}
                <div className="max-w-[1800px] mx-auto px-8 md:px-12 py-32">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-12">
                        {[1, 2, 3].map((i) => (
                            <div key={i} className="h-96 bg-white rounded-3xl border border-gray-100 p-10 shadow-sm hover:shadow-xl transition-shadow duration-300 flex flex-col justify-between group">
                                <div>
                                    <div className={`w-16 h-16 rounded-2xl mb-8 flex items-center justify-center text-2xl ${i === 1 ? 'bg-blue-50 text-blue-600' :
                                        i === 2 ? 'bg-purple-50 text-purple-600' :
                                            'bg-green-50 text-green-600'
                                        }`}>
                                        {i === 1 ? '🚀' : i === 2 ? '⚡' : '🛡️'}
                                    </div>
                                    <div className="h-6 bg-gray-100 rounded-full w-2/3 mb-6 group-hover:bg-gray-200 transition-colors" />
                                    <div className="space-y-3">
                                        <div className="h-3 bg-gray-50 rounded-full w-full" />
                                        <div className="h-3 bg-gray-50 rounded-full w-full" />
                                        <div className="h-3 bg-gray-50 rounded-full w-5/6" />
                                    </div>
                                </div>
                                <div className="h-10 w-32 bg-gray-50 rounded-xl group-hover:bg-blue-50 group-hover:text-blue-600 flex items-center justify-center text-sm font-medium transition-colors cursor-pointer">
                                    Learn more
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </main>
        </div>
    );
};

export default TestClientPage;
