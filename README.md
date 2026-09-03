# BotForge — Low-Latency RAG Search Platform

Backend for **BotForge**, a low-latency Retrieval-Augmented Generation (RAG) search platform. [Live demo](https://botforge-engine.onrender.com/)

## Tech Stack
- **Java, Spring Boot, Spring WebFlux** — reactive backend
- **PostgreSQL with pgvector** — vector similarity search
- **Server-Sent Events (SSE)** — streaming responses
- **React 19** — frontend ([repo](https://github.com/icas00/DocChat-Frontend))

## Architecture
1. Documents are ingested and split into chunks, embedded in **batches of 50** to cut external embedding API calls by ~80%.
2. Embeddings are stored in PostgreSQL via **pgvector** for similarity search.
3. Queries run through a **semantic caching** layer before hitting the LLM, cutting inference latency by ~90% on cache hits.
4. Responses stream back to the client over **SSE** through a Spring WebFlux reactive pipeline, targeting <200ms perceived response time.

## Key Features
- Reactive, non-blocking request handling end-to-end
- Semantic cache to avoid redundant LLM calls
- Batched embedding ingestion pipeline
- Streaming chat responses via SSE

## Getting Started
```bash
./mvnw spring-boot:run
```
Configure your PostgreSQL (with the `pgvector` extension) and LLM/embedding API credentials via environment variables before running.

## Related
- Frontend: [DocChat-Frontend](https://github.com/icas00/DocChat-Frontend) — embeddable chat widget with Shadow DOM CSS isolation

## License
MIT — see [LICENSE](LICENSE).
