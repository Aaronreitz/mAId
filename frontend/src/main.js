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

  const loadingEl = appendLoading();
  setLoading(true);

  try {
    const res = await fetch(`${API_BASE}/api/chat`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ model: modelSelect.value, messages: history }),
    });

    if (!res.ok) throw new Error((await res.text()) || `HTTP ${res.status}`);

    const data = await res.json();
    history.push({ role: 'assistant', content: data.content });
    loadingEl.remove();
    appendMessage('assistant', data.content);
  } catch (err) {
    loadingEl.remove();
    appendMessage('assistant', `Error: ${err.message}`, true);
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

function appendLoading() {
  const el = document.createElement('div');
  el.className = 'message assistant loading';
  el.innerHTML = `
    <div class="message-role">mAId</div>
    <div class="message-bubble">
      <div class="dots"><span></span><span></span><span></span></div>
    </div>`;
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
