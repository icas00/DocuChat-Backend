# AI Assistant Platform - Technical Documentation

## 1. Executive Summary
The AI Assistant Platform is a specialized Retrieval-Augmented Generation (RAG) system designed to provide accurate, context-aware AI responses based on private organizational documents. Unlike generic Large Language Models (LLMs) that rely solely on pre-trained "world knowledge," this system ingests, indexes, and semantically searches a curated knowledge base to answer user queries with high precision and verifiable sources.

## 2. System Architecture

The system follows a modern microservices-ready architecture using Spring Boot for the backend and PostgreSQL with `pgvector` for high-performance vector similarity search.

### 2.1 High-Level Data Flow

```mermaid
graph TD
    User[User / Widget] -->|1. Question| API[Spring Boot API]
    API -->|2. Generate Embedding| Model[Embedding Model API]
    Model -->|3. Vector| API
    API -->|4. Similarity Search| DB[(PostgreSQL + pgvector)]
    DB -->|5. Top K Chunks| API
    API -->|6. Context + Question| LLM[Chat Model API (GPT-4)]
    LLM -->|7. Streaming Response| API
    API -->|8. Answer| User
```

### 2.2 Core Components
- **API Server**: Spring Boot application handling REST requests, document parsing (PDF/Text), and orchestration.
- **Vector Store**: PostgreSQL with the `pgvector` extension. Stores document text alongside 768-dimensional or 1536-dimensional embedding vectors.
- **Model Adapter**: A flexible interface pattern to switch between different AI providers (e.g., OpenAI, Azure, Local models).
- **Client Widget**: A lightweight JavaScript embeddable widget for frontend integration.

## 3. Technology Stack

### Backend
- **Language**: Java 21 (LTS)
- **Framework**: Spring Boot 3.2+
- **Build Tool**: Maven
- **Database Access**: Spring Data JPA / Hibernate
- **Reactive Streams**: Project Reactor (Flux/Mono) for streaming AI responses.

### Data & Infrastructure
- **Database**: PostgreSQL 15+
- **Vector Engine**: `pgvector` v0.5.0+
- **Containerization**: Docker & Docker Compose

## 4. API Reference

### 4.1 Client Management
**Create Client**
- **Endpoint**: `POST /api/clients/create`
- **Description**: Registers a new tenant/client and generates API keys.
- **Response**:
  ```json
  {
      "id": 1,
      "apiKey": "distinct_client_key...",
      "adminKey": "admin_management_key..."
  }
  ```

### 4.2 Document Management
**Upload Document**
- **Endpoint**: `POST /api/clients/{clientId}/documents`
- **Content-Type**: `multipart/form-data`
- **Param**: `file` (File Object - .pdf or .txt)
- **Description**: Uploads a document, parses text (extracting from PDF if needed), and stores raw content.

**Trigger Indexing**
- **Endpoint**: `POST /api/clients/{clientId}/index`
- **Description**: Asynchronous trigger to chunk pending documents, generate embeddings, and store vectors.
- **Response**: 200 OK

**Clear Data**
- **Endpoint**: `DELETE /api/clients/{clientId}/data`
- **Description**: Hard deletes all documents and embeddings for a specific client.

### 4.3 Chat Interface
**Streaming Chat**
- **Endpoint**: `GET /api/chat/stream`
- **Query Params**:
  - `key`: Client API Key
  - `message`: User's question
  - `history`: (Optional) Previous conversation context
- **Response**: Server-Sent Events (SSE) stream of the AI answer.

## 5. Configuration (Environment Variables)

The application is configured via `application.yml` or Environment Variables.

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `SERVER_PORT` | Port for the API server | No | 8080 |
| `DATABASE_URL` | JDBC URL for Postgres | Yes | - |
| `DATABASE_USERNAME` | Database User | Yes | - |
| `DATABASE_PASSWORD` | Database Password | Yes | - |
| `REMOTE_CHAT_KEY` | API Key for LLM (e.g., OpenAI/OpenRouter) | Yes | - |
| `REMOTE_EMBEDDING_KEY` | API Key for Embedding Model | Yes | - |
| `APP_ADMIN_KEY` | Master key for system-wide admin actions | No | demo-secret-key |

## 6. Setup and Deployment

### 6.1 Prerequisites
1.  **PostgreSQL** installed with `vector` extension enabled:
    ```sql
    CREATE EXTENSION vector;
    ```
2.  **Java 21 JDK** installed.
3.  **Maven** installed.

### 6.2 Local Development
```bash
# 1. Clone Repository
git clone https://github.com/your-org/doc-chat.git

# 2. Configure Database/Keys
export DATABASE_URL=jdbc:postgresql://localhost:5432/ai_assistant
export REMOTE_CHAT_KEY=sk-...

# 3. Build & Run
mvn clean package
java -jar target/ai-assistant-0.0.1-SNAPSHOT.jar
```

### 6.3 Docker Deployment
The project includes a `Dockerfile` optimized for production (Eclipse Temurin JRE).

```bash
# Build
docker build -t ai-assistant .

# Run
docker run -p 8080:8080 \
  -e DATABASE_URL=... \
  -e REMOTE_CHAT_KEY=... \
  ai-assistant
```

## 7. Troubleshooting

- **"Type vector does not exist"**: Ensure you ran `CREATE EXTENSION vector;` in your Postgres database.
- **Connection Refused**: Check if your database container/service is running and accessible on the specified port.
- **Empty Responses**: Verify that documents have been indexed (`POST /index`) after uploading.
