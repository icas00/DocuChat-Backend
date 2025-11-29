import React, { useState, useCallback, useEffect } from 'react';
import { Link } from 'react-router-dom';
import '../styles/AdminPage.css';

const API_BASE_URL = 'https://icas00-docchat.hf.space';

const Status = ({ message, isProcessing, isError }) => {
    if (!message) return <div className="status">&nbsp;</div>;
    const className = `status ${isProcessing ? 'processing' : ''} ${isError ? 'error' : ''}`;
    return <div className={className}>{message}</div>;
};

const AdminPage = () => {
    const [selectedFile, setSelectedFile] = useState(null);
    const [fileName, setFileName] = useState('or drag and drop it here');
    const [status, setStatus] = useState({
        upload: { message: '', processing: false, error: false },
        index: { message: '', processing: false, error: false },
        clear: { message: '', processing: false, error: false },
        settings: { message: '', processing: false, error: false },
    });
    const [isIndexingComplete, setIsIndexingComplete] = useState(false);
    const [settings, setSettings] = useState({
        widgetColor: '#007aff',
        chatbotName: 'AI Assistant',
        welcomeMessage: 'Hi! How can I help you today?',
    });

    const setStatusMessage = (key, message, processing = false, error = false) => {
        setStatus(prev => ({ ...prev, [key]: { message, processing, error } }));
    };

    const resetStates = useCallback(() => {
        setStatus(prev => ({ ...prev, upload: {}, index: {}, clear: {} }));
        setIsIndexingComplete(false);
    }, []);

    const handleFileChange = (file) => {
        if (file) {
            setSelectedFile(file);
            setFileName(file.name);
            resetStates();
        }
    };

    const handleDragOver = (e) => e.preventDefault();
    const handleDrop = (e) => {
        e.preventDefault();
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            handleFileChange(e.dataTransfer.files[0]);
        }
    };

    const handleUpload = async () => {
        if (!selectedFile) return;
        resetStates();
        setStatusMessage('upload', 'Uploading...', true);

        const formData = new FormData();
        formData.append('file', selectedFile);

        try {
            const response = await fetch(`${API_BASE_URL}/api/clients/1/documents`, {
                method: 'POST',
                headers: { 'X-Admin-Key': 'demo-secret-key' },
                body: formData,
            });
            if (!response.ok) throw new Error((await response.json()).message || 'Upload failed');
            setStatusMessage('upload', '✅ Upload complete!');
        } catch (error) {
            setStatusMessage('upload', `❌ ${error.message}`, false, true);
        }
    };

    const handleIndex = async () => {
        setStatusMessage('index', 'Indexing...', true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/clients/1/index`, {
                method: 'POST',
                headers: { 'X-Admin-Key': 'demo-secret-key' },
            });
            if (!response.ok) throw new Error((await response.json()).message || 'Indexing failed');
            setStatusMessage('index', '✅ Indexing Complete!');
            setIsIndexingComplete(true);
        } catch (error) {
            setStatusMessage('index', `❌ ${error.message}`, false, true);
        }
    };

    const handleClearData = async () => {
        if (!window.confirm("Are you sure you want to delete all documents and embeddings? This action cannot be undone.")) return;
        resetStates();
        setStatusMessage('clear', 'Deleting data...', true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/clients/1/data`, {
                method: 'DELETE',
                headers: { 'X-Admin-Key': 'demo-secret-key' },
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.message || 'Failed to clear data');
            setStatusMessage('clear', `✅ ${result.message}`);
            setSelectedFile(null);
            setFileName('or drag and drop it here');
        } catch (error) {
            setStatusMessage('clear', `❌ ${error.message}`, false, true);
        }
    };

    const handleSettingsChange = (e) => {
        const { name, value } = e.target;
        setSettings(prev => ({ ...prev, [name]: value }));
    };

    const handleSaveSettings = async () => {
        setStatusMessage('settings', 'Saving...', true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/clients/1/settings`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Admin-Key': 'demo-secret-key',
                },
                body: JSON.stringify(settings),
            });
            const result = await response.json();
            if (!response.ok) throw new Error(result.message || 'Failed to save settings');
            setStatusMessage('settings', `✅ ${result.message}`);
        } catch (error) {
            setStatusMessage('settings', `❌ ${error.message}`, false, true);
        }
    };
    
    const handleCopyCode = () => {
        const code = `<script src="${API_BASE_URL}/widget.js" data-api-key="TEST_KEY" defer></script>`;
        navigator.clipboard.writeText(code);
    };

    return (
        <div className="admin-container">
            <h1>DocuChat Admin</h1>

            <div className="step">
                <h2>Step 1: Upload Knowledge Base</h2>
                {/* ... upload UI ... */}
            </div>

            <div className="step">
                <h2>Step 2: Customize Widget</h2>
                <div className="form-group">
                    <label htmlFor="chatbotName">Chatbot Name</label>
                    <input type="text" name="chatbotName" value={settings.chatbotName} onChange={handleSettingsChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="welcomeMessage">Welcome Message</label>
                    <input type="text" name="welcomeMessage" value={settings.welcomeMessage} onChange={handleSettingsChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="widgetColor">Widget Color</label>
                    <input type="color" name="widgetColor" value={settings.widgetColor} onChange={handleSettingsChange} />
                </div>
                <button className="btn" onClick={handleSaveSettings} disabled={status.settings.processing}>Save Settings</button>
                <Status {...status.settings} />
            </div>

            <div className="step">
                <h2>Step 3: Index Document</h2>
                {/* ... index UI ... */}
            </div>

            <div className="step">
                <h2>Step 4: Install on Your Website</h2>
                {/* ... install UI ... */}
            </div>

            {isIndexingComplete && (
                <div className="nav-link">
                    <p>🎉 Your chatbot is ready! <Link to="/test-client">Test it now &rarr;</Link></p>
                </div>
            )}

            <div className="step">
                <h2>Maintenance</h2>
                {/* ... clear data UI ... */}
            </div>
        </div>
    );
};

export default AdminPage;
