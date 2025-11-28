(function() {
    const scriptTag = document.currentScript;
    const apiKey = scriptTag.getAttribute('data-api-key');
    // Our backend server URL.
    const backendUrl = 'http://localhost:8080'; 

    if (!apiKey) {
        console.error('AI Assistant: API key is missing.');
        return;
    }

    const host = document.createElement('div');
    host.id = 'ai-widget-container';
    document.body.appendChild(host);

    const shadowRoot = host.attachShadow({ mode: 'open' });

    shadowRoot.innerHTML = `
        <style>
            .chat-widget {
                position: fixed;
                bottom: 20px;
                right: 20px;
                width: 350px;
                height: 500px;
                border: 1px solid #ccc;
                border-radius: 10px;
                display: flex;
                flex-direction: column;
                font-family: sans-serif;
                box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                z-index: 9999;
            }
            .chat-header {
                background: #007bff;
                color: white;
                padding: 10px;
                border-top-left-radius: 10px;
                border-top-right-radius: 10px;
                text-align: center;
            }
            .chat-messages {
                flex-grow: 1;
                padding: 10px;
                overflow-y: auto;
                display: flex;
                flex-direction: column;
            }
            .message {
                margin-bottom: 10px;
                padding: 8px 12px;
                border-radius: 18px;
                max-width: 80%;
            }
            .user-message {
                background: #007bff;
                color: white;
                align-self: flex-end;
            }
            .assistant-message {
                background: #f1f1f1;
                color: black;
                align-self: flex-start;
            }
            .chat-input {
                display: flex;
                padding: 10px;
                border-top: 1px solid #ccc;
            }
            .chat-input input {
                flex-grow: 1;
                border: 1px solid #ccc;
                border-radius: 20px;
                padding: 8px 12px;
                margin-right: 10px;
            }
            .chat-input button {
                background: #007bff;
                color: white;
                border: none;
                border-radius: 20px;
                padding: 8px 15px;
                cursor: pointer;
            }
        </style>
        <div class="chat-widget">
            <div class="chat-header">AI Assistant</div>
            <div class="chat-messages"></div>
            <div class="chat-input">
                <input type="text" placeholder="Ask something...">
                <button>Send</button>
            </div>
        </div>
    `;

    const messagesContainer = shadowRoot.querySelector('.chat-messages');
    const input = shadowRoot.querySelector('.chat-input input');
    const sendButton = shadowRoot.querySelector('.chat-input button');
    let chatHistory = [];

    sendButton.addEventListener('click', sendMessage);
    input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });

    function sendMessage() {
        const messageText = input.value.trim();
        if (!messageText) return;

        appendMessage(messageText, 'user');
        chatHistory.push('User: ' + messageText);
        input.value = '';

        // Use the full, absolute URL for the API call.
        fetch(`${backendUrl}/api/widget/chat`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                apiKey: apiKey,
                message: messageText,
                history: chatHistory,
            }),
        })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            appendMessage(data.text, 'assistant');
            chatHistory.push('Assistant: ' + data.text);
        })
        .catch(error => {
            console.error('AI Assistant: Error fetching response:', error);
            appendMessage('Sorry, something went wrong.', 'assistant');
        });
    }

    function appendMessage(text, sender) {
        const messageDiv = document.createElement('div');
        messageDiv.classList.add('message', sender + '-message');
        messageDiv.textContent = text;
        messagesContainer.appendChild(messageDiv);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
})();
