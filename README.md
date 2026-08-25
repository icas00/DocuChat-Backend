# DocuChat Backend

A multi-tenant AI document-chat backend built with Java 17 and Spring Boot. The application lets clients manage their own knowledge-base documents and expose an embeddable chat widget that retrieves relevant content and streams AI-generated answers.

## What it does

- Maintains separate client/tenant data using client-scoped API keys and database relationships.
- Accepts document uploads and converts supported content into FAQ/document records.
- Splits document content into smaller chunks before embedding.
- Generates text embeddings through an OpenAI-compatible embeddings API.
- Stores embeddings in PostgreSQL with `pgvector` and performs cosine-similarity retrieval.
- Builds an LLM prompt from the retrieved knowledge-base content and conversation history.
- Streams generated responses back to the browser using Spring WebFlux/Reactor.
- Provides an embeddable JavaScript chat widget with configurable name, color, and welcome message.
- Uses Flyway for database migrations and supports local/production configuration profiles.
- Includes Caffeine-based embedding caching and a simple semantic response cache.

## Architecture

```text
                 Client Website
                      |
                      v
              DocuChat Widget
                      |
              POST /api/widget/stream-chat
                      |
                      v
              +---------------+
              | Spring Boot   |
              | Controllers   |
              +-------+-------+
                      |
                      v
                Chat Service
                      |
          +-----------+-----------+
          |                       |
          v                       v
   Query Embedding          Client History
          |
          v
 PostgreSQL + pgvector
          |
   Cosine similarity
          |
          v
 Relevant FAQ/Documents
          |
          v
 OpenAI-compatible Chat API
          |
          v
    Streaming Flux<String>
          |
          v
       Browser
```

## RAG pipeline

The retrieval flow is implemented as:

1. A client uploads or manages knowledge-base content.
2. Content is split into smaller chunks by the document chunking service.
3. Chunks are sent to the configured embedding provider.
4. Embedding vectors are stored with the associated documents.
5. A user's question is converted into an embedding.
6. PostgreSQL/pgvector performs vector similarity search scoped to the client.
7. The retrieved document content is inserted into the LLM prompt as knowledge-base context.
8. The configured chat model generates the response.
9. The response is streamed to the widget instead of waiting for the complete answer.

## Multi-tenancy

Each `Client` has its own API/admin keys and associated documents. Retrieval queries are scoped by `client_id`, so the vector search is performed against the requesting client's knowledge base.

The client configuration also stores widget-specific settings such as:

- Chatbot name
- Welcome message
- Widget color
- Website URL

## Vector search

The PostgreSQL migrations enable the `pgvector` extension and create a vector column for embeddings.

The current migration history uses a 768-dimensional vector to match the configured `multi-qa-mpnet-base-dot-v1` embedding model and creates an IVFFlat index using cosine distance.

> Note: the migration history contains an earlier 1536-dimensional definition that was later changed to 768 dimensions when the embedding model changed.

## AI model integration

The backend uses an OpenAI-compatible HTTP API through Spring `WebClient`.

Two model operations are supported:

- Chat completion generation
- Text embedding generation

The provider, endpoint, API key, and model names are supplied through application configuration rather than hard-coded in the service.

## Response streaming

Chat responses are exposed as a reactive stream using Spring WebFlux and Reactor `Flux`. The browser widget reads the response body as a stream and progressively updates the displayed assistant message as new text arrives.

The remote model adapter also includes retry/backoff handling for remote model requests.

## Caching

The project contains two caching approaches:

- **Caffeine/Spring Cache:** embedding requests can be cached using `@Cacheable`.
- **Semantic response cache:** `CacheService` compares query embeddings using cosine similarity and returns a previously generated answer when the similarity reaches the configured threshold.

The semantic cache is an in-memory implementation and is intentionally simple; it does not provide distributed cache storage or a production eviction strategy.

## Database

The project uses Spring Data JPA/Hibernate for persistence and Flyway for schema migrations.

The main PostgreSQL data model includes:

```text
Client
  |
  +-- FaqDoc
        |
        +-- Embedding
```

PostgreSQL is the production database configuration used by the `prod` profile. H2 and other database drivers are also present in the Maven configuration for alternative/local setups.

## Security

The application uses Spring Security and an admin-key filter for protected administrative operations. Client API keys are used by the widget/client-facing flow to identify the tenant.

The repository also contains JWT dependencies, but JWT authentication is not the primary authentication mechanism implemented by the current code.

## Embedded widget

The backend serves a JavaScript widget from `src/main/resources/static/widget.js`.

The widget:

- Reads a client API key from its script tag.
- Loads client-specific widget settings.
- Uses Shadow DOM to isolate the widget UI.
- Maintains conversation history in the browser session.
- Sends questions to the streaming chat endpoint.
- Reads the streamed response using the browser `ReadableStream` API.

A typical integration uses a script tag similar to:

```html
<script
  id="docuchat-widget-script"
  src="https://YOUR-BACKEND-HOST/widget.js"
  data-api-key="YOUR_CLIENT_API_KEY">
</script>
```

Use the actual deployed backend URL and client API key for your environment.

## Tech stack

### Backend

- Java 17
- Spring Boot 3.2
- Spring MVC / REST
- Spring Data JPA / Hibernate
- Spring Security
- Spring WebFlux / Reactor
- Bean Validation

### AI / Retrieval

- OpenAI-compatible chat API
- OpenAI-compatible embeddings API
- PostgreSQL + pgvector
- Cosine-similarity retrieval
- RAG prompt construction

### Data / Infrastructure

- PostgreSQL
- Flyway
- Caffeine
- Docker
- Maven

### Frontend integration

- Vanilla JavaScript
- Shadow DOM
- Browser Fetch / ReadableStream APIs

## Configuration

The application reads model and database configuration from Spring configuration/environment variables. Do not commit API keys to the repository.

Typical model configuration includes:

```yaml
model:
  remote:
    chat:
      provider: <provider>
      endpoint: <chat-endpoint>
      key: <chat-api-key>
      model: <chat-model>
    embedding:
      provider: <provider>
      endpoint: <embedding-endpoint>
      key: <embedding-api-key>
      model: <embedding-model>
```

Production database configuration is provided through environment variables such as `SPRING_DATASOURCE_URL`.

## Run locally

### Prerequisites

- JDK 17
- Maven
- PostgreSQL with the `pgvector` extension for the PostgreSQL profile
- API credentials for the configured chat and embedding providers

### Start the application

```bash
mvn spring-boot:run
```

The default development configuration runs the application on port `8080`.

For a production-style deployment, activate the appropriate Spring profile and provide the required database/model environment variables.

## Deployment

The repository includes Docker and production configuration intended for deployment to a container-based environment. The current project also has a deployed backend associated with the repository.

## Project structure

```text
src/main/java/com/aiassistant/
├── adapter/        # Model provider abstraction and remote/local adapters
├── config/         # Spring, model, cache, WebClient and Flyway configuration
├── controller/     # REST and widget endpoints
├── dto/             # Request/response DTOs
├── model/           # JPA entities
├── repository/      # Spring Data repositories
├── security/       # Request authentication/filtering
└── service/        # Chat, embedding, caching and document processing

src/main/resources/
├── db/migration/    # Flyway database migrations
└── static/          # Embeddable chat widget
```

## Current scope

This repository is focused on the backend and embeddable widget. The codebase includes several optional dependencies and historical migration/configuration paths; the README intentionally documents the functionality that is represented by the current implementation rather than treating every declared dependency as an active feature.
