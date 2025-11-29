import React, { useState, useCallback, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import '../styles/AdminPage.css';

const API_BASE_URL = 'https://icas00-docchat.hf.space';

// A smaller, reusable Status component
const Status = ({ message, isProcessing, isError }) => {
    if (!message) return <div className="status">&nbsp;</div>;
    const className = `status ${isProcessing ? 'processing' : ''} ${isError ? 'error' : ''}`;
    return <div className={className}>{message}</div>;
};

const AdminPage = () => {
    const { state: locationState } = useLocation();
    const navigate = useNavigate();

    // App state now holds all keys and is the single source of truth
    const [appState, setAppState] = useState(null);

    const [selectedFile, setSelectedFile] = useState(null);
    const [fileName, setFileName] = useState('or drag and drop it here');
    const [status, setStatus] = useState({
        upload: {}, index: {}, clear: {}, settings: {}
    });
    const [isIndexingComplete, setIsIndexingComplete] = useState(false);
    const [settings, setSettings] = useState({
        widgetColor: '#007aff',
        chatbotName: 'AI Assistant',
        welcomeMessage: 'Hi! How can I help you today?',
    });

    // On component mount, check for session storage first, then location state.
    useEffect(() => {
        const sessionData = sessionStorage.getItem('docuChatSession');
        if (sessionData) {
            setAppState(JSON.parse(sessionData));
        } else if (locationState) {
            sessionStorage.setItem('docuChatSession', JSON.stringify(locationState));
            setAppState(locationState);
        } else {
            navigate('/');
        }
    }, [locationState, navigate]);

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
    
    const handleStartOver = () => {
        sessionStorage.removeItem('docuChatSession');
        navigate('/');
    };

    const handleDragOver = (e) => e.preventDefault();
    const handleDrop = (e) => {
        e.preventDefault();
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            handleFileChange(e.dataTransfer.files[0]);
        }
    };

    // All API call functions remain the same, but now use appState for keys
    const handleUpload = async () => {
        if (!selectedFile || !appState) return;
        resetStates();
        setStatusMessage('upload', 'Uploading...', true);
        const formData = new FormData();
        formData.append('file', selectedFile);
        try {
            const response = await fetch(`${API_BASE_URL}/api/clients/${appState.clientId}/documents`, {
                method: 'POST', headers: { 'X-Admin-Key': appState.adminKey }, body: formData,
            });
            if (!response.ok) throw new Error((await response.json()).message || 'Upload failed');
            setStatusMessage('upload', '✅ Upload complete!');
        } catch (error) {
            setStatusMessage('upload', `❌ ${error.message}`, false, true);
        }
    };

    const handleIndex = async () => {
        if (!appState) return;
        setStatusMessage('index', 'Indexing...', true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/clients/${appState.clientId}/index`, {
                method: 'POST', headers: { 'X-Admin-Key': appState.adminKey },
            });
            if (!response.ok) throw new Error((await response.json()).message || 'Indexing failed');
            setStatusMessage('index', '✅ Indexing Complete!');
            setIsIndexingComplete(true);
        } catch (error) {
            setStatusMessage('index', `❌ ${error.message}`, false, true);
        }
    };

    const handleClearData = async () => {
        if (!appState || !window.confirm("Are you sure you want to delete all documents and embeddings?")) return;
        resetStates();
        setStatusMessage('clear', 'Deleting data...', true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/clients/${appState.clientId}/data`, {
                method: 'DELETE', headers: { 'X-Admin-Key': appState.adminKey },
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
        if (!appState) return;
        setStatusMessage('settings', 'Saving...', true);
        try {
            const response = await fetch(`${API_BASE_URL}/api/clients/${appState.clientId}/settings`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json', 'X-Admin-Key': appState.adminKey },
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
        if (!appState) return;
        const code = `<script src="${API_BASE_URL}/widget.js" data-api-key="${appState.apiKey}" defer></script>`;
        navigator.clipboard.writeText(code);
    };

    if (!appState) {
        return <div className="admin-container"><h1>Loading...</h1></div>;
    }

    return (
        <div className="admin-container">
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                <h1>DocuChat Admin</h1>
                <button onClick={handleStartOver} className="btn warn" style={{width: 'auto', marginTop: 0}}>Start Over</button>
            </div>
            
            <div className="step">
                <h2>Step 1: Upload Knowledge Base</h2>
                <div className="upload-area" onDragOver={handleDragOver} onDrop={handleDrop}>
                    <input type="file" id="file-upload" accept=".txt,.md,.pdf" style={{ display: 'none' }} onChange={(e) => handleFileChange(e.target.files[0])} />
                    <label htmlFor="file-upload" className="btn">Choose File</label>
                    <p>{fileName}</p>
                </div>
                <button className="btn" onClick={handleUpload} disabled={!selectedFile || status.upload.processing}>Upload Document</button>
                <Status {...status.upload} />
            </div>

            <div className="step">
                <h2>Step 2: Index Document</h2>
                <button className="btn" onClick={handleIndex} disabled={status.upload.message !== '✅ Upload complete!' || status.index.processing}>Create Embeddings</button>
                <Status {...status.index} />
            </div>

            <div className="step">
                <h2>Step 3: Customize Widget</h2>
                <div className="form-group">
                    <label htmlFor="chatbotName">Chatbot Name</label>
                    <input type="text" id="chatbotName" name="chatbotName" value={settings.chatbotName} onChange={handleSettingsChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="welcomeMessage">Welcome Message</label>
                    <input type="text" id="welcomeMessage" name="welcomeMessage" value={settings.welcomeMessage} onChange={handleSettingsChange} />
                </div>
                <div className="form-group">
                    <label htmlFor="widgetColor">Widget Color</label>
                    <input type="color" id="widgetColor" name="widgetColor" value={settings.widgetColor} onChange={handleSettingsChange} />
                </div>
                <button className="btn" onClick={handleSaveSettings} disabled={status.settings.processing}>Save Settings</button>
                <Status {...status.settings} />
            </div>

            <div className="step">
                <h2>Step 4: Install on Your Website</h2>
                <div className="code-box">
                    <button className="copy-btn" onClick={handleCopyCode}>Copy</button>
                    <pre><code>{`<script 
  src="${API_BASE_URL}/widget.js" 
  data-api-key="${appState.apiKey}" 
  defer>
</script>`}</code></pre>
                </div>
            </div>

            {isIndexingComplete && (
                <div className="nav-link">
                    <p>🎉 Your chatbot is ready! <Link to="/test-client" state={appState}>Test it now &rarr;</Link></p>
                </div>
            )}

            <div className="step">
                <h2>Maintenance</h2>
                <button className="btn warn" onClick={handleClearData} disabled={status.clear.processing}>Clear All Data</button>
                <Status {...status.clear} />
            </div>
        </div>
    );
};

export default AdminPage;
