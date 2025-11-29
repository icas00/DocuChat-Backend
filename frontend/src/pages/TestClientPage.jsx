import React, { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import '../styles/TestClientPage.css';

const API_BASE_URL = 'https://icas00-docchat.hf.space';

const TestClientPage = () => {
    const { state: locationState } = useLocation();
    const navigate = useNavigate();
    const scriptRef = useRef(null);
    const [appState, setAppState] = useState(null);

    useEffect(() => {
        const sessionData = sessionStorage.getItem('docuChatSession');
        let session = null;
        if (sessionData) {
            session = JSON.parse(sessionData);
        } else if (locationState) {
            session = locationState;
            sessionStorage.setItem('docuChatSession', JSON.stringify(locationState));
        }

        if (session?.apiKey) {
            setAppState(session);
            if (!document.getElementById('docuchat-widget-script')) {
                const script = document.createElement('script');
                script.id = 'docuchat-widget-script';
                script.src = `${API_BASE_URL}/widget.js`;
                script.setAttribute('data-api-key', session.apiKey);
                script.defer = true;
                
                document.body.appendChild(script);
                scriptRef.current = script;
            }
        } else {
            navigate('/');
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
    }, [locationState, navigate]);

    if (!appState) {
        return <div className="test-client-container"><h1>Loading...</h1></div>;
    }

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
                    <Link to="/admin" state={appState}>&larr; Back to Admin Dashboard</Link>
                </div>
            </div>
        </div>
    );
};

export default TestClientPage;
