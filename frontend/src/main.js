const API_BASE = 'http://localhost:8080';

const messagesEl  = document.getElementById('messages');
const inputEl     = document.getElementById('user-input');
const sendBtn     = document.getElementById('send-btn');
const modelSelect = document.getElementById('model-select');

const history = [];

function init() {
  showEmptyState();
  loadModels();
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

async function loadModels() {
  try {
    const res = await fetch(`${API_BASE}/api/models`);
    if (!res.ok) return;
    const models = await res.json();

    const previousValue = modelSelect.value;
    modelSelect.innerHTML = '';

    const groups = {};
    for (const m of models) {
      if (!groups[m.provider]) groups[m.provider] = [];
      groups[m.provider].push(m);
    }

    const providerLabels = { anthropic: 'Claude', openai: 'OpenAI', ollama: 'Local (Ollama)' };
    for (const [provider, items] of Object.entries(groups)) {
      const group = document.createElement('optgroup');
      group.label = providerLabels[provider] || provider;
      for (const m of items) {
        const opt = document.createElement('option');
        opt.value = m.id;
        opt.textContent = '✦ ' + m.name;
        group.appendChild(opt);
      }
      modelSelect.appendChild(group);
    }

    if ([...modelSelect.options].some(o => o.value === previousValue)) {
      modelSelect.value = previousValue;
    }
  } catch (e) {
    // Backend not running yet; static fallback options in HTML remain
  }
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
  el.innerHTML = `
    <div class="message-role">${role === 'user' ? 'You' : 'mAId'}</div>
    <div class="message-bubble">${escapeHtml(content)}</div>`;
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
  el.querySelector('.message-bubble').textContent = text;
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
