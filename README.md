# mAId

> Your personal AI maid — chat, create, imagine.

mAId is a self-hosted, modular AI interface that brings together the best of multiple AI models under one roof — with a personal touch.  
No more switching between tabs. No more copy-pasting between tools. Just one place for everything.

---

##  Features (Planned)

-  **Chat** — Talk to powerful LLMs (Claude, GPT, or local models via Ollama)
-  **Image Generation** — Generate images locally via ComfyUI + FLUX
-  **Hybrid Mode** — Seamlessly switch between local and cloud-based models
-  **Network Access** — Run on your home server, access from any device
-  **Extensible** — Designed to grow with new models and features over time

---

##  Architecture

```
Browser (any device)
       ↓
  mAId Frontend
       ↓
  mAId Backend (Quarkus)
       ↓
  ┌─────────────────────────┐
  │  Local (Home Server)    │
  │  Ollama  |  ComfyUI     │
  └─────────────────────────┘
       +
  Cloud APIs (Claude / GPT)
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Frontend | HTML / CSS / JavaScript |
| Backend | Java / Quarkus |
| Local LLM | Ollama |
| Image Generation | ComfyUI + FLUX |
| Cloud APIs | Anthropic Claude, OpenAI |

---

## 🚀 Roadmap

- [x] Project setup & repository structure
- [ ] Phase 1 — Basic chat interface with cloud API (Claude/GPT)
- [ ] Phase 2 — Ollama integration for local LLM support
- [ ] Phase 3 — Image generation via ComfyUI
- [ ] Phase 4 — Polish, animations & personality layer

---

## 📁 Project Structure

```
mAId/
├── README.md
├── .gitignore
├── frontend/
│   ├── index.html
│   ├── assets/
│   │   └── sprites/
│   └── src/
└── backend/
    └── (Quarkus project)
```

---

## 📄 License

MIT License — do whatever you want, just don't claim you built the maid.

---

*Built with curiosity, caffeine, and a soft spot for anime aesthetics.*
