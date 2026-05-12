const API_BASE = 'http://localhost:8080';

const messagesEl  = document.getElementById('messages');
const inputEl     = document.getElementById('user-input');
const sendBtn     = document.getElementById('send-btn');
const modelSelect = document.getElementById('model-select');

const history = [];

marked.use({ breaks: true, gfm: true });

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

  const msgEl = appendStreamingMessage();

  try {
    const res = await fetch(`${API_BASE}/api/chat/stream`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ model: modelSelect.value, messages: history }),
    });

    if (!res.ok) throw new Error((await res.text()) || `HTTP ${res.status}`);

    const reader = res.body.getReader();
    const decoder = new TextDecoder();
    let fullText = '';
    let buffer = '';

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      // SSE events are delimited by double newline
      const events = buffer.split('\n\n');
      buffer = events.pop();
      for (const event of events) {
        const chunk = event.split('\n')
          .filter(l => l.startsWith('data: '))
          .map(l => l.slice(6))
          .join('\n');
        if (chunk) {
          fullText += chunk;
          updateStreamingMessage(msgEl, fullText);
        }
      }
    }
    // flush remaining buffer
    if (buffer) {
      const chunk = buffer.split('\n')
        .filter(l => l.startsWith('data: '))
        .map(l => l.slice(6))
        .join('\n');
      if (chunk) fullText += chunk;
    }

    finalizeStreamingMessage(msgEl, fullText);
    history.push({ role: 'assistant', content: fullText });
  } catch (err) {
    msgEl.remove();
    appendMessage('assistant', `Error: ${err.message}`, true);
  } finally {
    setLoading(false);
    inputEl.focus();
  }
}

function appendMessage(role, content, isError = false) {
  const el = document.createElement('div');
  el.className = `message ${role}${isError ? ' error' : ''}`;
  const isAssistant = role === 'assistant';
  const body = isAssistant ? marked.parse(content) : escapeHtml(content);
  const bubbleClass = isAssistant ? 'message-bubble markdown' : 'message-bubble';
  el.innerHTML = `
    <div class="message-role">${role === 'user' ? 'You' : 'mAId'}</div>
    <div class="${bubbleClass}">${body}</div>`;
  messagesEl.appendChild(el);
  el.scrollIntoView({ behavior: 'smooth', block: 'end' });
  return el;
}

function appendStreamingMessage() {
  const el = document.createElement('div');
  el.className = 'message assistant streaming';
  el.innerHTML = `
    <div class="message-role">mAId</div>
    <div class="message-bubble"><span class="cursor"></span></div>`;
  messagesEl.appendChild(el);
  el.scrollIntoView({ behavior: 'smooth', block: 'end' });
  return el;
}

function updateStreamingMessage(el, text) {
  const bubble = el.querySelector('.message-bubble');
  bubble.innerHTML = escapeHtml(text) + '<span class="cursor"></span>';
  el.scrollIntoView({ behavior: 'smooth', block: 'end' });
}

function finalizeStreamingMessage(el, text) {
  el.classList.remove('streaming');
  const bubble = el.querySelector('.message-bubble');
  bubble.classList.add('markdown');
  bubble.innerHTML = marked.parse(text);
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
