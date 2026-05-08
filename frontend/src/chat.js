const API_BASE = 'http://localhost:8080';

let history = [];
let isLoading = false;

const chatArea = document.getElementById('chat-area');
const chatForm = document.getElementById('chat-form');
const userInput = document.getElementById('user-input');
const sendBtn = document.getElementById('send-btn');
const modelSelector = document.getElementById('model-selector');

function removeWelcome() {
    const welcome = document.getElementById('welcome');
    if (welcome) welcome.remove();
}

function appendMessage(role, content) {
    const bubble = document.createElement('div');
    bubble.className = `message ${role}`;

    const avatar = document.createElement('div');
    avatar.className = 'message-avatar';
    avatar.textContent = role === 'user' ? '👤' : '🌸';

    const text = document.createElement('div');
    text.className = 'message-text';
    text.textContent = content;

    bubble.appendChild(avatar);
    bubble.appendChild(text);
    chatArea.appendChild(bubble);
    chatArea.scrollTop = chatArea.scrollHeight;
    return text;
}

function setLoading(loading) {
    isLoading = loading;
    sendBtn.disabled = loading;
    userInput.disabled = loading;
    sendBtn.classList.toggle('loading', loading);
}

function showError(msg) {
    const error = document.createElement('div');
    error.className = 'error-message';
    error.textContent = msg;
    chatArea.appendChild(error);
    chatArea.scrollTop = chatArea.scrollHeight;
    setTimeout(() => error.remove(), 5000);
}

async function sendMessage(message) {
    const model = modelSelector.value;

    setLoading(true);
    removeWelcome();
    appendMessage('user', message);

    const typingText = appendMessage('assistant', '...');
    typingText.classList.add('typing');

    try {
        const response = await fetch(`${API_BASE}/api/chat`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ model, message, history }),
        });

        if (!response.ok) {
            throw new Error(`Server error: ${response.status}`);
        }

        const data = await response.json();

        typingText.classList.remove('typing');
        typingText.textContent = data.reply;

        history.push({ role: 'user', content: message });
        history.push({ role: 'assistant', content: data.reply });

    } catch (err) {
        typingText.parentElement.remove();
        showError(`Failed to get a response: ${err.message}`);
    } finally {
        setLoading(false);
    }
}

chatForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const message = userInput.value.trim();
    if (!message || isLoading) return;

    userInput.value = '';
    userInput.style.height = 'auto';
    await sendMessage(message);
});

userInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        chatForm.dispatchEvent(new Event('submit'));
    }
});

userInput.addEventListener('input', () => {
    userInput.style.height = 'auto';
    userInput.style.height = Math.min(userInput.scrollHeight, 150) + 'px';
});
