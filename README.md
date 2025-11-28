---
title: DocChat Platform
emoji: 🚀
colorFrom: red
colorTo: pink
sdk: docker
app_port: 7860
---
# Embeddable AI Chat Widget - Backend

This project is the backend service for a multi-tenant, embeddable AI chat widget platform. It is a Spring Boot application designed to provide a RAG (Retrieval-Augmented Generation) pipeline, allowing multiple clients to have a custom chatbot on their websites powered by their own knowledge base documents.

## Live Demo & Admin Dashboard

This project demonstrates a clear, two-page workflow for managing and testing your AI chatbot.

1.  **Admin Dashboard:** Manage your documents and trigger indexing.
    **[http://localhost:8080/admin.html](http://localhost:8080/admin.html)**

2.  **Live Test Page:** Interact with your chatbot on a simulated client website.
    **[http://localhost:8080/test-client.html](http://localhost:8080/test-client.html)**

**Workflow:**
1.  **Go to the Admin Dashboard.**
2.  **Upload a Document:** Use the "Upload Document" section to select a `.txt` or `.md` file (e.g., `Gym.txt`).
3.  **Index the Document:** Click the "Make AI Smart (Index)" button. The backend will process the document and create embeddings.
4.  **Test the Chatbot:** Click the provided link on the Admin Dashboard to go to the Live Test Page. The chat widget will appear. Ask questions based on the document you just uploaded.

---

## Architecture Overview

- **Technology Stack:** Java 21, Spring Boot 3, Spring Data JPA, Hibernate, Flyway, H2 (for local dev), MySQL (for production).
- **Multi-Tenancy:** Data is partitioned by a `clientId`. Each client has its own set of documents and embeddings, which are accessed via a unique `apiKey`.
- **RAG Pipeline:** The core logic involves a two-step AI process:
    1.  **Retrieval:** When a user asks a question, the system generates a vector embedding of the question and uses a cosine similarity search to find the most relevant documents from the client's specific knowledge base.
    2.  **Generation:** The relevant documents and the user's question are then passed to a generative AI model to synthesize a natural, human-like answer based only on the provided sources.

---

## Setup and Configuration

### Prerequisites

- Java (JDK 21+)
- Apache Maven
- An IDE (like IntelliJ IDEA or VS Code)
- API keys from your chosen AI providers.

### Environment Variables

For local development, these should be set in your IDE's Run Configuration.

1.  `REMOTE_CHAT_KEY`: Your API key for the chat model (e.g., Groq).
2.  `REMOTE_EMBEDDING_KEY`: Your API key for the embedding model (e.g., OpenRouter).

---

## Running the Application

1.  Set the environment variables listed above in your IDE's Run Configuration.
2.  Run the `AiAssistantApplication.java` file, or use the Maven command:
    ```sh
    mvn spring-boot:run
    ```
The application will start on `http://localhost:8080`.

---

## Deployment to Hugging Face Spaces

This backend application can be easily deployed to Hugging Face Spaces as a Docker Space.

1.  **Create a New Space:**
    - Go to [Hugging Face Spaces](https://huggingface.co/spaces).
    - Click "Create new Space".
    - Choose "Docker" as the Space SDK.
    - Select "Private" or "Public" as desired.

2.  **Push Your Code:**
    - Clone your new Space's Git repository locally.
    - Copy your entire `backend` project folder into this cloned repository.
    - Commit and push your code.

3.  **Configure Environment Variables:**
    - In your Hugging Face Space settings, navigate to "Space settings" -> "Repository secrets".
    - Add the following secrets:
        - `REMOTE_CHAT_KEY`: Your Groq API Key.
        - `REMOTE_EMBEDDING_KEY`: Your OpenRouter API Key.
        - `APP_ADMIN_KEY`: `demo-secret-key` (or your chosen admin key).
        - `JWT_SECRET`: `any_random_secret_string_at_least_32_chars_long` (for future admin dashboard).
    - **Important:** Ensure `REMOTE_CHAT_ENDPOINT` and `REMOTE_EMBEDDING_ENDPOINT` are correctly set in your `application.yml` (as they are now).

4.  **Build and Deploy:**
    - Hugging Face will automatically detect your `Dockerfile` and build the application.
    - The application will be accessible at your Space's URL (e.g., `https://your-username-your-space-name.hf.space`).

### Security Notes for Deployment

-   **CORS:** The `SecurityConfig.java` is intentionally configured to allow `*` for `AllowedOrigins`. This is crucial for the embeddable `widget.js` to function on any client website.
-   **API Keys:** All sensitive API keys are managed via environment variables (secrets) and are not hardcoded in the application.
