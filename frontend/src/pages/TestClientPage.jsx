import React, { useEffect } from 'react';
import { Link } from 'react-router-dom';
import '../styles/TestClientPage.css';

const API_BASE_URL = 'https://icas00-docchat.hf.space';

const TestClientPage = () => {
    useEffect(() => {
        const script = document.createElement('script');
        script.src = `${API_BASE_URL}/widget.js`;
        script.setAttribute('data-api-key', 'TEST_KEY');
        script.defer = true;

        document.body.appendChild(script);

        // Cleanup function to remove the script when the component unmounts
        return () => {
            document.body.removeChild(script);
        };
    }, []);

    return (
        <div className="test-client-container">
            <div className="main-content">
                <h1>Live Test Page</h1>
                <p>This page simulates a client's website where the AI chat widget is embedded.</p>

                <div className="demo-card">
                    <h2>Your AI Assistant is Live!</h2>
                    <p>The bot is powered by the document you just indexed on the admin dashboard.</p>
                    <p>Click the chat bubble in the bottom-right corner to start a conversation. Ask questions based on the content of your document to see it in action.</p>
                </div>

                <div className="nav-link">
                    <Link to="/admin">&larr; Back to Admin Dashboard</Link>
                </div>
            </div>
        </div>
    );
};

export default TestClientPage;
