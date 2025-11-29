import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/LandingPage.css';

const API_BASE_URL = 'https://icas00-docchat.hf.space';

const LandingPage = () => {
    const navigate = useNavigate();
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    const handleGetStarted = async () => {
        setIsLoading(true);
        setError('');
        try {
            const response = await fetch(`${API_BASE_URL}/api/clients/create`, {
                method: 'POST',
            });
            if (!response.ok) {
                throw new Error('Failed to create a new client. Please try again.');
            }
            const data = await response.json();

            // Store keys in sessionStorage to persist across reloads
            sessionStorage.setItem('docuChatSession', JSON.stringify(data));

            // Redirect to the admin page with the new keys in the URL state
            navigate('/admin', { state: { ...data } });
        } catch (err) {
            setError(err.message);
            setIsLoading(false);
        }
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
                    <button onClick={handleGetStarted} className="cta-button" disabled={isLoading}>
                        {isLoading ? 'Creating...' : 'Try the Live Demo →'}
                    </button>
                    {error && <p className="error-message">{error}</p>}
                </div>
            </main>
        </div>
    );
};

export default LandingPage;
