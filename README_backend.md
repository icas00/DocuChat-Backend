# Embeddable AI Chat Widget - Backend

This project is the backend service for a multi-tenant, embeddable AI chat widget platform. It is a Spring Boot application designed to provide a RAG (Retrieval-Augmented Generation) pipeline, allowing multiple clients to have a custom chatbot on their websites powered by their own knowledge base documents.

## Architecture Overview

The backend is built on a streamlined, API-first architecture using Java and Spring Boot. It is designed to be a single, multi-tenant application that can serve thousands of clients simultaneously while keeping their data completely isolated.

- **Technology Stack:** Java 21, Spring Boot 3, Spring Data JPA, Hibernate, Flyway, PostgreSQL (with **pgvector**).
- **Multi-Tenancy:** Data is partitioned by a `clientId`. Each client has its own set of documents and embeddings, which are accessed via a unique `apiKey`.
- **RAG Pipeline:** The core logic involves a two-step AI process:
    1.  **Retrieval:** When a user asks a question, the system generates a vector embedding of the question and uses a cosine similarity search (via pgvector) to find the most relevant documents from the client's specific knowledge base.
    2.  **Generation:** The relevant documents and the user's question are then passed to a generative AI model (like Groq's Llama 3.1) to synthesize a natural, human-like answer based only on the provided sources.
- **Optimization:**
    - **Batch Processing:** Embedding generation is batched (e.g., 50 chunks at a time) to minimize API calls and improve performance.
    - **Buffer Size:** `WebClient` is configured with a 16MB buffer to handle large batch responses.

---

## Setup and Configuration

### Prerequisites

- Java (JDK 21+)
- Apache Maven
- An IDE (like IntelliJ IDEA or VS Code)
- API keys from your chosen AI providers (e.g., Groq for chat, Mistral for embeddings).
- PostgreSQL database with `pgvector` extension installed.

### Environment Variables

The application is configured via environment variables. For local development, these should be set in your IDE's Run Configuration.

1.  `REMOTE_CHAT_KEY`
    - **Purpose:** The API key for your generative chat model (e.g., Groq).
    - **Value:** `your_actual_groq_api_key_here`

2.  `REMOTE_EMBEDDING_KEY`
    - **Purpose:** The API key for your embedding model (e.g., Mistral AI).
    - **Value:** `your_actual_mistral_api_key_here`

3.  `APP_ADMIN_KEY`
    - **Purpose:** The master key for system-wide administrative actions (like "Nuke System").
    - **Value:** `your_secure_admin_key` (Defaults to `demo-secret-key` if not set).

4.  `REMOTE_CHAT_MODEL`
    - **Purpose:** The model identifier for your chat provider.
    - **Value:** `llama-3.1-8b-instant` (or any other current model from your provider).

---

## Running the Application

### Local Development

1.  Set the environment variables listed above in your IDE's Run Configuration.
2.  Run the `AiAssistantApplication.java` file, or use the Maven command in your terminal:
    ```sh
    mvn spring-boot:run
    ```
The application will start on `http://localhost:8080`.

### Docker Deployment

The project includes a multi-stage `Dockerfile` optimized for deployment on platforms like Hugging Face Spaces or any other container service.

1.  Ensure you have a `.env` file in the `backend` directory containing the required environment variables.
2.  Build and run the Docker container:
    ```sh
    docker build -t ai-assistant-backend .
    docker run -p 8080:7860 --env-file .env ai-assistant-backend
    ```

---

## API Endpoints

The API is split into two parts: a secured Admin API for managing clients and a Public API for the chat widget.

### Admin API

These endpoints are for your use as the platform administrator. They are protected and require a secret admin key to be passed in the `X-Admin-Key` header.

#### 1. Upload Documents for a Client

- **URL:** `POST /api/clients/{clientId}/documents`
- **Headers:**
  - `X-Admin-Key`: `client_admin_key` (Found in DB or returned on creation)
- **Body:** Multipart File Upload (`file`)
- **Success Response:** `200 OK` with text "Document uploaded successfully...".

#### 2. Index Documents for a Client

- **URL:** `POST /api/clients/{clientId}/index`
- **Headers:**
  - `X-Admin-Key`: `client_admin_key`
- **Body:** None
- **Success Response:** `200 OK` with text "Indexing started for client {clientId}".

#### 3. Clear Client Data

- **URL:** `DELETE /api/clients/{clientId}/data`
- **Headers:**
  - `X-Admin-Key`: `client_admin_key`
- **Success Response:** `200 OK` with count of deleted documents.

#### 4. Nuke System (Super Admin)

- **URL:** `DELETE /api/clients/admin/data`
- **Headers:**
  - `X-Admin-Key`: `APP_ADMIN_KEY` (System Admin Key)
- **Success Response:** `200 OK` confirming all system data has been wiped.

### Public Widget API

This endpoint is used by the frontend JavaScript widget and is publicly accessible.

#### 1. Stream Chat Response

- **URL:** `POST /api/widget/stream-chat`
- **Body:**
  ```json
  {
      "apiKey": "CLIENT_API_KEY",
      "message": "Are you taking new patients?",
      "history": ["User: When can I visit?", "Assistant: We are open from 9 AM to 5 PM."]
  }
  ```
- **Success Response:** Server-Sent Events (SSE) stream of the answer.

---

## Local Testing Workflow

To test the entire system from scratch after starting the application:

1.  **Open Test Client:** Open `test-client.html` in your browser.
2.  **Clear Data:** Enter Client ID (e.g., 50) and Admin Key, then click "Clear Data" to ensure a clean state.
3.  **Upload Documents:** Use `curl` or Postman to upload a text file to `/api/clients/50/documents`.
4.  **Index Documents:** Trigger indexing via `/api/clients/50/index`. Watch logs for "Processing batch..." messages.
5.  **Test Chat:** Use the chat interface in `test-client.html` to ask questions and verify answers.
6.  **Nuke System:** (Optional) Use the "Nuke System" button with the System Admin Key to wipe everything.
