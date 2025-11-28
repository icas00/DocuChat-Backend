# Embeddable AI Chat Widget - Backend

This project is the backend service for a multi-tenant, embeddable AI chat widget platform. It is a Spring Boot application designed to provide a RAG (Retrieval-Augmented Generation) pipeline, allowing multiple clients to have a custom chatbot on their websites powered by their own knowledge base documents.

## Architecture Overview

The backend is built on a streamlined, API-first architecture using Java and Spring Boot. It is designed to be a single, multi-tenant application that can serve thousands of clients simultaneously while keeping their data completely isolated.

- **Technology Stack:** Java 21, Spring Boot 3, Spring Data JPA, Hibernate, Flyway, H2 (for local dev), MySQL (for production).
- **Multi-Tenancy:** Data is partitioned by a `clientId`. Each client has its own set of documents and embeddings, which are accessed via a unique `apiKey`.
- **RAG Pipeline:** The core logic involves a two-step AI process:
    1.  **Retrieval:** When a user asks a question, the system generates a vector embedding of the question and uses a cosine similarity search to find the most relevant documents from the client's specific knowledge base.
    2.  **Generation:** The relevant documents and the user's question are then passed to a generative AI model (like Groq's Llama 3.1) to synthesize a natural, human-like answer based only on the provided sources.

---

## Setup and Configuration

### Prerequisites

- Java (JDK 21+)
- Apache Maven
- An IDE (like IntelliJ IDEA or VS Code)
- API keys from your chosen AI providers (e.g., Groq for chat, Mistral for embeddings).

### Environment Variables

The application is configured via environment variables. For local development, these should be set in your IDE's Run Configuration.

1.  `REMOTE_CHAT_KEY`
    - **Purpose:** The API key for your generative chat model (e.g., Groq).
    - **Value:** `your_actual_groq_api_key_here`

2.  `REMOTE_EMBEDDING_KEY`
    - **Purpose:** The API key for your embedding model (e.g., Mistral AI).
    - **Value:** `your_actual_mistral_api_key_here`

3.  `JWT_SECRET`
    - **Purpose:** A secret key for signing JWTs. While not currently used for the widget, it's required for startup and reserved for a future admin dashboard.
    - **Value:** `any_random_secret_string_at_least_32_chars_long`

4.  `REMOTE_CHAT_MODEL`
    - **Purpose:** The model identifier for your chat provider.
    - **Value:** `llama-3.1-8b-instant` (or any other current model from your provider).

---

## Running the Application

### Local Development

The application is configured to run with an in-memory H2 database for local development.

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
  - `X-Admin-Key`: `your_admin_key` (defaults to `demo-secret-key` for local dev)
- **Body:**
  ```json
  {
      "entries": [
          {
              "question": "What are your hours?",
              "answer": "We are open from 9 AM to 5 PM."
          }
      ]
  }
  ```
- **Success Response:** `200 OK` with text "Documents uploaded successfully for client {clientId}".

#### 2. Index Documents for a Client

- **URL:** `POST /api/clients/{clientId}/index`
- **Headers:**
  - `X-Admin-Key`: `your_admin_key`
- **Body:** None
- **Success Response:** `200 OK` with text "Indexing started for client {clientId}".

### Public Widget API

This endpoint is used by the frontend JavaScript widget and is publicly accessible.

#### 1. Get Chat Response

- **URL:** `POST /api/widget/chat`
- **Body:**
  ```json
  {
      "apiKey": "TEST_KEY",
      "message": "Are you taking new patients?",
      "history": ["User: When can I visit?", "Assistant: We are open from 9 AM to 5 PM."]
  }
  ```
- **Success Response:** `200 OK` with a JSON body:
  ```json
  {
      "text": "Yes, we are always excited to welcome new patients to our family.",
      "sources": ["Do you accept new patients?"],
      "confidence": 0.9
  }
  ```

---

## Local Testing Workflow

To test the entire system from scratch after starting the application:

1.  **Upload Documents:** Send a `POST` request to `http://localhost:8080/api/clients/1/documents` with your FAQ data and the `X-Admin-Key` header.
2.  **Index Documents:** Send a `POST` request to `http://localhost:8080/api/clients/1/index` with the `X-Admin-Key` header.
3.  **Test Chat:** Send a `POST` request to `http://localhost:8080/api/widget/chat` with a question to verify the AI response.
4.  **Test Frontend:** Open the `test-client.html` file in your browser to interact with the live widget.
