# Embeddable AI Chat Widget - Backend

This project is the backend service for a multi-tenant, embeddable AI chat widget platform. It is a Spring Boot application designed to provide a RAG (Retrieval-Augmented Generation) pipeline, allowing multiple clients to have a custom chatbot on their websites powered by their own knowledge base documents.

## Architecture Overview

The backend is built on a streamlined, API-first architecture using Java and Spring Boot. It is designed to be a single, multi-tenant application that can serve thousands of clients simultaneously while keeping their data completely isolated.

- **Technology Stack:** Java 17, Spring Boot 3, Spring Data JPA, Hibernate, Flyway, PostgreSQL (with **pgvector**), Apache PDFBox (for PDF processing), Caffeine (for caching).
- **Multi-Tenancy:** Data is partitioned by a `clientId`. Each client has its own set of documents and embeddings, which are accessed via a unique `apiKey`.
- **RAG Pipeline:** The core logic involves a two-step AI process:
    1.  **Retrieval:** When a user asks a question, the system generates a vector embedding of the question and uses a cosine similarity search (via pgvector) to find the most relevant documents from the client's specific knowledge base.
    2.  **Generation:** The relevant documents and the user's question are then passed to a generative AI model (defaulting to OpenRouter's GPT-4o-mini) to synthesize a natural, human-like answer based only on the provided sources.
- **Optimization:**
    - **Batch Processing:** Embedding generation is batched (e.g., 50 chunks at a time) to minimize API calls and improve performance.
    - **Caching:** Query embeddings are cached using Caffeine to reduce API calls and latency for repeated queries.
    - **Buffer Size:** `WebClient` is configured with a 16MB buffer to handle large batch responses.

---

## Setup and Configuration

### Prerequisites

- Java (JDK 17+)
- Apache Maven
- An IDE (like IntelliJ IDEA or VS Code)
- API keys from your chosen AI providers (e.g., OpenRouter).
- PostgreSQL database with `pgvector` extension installed.

### Environment Variables

The application is configured via environment variables (which override `application.yml`). For local development, these should be set in your IDE's Run Configuration.

1.  `REMOTE_CHAT_KEY`
    - **Purpose:** The API key for your generative chat model (e.g., OpenRouter).
    - **Value:** `your_actual_api_key_here`

2.  `REMOTE_EMBEDDING_KEY`
    - **Purpose:** The API key for your embedding model (e.g., OpenRouter/Mistral).
    - **Value:** `your_actual_api_key_here`

3.  `APP_ADMIN_KEY`
    - **Purpose:** The master key for system-wide administrative actions (like "Nuke System").
    - **Value:** `your_secure_admin_key` (Defaults to `demo-secret-key` if not set).

4.  `REMOTE_CHAT_MODEL`
    - **Purpose:** The model identifier for your chat provider.
    - **Value:** `openai/gpt-4o-mini` (default) or other models supported by your provider.

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

These endpoints are for your use as the platform administrator.

#### 1. Create Client
- **URL:** `POST /api/clients/create`
- **Body:** None (Creates a default "New Client")
- **Success Response:** `201 Created` with JSON containing `id`, `apiKey`, and `adminKey`.

#### 2. Upload Documents for a Client

- **URL:** `POST /api/clients/{clientId}/documents`
- **Body:** Multipart File Upload (`file`). Supports `.txt` and `.pdf` files (text is automatically extracted).
- **Success Response:** `200 OK` with text "Document uploaded successfully...".

#### 3. Index Documents for a Client

- **URL:** `POST /api/clients/{clientId}/index`
- **Body:** None
- **Success Response:** `200 OK` with text "Indexing started for client {clientId}".

#### 4. Update Client Settings
- **URL:** `PUT /api/clients/{clientId}/settings`
- **Body:**
  ```json
  {
      "chatbotName": "My Bot",
      "welcomeMessage": "Hello!",
      "widgetColor": "#000000"
  }
  ```
- **Success Response:** `200 OK` with text "Settings updated successfully."

#### 5. Clear Client Data

- **URL:** `DELETE /api/clients/{clientId}/data`
- **Success Response:** `200 OK` with count of deleted documents.

#### 6. Nuke System (Super Admin)

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

#### 2. Get Widget Settings
- **URL:** `GET /api/widget/settings?apiKey=CLIENT_API_KEY`
- **Success Response:** `200 OK` with JSON:
  ```json
  {
      "chatbotName": "My Bot",
      "welcomeMessage": "Hello!",
      "widgetColor": "#000000"
  }
  ```

---

## Local Testing Workflow

To test the entire system from scratch after starting the application:

1.  **Open Test Client:** Open `test-client.html` in your browser.
2.  **Create Client:** (If not hardcoded) Use the API to create a client and get the ID/Keys.
3.  **Clear Data:** Enter Client ID (e.g., 50) and Admin Key, then click "Clear Data" to ensure a clean state.
4.  **Upload Documents:** Use `curl` or Postman to upload a text file to `/api/clients/{clientId}/documents`.
5.  **Index Documents:** Trigger indexing via `/api/clients/{clientId}/index`. Watch logs for "Processing batch..." messages.
6.  **Test Chat:** Use the chat interface in `test-client.html` to ask questions and verify answers.
7.  **Nuke System:** (Optional) Use the "Nuke System" button with the System Admin Key to wipe everything.
