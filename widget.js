(function () {
    const scriptTag = document.getElementById('docuchat-widget-script');
    if (!scriptTag) {
        console.error("DocuChat: Critical error - Cannot find the widget script tag.");
        return;
    }

    const apiKey = scriptTag.getAttribute('data-api-key');
    // Allow dynamic backend URL or fallback to script origin (if served from backend) or default
    const backendUrl = scriptTag.getAttribute('data-backend-url') || 'https://icas00-docchat.hf.space';

    if (!apiKey) {
        console.error("DocuChat: Missing data-api-key attribute. Widget will not load.");
        return;
    }

    let settings = {
        widgetColor: '#007aff',
        chatbotName: 'AI Assistant',
        welcomeMessage: 'Hi! How can I help you today?'
    };

    const host = document.createElement('div');
    host.id = 'docuchat-widget-host';
    document.body.appendChild(host);
    const shadowRoot = host.attachShadow({ mode: 'open' });

    const renderWidget = () => {
        shadowRoot.innerHTML = `
            <style>
                :host { all: initial; }
                .docu-widget-btn {
                    position: fixed; bottom: 40px; right: 40px;
                    width: 60px; height: 60px;
                    background: ${settings.widgetColor};
                    border-radius: 50%; cursor: pointer;
                    box-shadow: 0 10px 25px rgba(0,0,0,0.2);
                    display: flex; align-items: center; justify-content: center;
                    z-index: 9999; transition: transform 0.3s;
                    border: none;
                }
                .docu-widget-btn:hover { transform: scale(1.1); }
                .docu-widget-box {
                    position: fixed; bottom: 120px; right: 40px;
                    width: 400px; height: 600px;
                    background: white; border-radius: 20px;
                    box-shadow: 0 20px 50px rgba(0,0,0,0.2);
                    display: none; flex-direction: column;
                    z-index: 9999; overflow: hidden;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                }
                .docu-header {
                    background: #0f172a; color: white;
                    padding: 20px; font-weight: bold; font-size: 18px;
                    display: flex; align-items: center; gap: 10px;
                }
                .docu-messages {
                    flex: 1; padding: 15px; overflow-y: auto;
                    background: #f8fafc; display: flex; flex-direction: column; gap: 10px;
                }
                .docu-msg {
                    max-width: 80%; padding: 12px 16px; border-radius: 14px; font-size: 16px; line-height: 1.5;
                    position: relative;
                    word-wrap: break-word;
                    white-space: pre-wrap;
                }
                .docu-msg.bot { background: white; border: 1px solid #e2e8f0; align-self: flex-start; border-bottom-left-radius: 2px; color: #333; }
                .docu-msg.user { background: #0f172a; color: white; align-self: flex-end; border-bottom-right-radius: 2px; }
                .docu-msg.error { background: #fee2e2; color: #dc2626; border: 1px solid #fecaca; }
                
                .typing-indicator {
                    display: flex; gap: 4px; padding: 12px 16px; background: white; border: 1px solid #e2e8f0;
                    border-radius: 14px; align-self: flex-start; border-bottom-left-radius: 2px;
                    width: fit-content;
                }
                .typing-dot {
                    width: 6px; height: 6px; background: #94a3b8; border-radius: 50%;
                    animation: typing 1.4s infinite ease-in-out both;
                }
                .typing-dot:nth-child(1) { animation-delay: -0.32s; }
                .typing-dot:nth-child(2) { animation-delay: -0.16s; }
                
                @keyframes typing {
                    0%, 80%, 100% { transform: scale(0); }
                    40% { transform: scale(1); }
                }

                .docu-input-area {
                    padding: 15px; border-top: 1px solid #e2e8f0; display: flex; gap: 10px;
                }
                .docu-input {
                    flex: 1; padding: 12px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; font-size: 16px;
                }
                .docu-send {
                    background: ${settings.widgetColor};
                    color: white; border: none;
                    padding: 0 20px; border-radius: 8px; cursor: pointer; font-weight: bold; font-size: 16px;
                }
                .docu-send:disabled { opacity: 0.7; cursor: not-allowed; }
            </style>
            
            <div class="docu-widget-btn">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
            </div>

            <div class="docu-widget-box">
                <div class="docu-header">
                    <div style="width:8px; height:8px; background:#4ade80; border-radius:50%"></div>
                    <span id="chatbot-name">${settings.chatbotName}</span>
                </div>
                <div class="docu-messages">
                    <div class="docu-msg bot">${settings.welcomeMessage}</div>
                </div>
                <div class="docu-input-area">
                    <input class="docu-input" type="text" placeholder="Type a message..." />
                    <button class="docu-send">→</button>
                </div>
            </div>
        `;

        const widgetButton = shadowRoot.querySelector('.docu-widget-btn');
        const chatBox = shadowRoot.querySelector('.docu-widget-box');
        const messagesContainer = shadowRoot.querySelector('.docu-messages');
        const input = shadowRoot.querySelector('.docu-input');
        const sendButton = shadowRoot.querySelector('.docu-send');

        let chatHistory = [];
        let isOpen = false;

        widgetButton.onclick = () => {
            isOpen = !isOpen;
            chatBox.style.display = isOpen ? 'flex' : 'none';
            if (isOpen) input.focus();
        };

        const addTypingIndicator = () => {
            const div = document.createElement('div');
            div.className = 'typing-indicator';
            div.innerHTML = '<div class="typing-dot"></div><div class="typing-dot"></div><div class="typing-dot"></div>';
            messagesContainer.appendChild(div);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
            return div;
        };

        const sendMessage = async () => {
            const text = input.value.trim();
            if (!text) return;

            addMessage(text, 'user');
            chatHistory.push('User: ' + text);
            input.value = '';
            input.disabled = true;
            sendButton.disabled = true;

            const typingIndicator = addTypingIndicator();
            let botMessageElement = null;
            let fullBotResponse = '';

            try {
                const response = await fetch(`${backendUrl}/api/widget/stream-chat`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ apiKey: apiKey, message: text, history: chatHistory })
                });

                if (!response.body) throw new Error("Streaming not supported by the browser.");
                if (!response.ok) throw new Error(`Network error: ${response.status} ${response.statusText}`);

                const reader = response.body.getReader();
                const decoder = new TextDecoder();
                let isFirstChunk = true;

                while (true) {
                    const { done, value } = await reader.read();
                    if (done) break;

                    const chunk = decoder.decode(value, { stream: true });

                    // Remove typing indicator on first chunk
                    if (isFirstChunk) {
                        typingIndicator.remove();
                        botMessageElement = addMessage('▋', 'bot');
                        isFirstChunk = false;
                    }

                    // Process SSE format
                    const lines = chunk.split('\n');
                    for (const line of lines) {
                        if (line.startsWith('data:')) {
                            const data = line.substring(5);
                            if (data.trim() === '[DONE]') continue;
                            if (data) {
                                fullBotResponse += data;
                                botMessageElement.innerText = fullBotResponse + '▋';
                                messagesContainer.scrollTop = messagesContainer.scrollHeight;
                            }
                        } else if (line && !line.startsWith(':')) {
                            // Fallback for plain text
                            fullBotResponse += line;
                            botMessageElement.innerText = fullBotResponse + '▋';
                            messagesContainer.scrollTop = messagesContainer.scrollHeight;
                        }
                    }
                }

                if (botMessageElement) {
                    botMessageElement.innerText = fullBotResponse; // Final cleanup
                    chatHistory.push('Assistant: ' + fullBotResponse);
                }

            } catch (e) {
                if (typingIndicator) typingIndicator.remove();
                addMessage(`Sorry, an error occurred: ${e.message}`, 'error');
                console.error("DocuChat: Fetch stream error:", e);
            } finally {
                input.disabled = false;
                sendButton.disabled = false;
                input.focus();
            }
        };

        sendButton.addEventListener('click', sendMessage);
        input.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') sendMessage();
        });

        function addMessage(text, role) {
            const div = document.createElement('div');
            div.className = `docu-msg ${role}`;
            div.innerText = text;
            messagesContainer.appendChild(div);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
            return div;
        }
    };

    const updateWidgetSettings = (newSettings) => {
        const widgetButton = shadowRoot.querySelector('.docu-widget-btn');
        const sendButton = shadowRoot.querySelector('.docu-send');
        const chatbotName = shadowRoot.querySelector('#chatbot-name');

        if (widgetButton) widgetButton.style.background = newSettings.widgetColor;
        if (sendButton) sendButton.style.background = newSettings.widgetColor;
        if (chatbotName) chatbotName.innerText = newSettings.chatbotName;
    };

    renderWidget();

    fetch(`${backendUrl}/api/widget/settings?apiKey=${apiKey}`)
        .then(response => {
            if (!response.ok) throw new Error("Settings not found.");
            return response.json();
        })
        .then(data => {
            if (data) {
                settings = { ...settings, ...data };
                updateWidgetSettings(settings);
            }
        })
        .catch(error => console.error("DocuChat: Could not fetch custom settings. Using defaults.", error));
})();
