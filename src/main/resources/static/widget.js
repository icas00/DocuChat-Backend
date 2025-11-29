(function() {
    const scriptTag = document.currentScript;
    const apiKey = scriptTag.getAttribute('data-api-key');
    const scriptSrc = new URL(scriptTag.src);
    const backendUrl = scriptSrc.origin;

    if (!apiKey) {
        console.error("DocuChat: Missing data-api-key attribute.");
        return;
    }

    // --- Default Settings ---
    let settings = {
        widgetColor: '#e11d48',
        chatbotName: 'AI Assistant',
        welcomeMessage: 'Hi! How can I help you today?'
    };

    const host = document.createElement('div');
    document.body.appendChild(host);
    const shadowRoot = host.attachShadow({ mode: 'open' });

    const renderWidget = () => {
        shadowRoot.innerHTML = `
            <style>
                .docu-widget-btn {
                    background: ${settings.widgetColor};
                    /* ... other styles ... */
                }
                .docu-header {
                    background: #0f172a; color: white;
                    padding: 20px; font-weight: bold;
                    display: flex; align-items: center; gap: 10px;
                }
                .docu-send {
                    background: ${settings.widgetColor};
                    /* ... other styles ... */
                }
                /* ... all other styles ... */
            </style>
            
            <div class="docu-widget-btn">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
            </div>

            <div class="docu-widget-box">
                <div class="docu-header">
                    <div style="width:8px; height:8px; background:#4ade80; border-radius:50%"></div>
                    ${settings.chatbotName}
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

        // Re-query elements and attach event listeners after rendering
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
                const data = await res.json();
                addMessage(data.text, 'bot');
                chatHistory.push('Assistant: ' + data.text);
            } catch(e) {
                addMessage("Could not connect to server.", 'bot');
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

    // Fetch settings and then render the widget
    fetch(`${backendUrl}/api/widget/settings?apiKey=${apiKey}`)
        .then(response => response.json())
        .then(data => {
            if (data) {
                settings = { ...settings, ...data };
            }
        })
        .catch(error => console.error("DocuChat: Failed to fetch custom settings.", error))
        .finally(() => {
            renderWidget();
        });
})();
