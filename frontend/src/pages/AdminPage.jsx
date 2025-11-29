import React, { useState, useCallback, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import '../styles/AdminPage.css';

const API_BASE_URL = 'https://icas00-docchat.hf.space';

const Status = ({ message, isProcessing, isError }) => {
    if (!message) return <div className="status">&nbsp;</div>;
    const className = `status ${isProcessing ? 'processing' : ''} ${isError ? 'error' : ''}`;
    return <div className={className}>{message}</div>;
};

const AdminPage = () => {
    const { state } = useLocation();
    const navigate = useNavigate();
    
    const [clientId, setClientId] = useState(null);
    const [apiKey, setApiKey] = useState(null);
    const [adminKey, setAdminKey] = useState(null);

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

    useEffect(() => {
        if (state?.clientId && state?.apiKey && state?.adminKey) {
            setClientId(state.clientId);
            setApiKey(state.apiKey);
            setAdminKey(state.adminKey);
        } else {
            // If no keys are provided, redirect back to the landing page
            navigate('/');
        }
    }, [state, navigate]);

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
            const response = await fetch(`${API_BASE_URL}/api/clients/${clientId}/documents`, {
                method: 'POST',
                headers: { 'X-Admin-Key': adminKey },
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
            const response = await fetch(`${API_BASE_URL}/api/clients/${clientId}/index`, {
                method: 'POST',
                headers: { 'X-Admin-Key': adminKey },
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
            const response = await fetch(`${API_BASE_URL}/api/clients/${clientId}/data`, {
                method: 'DELETE',
                headers: { 'X-Admin-Key': adminKey },
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
            const response = await fetch(`${API_BASE_URL}/api/clients/${clientId}/settings`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-Admin-Key': adminKey,
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
        const code = `<script src="${API_BASE_URL}/widget.js" data-api-key="${apiKey}" defer></script>`;
        navigator.clipboard.writeText(code);
    };

    if (!clientId) {
        return null; // Or a loading spinner
    }

    return (
        <div className="admin-container">
            <h1>DocuChat Admin</h1>
            {/* ... Rest of the JSX ... */}
        </div>
    );
};

export default AdminPage;
