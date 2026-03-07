document.addEventListener("DOMContentLoaded", () => {
    const chatContainers = document.querySelectorAll(".cmp-chatbot");

    chatContainers.forEach(chatContainer => {
        const apiEndpoint = chatContainer.dataset.apiEndpoint;
        const namespace = chatContainer.dataset.namespace || 'default';
        const messagesContainer = chatContainer.querySelector("#chatbot-messages");
        const inputField = chatContainer.querySelector("#chatbot-input");
        const sendBtn = chatContainer.querySelector("#chatbot-send-btn");
        const closeBtn = chatContainer.querySelector(".cmp-chatbot__close-btn");

        let isWaiting = false;

        // Persist sessionId across page navigations within the same tab
        if (!sessionStorage.getItem('chatbot-session-id')) {
            sessionStorage.setItem('chatbot-session-id', 'session-' + Date.now());
        }
        const sessionId = sessionStorage.getItem('chatbot-session-id');

        if (closeBtn) {
            closeBtn.addEventListener("click", () => {
                const container = chatContainer.querySelector(".cmp-chatbot__container");
                if (container) {
                    container.style.display = "none";
                }
            });
        }

        const appendMessage = (text, sender) => {
            const msgDiv = document.createElement("div");
            msgDiv.className = `cmp-chatbot__message cmp-chatbot__message--${sender}`;

            const contentDiv = document.createElement("div");
            contentDiv.className = "cmp-chatbot__message-content";
            contentDiv.innerText = text;

            msgDiv.appendChild(contentDiv);
            messagesContainer.appendChild(msgDiv);
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
            return msgDiv;
        };

        const renderSources = (sources) => {
            if (!sources || sources.length === 0) return '';

            let html = '<div class="cmp-chatbot__sources"><p class="cmp-chatbot__sources-title">Sources:</p><ul>';
            sources.forEach(source => {
                html += `<li><a href="${source.url}" target="_blank">${source.title || source.url}</a></li>`;
            });
            html += '</ul></div>';
            return html;
        };

        const sendMessage = async () => {
            const text = inputField.value.trim();
            if (!text || isWaiting) return;

            console.log('[Chatbot] Sending question:', text);
            appendMessage(text, "user");
            inputField.value = "";
            isWaiting = true;
            sendBtn.disabled = true;

            const thinkingMsg = appendMessage("Thinking...", "bot");
            thinkingMsg.classList.add("cmp-chatbot__message--thinking");

            try {
                const payload = {
                    question: text,
                    namespace: namespace,
                    sessionId: sessionId
                };

                const response = await fetch(apiEndpoint, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                if (!response.ok) throw new Error(`HTTP ${response.status}`);

                const reader = response.body.getReader();
                const decoder = new TextDecoder("utf-8");
                let fullAnswer = "";
                let botMessageElement = null;
                let textDiv = null;      // The <p> or div inside contentDiv for answer text
                let contentDiv = null;   // The .cmp-chatbot__message-content bubble
                let buffer = "";
                let sourcesAdded = false;

                // Helper: remove the thinking indicator and ensure a bot message box exists
                const ensureBotBox = () => {
                    if (thinkingMsg && thinkingMsg.parentNode) {
                        thinkingMsg.remove();
                    }
                    if (!botMessageElement) {
                        botMessageElement = appendMessage("", "bot");
                        contentDiv = botMessageElement.querySelector(".cmp-chatbot__message-content");
                        // Create a dedicated text node inside the bubble
                        textDiv = document.createElement("span");
                        textDiv.className = "cmp-chatbot__answer-text";
                        if (contentDiv) {
                            contentDiv.appendChild(textDiv);
                        }
                        console.log('[Chatbot] Created new bot message box');
                    }
                };

                while (true) {
                    const { value, done } = await reader.read();
                    if (done) break;

                    buffer += decoder.decode(value, { stream: true });
                    let lines = buffer.split('\n');
                    buffer = lines.pop(); // Keep partial line

                    for (const line of lines) {
                        const trimmedLine = line.trim();
                        if (!trimmedLine || trimmedLine === 'data: [DONE]') continue;

                        let dataStr = trimmedLine;
                        if (trimmedLine.startsWith('data:')) {
                            dataStr = trimmedLine.slice(5).trim();
                        }

                        try {
                            const parsed = JSON.parse(dataStr);
                            console.log('[Chatbot] SSE Event type:', parsed.type);

                            if (parsed.type === 'metadata') {
                                ensureBotBox();
                                // Add sources inside the content bubble (below the text area)
                                if (parsed.sources && parsed.sources.length > 0 && !sourcesAdded && contentDiv) {
                                    console.log('[Chatbot] Rendering sources:', parsed.sources.length);
                                    const sourcesHtml = renderSources(parsed.sources);
                                    const sourcesEl = document.createElement('div');
                                    sourcesEl.innerHTML = sourcesHtml;
                                    contentDiv.appendChild(sourcesEl);
                                    sourcesAdded = true;
                                }
                            } else if (parsed.type === 'token') {
                                ensureBotBox();
                                const tokenContent = parsed.content || "";
                                if (tokenContent) {
                                    fullAnswer += tokenContent;
                                    // Update the text span with the accumulated answer
                                    if (textDiv) {
                                        textDiv.innerText = fullAnswer;
                                    } else if (contentDiv) {
                                        // Fallback: no textDiv — update contentDiv directly (before sources)
                                        contentDiv.firstChild
                                            ? (contentDiv.firstChild.textContent = fullAnswer)
                                            : (contentDiv.innerText = fullAnswer);
                                    }
                                    messagesContainer.scrollTop = messagesContainer.scrollHeight;
                                }
                            } else if (parsed.type === 'error') {
                                ensureBotBox();
                                if (textDiv) textDiv.innerText = parsed.message || "An error occurred.";
                            }
                        } catch (e) {
                            console.warn('[Chatbot] Non-JSON or parse error, raw line:', dataStr, e);
                        }
                    }
                }

                // If we never got any tokens (empty response), show a fallback
                if (botMessageElement && !fullAnswer) {
                    if (textDiv) textDiv.innerText = "I couldn't generate a response. Please try again.";
                }

            } catch (error) {
                console.error("[Chatbot] Fatal Error:", error);
                if (thinkingMsg && thinkingMsg.parentNode) thinkingMsg.remove();
                appendMessage("Sorry, I encountered an error. Please check if the RAG service is running.", "bot");
            } finally {
                isWaiting = false;
                sendBtn.disabled = false;
                inputField.focus();
            }
        };

        if (sendBtn && inputField) {
            sendBtn.addEventListener("click", sendMessage);
            inputField.addEventListener("keypress", (e) => {
                if (e.key === "Enter") {
                    sendMessage();
                }
            });
        }
    });
});
