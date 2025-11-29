import React, { useEffect, useRef } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import '../styles/TestClientPage.css';

const API_BASE_URL = 'https://icas00-docchat.hf.space';

const TestClientPage = () => {
    const { state } = useLocation();
    const navigate = useNavigate();
    const scriptRef = useRef(null);

    useEffect(() => {
        const apiKey = state?.apiKey;
        const adminKey = state?.adminKey;

        if (!apiKey || !adminKey) {
            navigate('/');
            return;
        }

        if (!document.getElementById('docuchat-widget-script')) {
            const script = document.createElement('script');
            script.id = 'docuchat-widget-script';
            script.src = `${API_BASE_URL}/widget.js`;
            script.setAttribute('data-api-key', apiKey);
            script.defer = true;
            
            document.body.appendChild(script);
            scriptRef.current = script;
        }

        return () => {
            const scriptElement = scriptRef.current;
            const hostElement = document.getElementById('docuchat-widget-host');
            if (scriptElement && scriptElement.parentNode) {
                scriptElement.parentNode.removeChild(scriptElement);
            }
            if (hostElement && hostElement.parentNode) {
                hostElement.parentNode.removeChild(hostElement);
            }
        };
    }, [state, navigate]);

    return (
        <div className="test-client-container">
            <div className="main-content">
                <h1>Live Test Page</h1>
                <p>This page simulates a client's website where the AI chat widget is embedded.</p>

                <div className="demo-card">
                    <h2>Your AI Assistant is Live!</h2>
                    <p>The bot is powered by the document you just indexed on the admin dashboard.</p>
                    <p>Click the chat bubble in the bottom-right corner to start a conversation.</p>
                </div>

                <div className="nav-link">
                    <Link to="/admin" state={{ ...state }}>&larr; Back to Admin Dashboard</Link>
                </div>
            </div>
        </div>
    );
};

export default TestClientPage;
