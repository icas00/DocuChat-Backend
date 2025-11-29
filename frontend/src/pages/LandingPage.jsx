import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/LandingPage.css';

const LandingPage = () => {
    const navigate = useNavigate();

    const handleGetStarted = () => {
        // In a real app, this might call an API to create a new client.
        // For this demo, we'll navigate directly to the admin page.
        navigate('/admin');
    };

    return (
        <div className="landing-container">
            <header className="landing-header">
                <h1>DocuChat</h1>
                <p className="subtitle">Create a custom AI chatbot from your documents in minutes.</p>
            </header>
            <main className="landing-main">
                <div className="cta-box">
                    <h2>Get Started</h2>
                    <p>Upload a document, index it, and deploy a fully functional AI assistant to your website. No code required.</p>
                    <button onClick={handleGetStarted} className="cta-button">
                        Try the Live Demo &rarr;
                    </button>
                </div>
            </main>
        </div>
    );
};

export default LandingPage;
