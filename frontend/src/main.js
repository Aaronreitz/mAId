const API_BASE = 'http://localhost:8080';

const messagesEl  = document.getElementById('messages');
const inputEl     = document.getElementById('user-input');
const sendBtn     = document.getElementById('send-btn');
const modelSelect = document.getElementById('model-select');

const history = [];

function init() {
  showEmptyState();
  inputEl.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  });
  inputEl.addEventListener('input', () => {
    inputEl.style.height = 'auto';
    inputEl.style.height = Math.min(inputEl.scrollHeight, 160) + 'px';
  });
  sendBtn.addEventListener('click', sendMessage);
}

function showEmptyState() {
  messagesEl.innerHTML = `
    <div class="empty-state">
      <div class="empty-icon">✦</div>
      <p>At your service, Master. What shall we work on today?</p>
    </div>`;
}

async function sendMessage() {
  const content = inputEl.value.trim();
  if (!content || sendBtn.disabled) return;

  const emptyState = messagesEl.querySelector('.empty-state');
  if (emptyState) emptyState.remove();

  history.push({ role: 'user', content });
  appendMessage('user', content);

  inputEl.value = '';
  inputEl.style.height = 'auto';
  setLoading(true);

  const assistantEl = appendStreamingBubble();
  const bubbleEl = assistantEl.querySelector('.message-bubble');
  let accumulated = '';

  try {
    const response = await fetch(`${API_BASE}/api/chat/stream`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ model: modelSelect.value, messages: history }),
    });

    if (!response.ok) {
      throw new Error((await response.text()) || `HTTP ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder();
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split('\n');
      buffer = lines.pop(); // keep any incomplete trailing line

      for (const line of lines) {
        if (line.startsWith('data: ')) {
          const token = line.slice(6);
          if (token) {
            accumulated += token;
            bubbleEl.textContent = accumulated;
            assistantEl.scrollIntoView({ behavior: 'smooth', block: 'end' });
          }
        }
      }
    }

    assistantEl.classList.remove('streaming');
    history.push({ role: 'assistant', content: accumulated });
  } catch (err) {
    assistantEl.classList.remove('streaming');
    bubbleEl.textContent = `Error: ${err.message}`;
    assistantEl.classList.add('error');
  } finally {
    setLoading(false);
    inputEl.focus();
  }
}

function appendMessage(role, content, isError = false) {
  const el = document.createElement('div');
  el.className = `message ${role}${isError ? ' error' : ''}`;
  el.innerHTML = `
    <div class="message-role">${role === 'user' ? 'You' : 'mAId'}</div>
    <div class="message-bubble">${escapeHtml(content)}</div>`;
  messagesEl.appendChild(el);
  el.scrollIntoView({ behavior: 'smooth', block: 'end' });
  return el;
}

function appendStreamingBubble() {
  const el = document.createElement('div');
  el.className = 'message assistant streaming';
  el.innerHTML = `
    <div class="message-role">mAId</div>
    <div class="message-bubble"></div>`;
  messagesEl.appendChild(el);
  el.scrollIntoView({ behavior: 'smooth', block: 'end' });
  return el;
}

function setLoading(on) {
  sendBtn.disabled = on;
  inputEl.disabled = on;
}

function escapeHtml(str) {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;');
}

init();
