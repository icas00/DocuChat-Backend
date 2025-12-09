---
title: DocChat Platform
emoji: 🚀
colorFrom: red
colorTo: pink
sdk: docker
app_port: 7860
---

# DocChat Engine

A multi-tenant, embeddable AI chat platform. This backend powers custom RAG (Retrieval-Augmented Generation) chatbots that can be embedded on any website, letting users chat with their own documents.

## Live Demo

Try the full workflow here:

- **[Admin Dashboard](https://botforge-engine.onrender.com/)**: Upload docs and train the AI.
- **[Client Widget Demo](https://botforge-engine.onrender.com/)**: See the chat widget in action on a sample site.

**How to test:**
1. Go to the **Admin Dashboard**.
2. Upload a simple `.txt` or `.pdf` file (e.g., notes or a FAQ).
3. Click **"Make AI Smart (Index)"**.
4. Jump to the **Client Widget Demo** and ask questions about your file.

---

## Technical Overview

Built with **Java 21** and **Spring Boot 3**.

- **RAG Pipeline**: Vectorizes user queries to find relevant snippets from uploaded docs before generating an answer.
- **Multi-Tenancy**: Supports multiple clients (tenants) securely. Data is isolated by `clientId`.
- **Database**: H2 (local) / MySQL (prod). Uses `vector` columns for embeddings.

### Key Components
- **Retrieval**: Cosine similarity search for semantic relevance.
- **Generation**: Feeds context + query to an LLM (via Groq/OpenRouter) for a natural response.

---

## Local Setup

### Prerequisites
- JDK 21+
- Maven
- API Keys (Groq for chat, OpenRouter for embeddings)

### Configuration
Set these environment variables in your IDE or `.env`:

```bash
REMOTE_CHAT_KEY=<your_groq_key>
REMOTE_EMBEDDING_KEY=<your_openrouter_key>
```

### Run
```bash
mvn spring-boot:run
```
Server starts at `http://localhost:8080`.

---

## Deployment (Hugging Face)

Designed to run as a **Docker Space**.

1. Create a new Space (Docker SDK).
2. Copy the contents of this repo.
3. Add secrets in Space Settings:
   - `REMOTE_CHAT_KEY`
   - `REMOTE_EMBEDDING_KEY`
   - `APP_ADMIN_KEY` (e.g. `my-secret-key`)
4. Deploy.

> **Note**: CORS is open (`*`) by default to allow the widget to load on any client domain.
