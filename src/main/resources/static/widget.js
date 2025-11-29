(function() {
    const scriptTag = document.currentScript;
    if (!scriptTag) {
        console.error("DocuChat: Cannot find current script tag.");
        return;
    }

    const apiKey = scriptTag.getAttribute('data-api-key');
    const scriptSrc = new URL(scriptTag.src);
    const backendUrl = scriptSrc.origin;

    if (!apiKey) {
        console.error("DocuChat: Missing data-api-key attribute.");
        return;
    }

    // --- Default Settings ---
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
                :host {
                    all: initial; /* Reset all inherited styles */
                }
                .docu-widget-btn {
                    position: fixed; bottom: 20px; right: 20px;
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
                    position: fixed; bottom: 100px; right: 20px;
                    width: 350px; height: 500px;
                    background: white; border-radius: 20px;
                    box-shadow: 0 20px 50px rgba(0,0,0,0.2);
                    display: none; flex-direction: column;
                    z-index: 9999; overflow: hidden;
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
                }
                .docu-header {
                    background: #0f172a; color: white;
                    padding: 20px; font-weight: bold;
                    display: flex; align-items: center; gap: 10px;
                }
                .docu-messages {
                    flex: 1; padding: 15px; overflow-y: auto;
                    background: #f8fafc; display: flex; flex-direction: column; gap: 10px;
                }
                .docu-msg {
                    max-width: 80%; padding: 10px 14px; border-radius: 14px; font-size: 14px; line-height: 1.4;
                }
                .docu-msg.bot { background: white; border: 1px solid #e2e8f0; align-self: flex-start; border-bottom-left-radius: 2px; color: #333; }
                .docu-msg.user { background: #0f172a; color: white; align-self: flex-end; border-bottom-right-radius: 2px; }
                .docu-input-area {
                    padding: 15px; border-top: 1px solid #e2e8f0; display: flex; gap: 10px;
                }
                .docu-input {
                    flex: 1; padding: 10px; border: 1px solid #e2e8f0; border-radius: 8px; outline: none; font-size: 14px;
                }
                .docu-send {
                    background: ${settings.widgetColor};
                    color: white; border: none;
                    padding: 0 15px; border-radius: 8px; cursor: pointer; font-weight: bold;
                }
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
        };

        const sendMessage = async () => {
            const text = input.value.trim();
            if(!text) return;

            addMessage(text, 'user');
            chatHistory.push('User: ' + text);
            input.value = '';

            try {
                const res = await fetch(`${backendUrl}/api/widget/chat`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ apiKey: apiKey, message: text, history: chatHistory })
                });
                if (!res.ok) throw new Error("Network response was not ok.");
                const data = await res.json();
                addMessage(data.text, 'bot');
                chatHistory.push('Assistant: ' + data.text);
            } catch(e) {
                addMessage("Sorry, I'm having trouble connecting. Please try again later.", 'bot');
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
        }
    };

    const updateWidgetSettings = (newSettings) => {
        const widgetButton = shadowRoot.querySelector('.docu-widget-btn');
        const sendButton = shadowRoot.querySelector('.docu-send');
        const chatbotName = shadowRoot.querySelector('#chatbot-name');
        
        if (widgetButton) widgetButton.style.background = newSettings.widgetColor;
        if (sendButton) sendButton.style.background = newSettings.widgetColor;
        if (chatbotName) chatbotName.innerText = newSettings.chatbotName;
        // Welcome message is only set on initial render, so no update needed here.
    };

    // 1. Render the widget immediately with default settings.
    renderWidget();

    // 2. Fetch custom settings and update the widget if they exist.
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
