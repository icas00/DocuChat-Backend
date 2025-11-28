-- This script defines the database schema for PostgreSQL.

CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    api_key TEXT NOT NULL UNIQUE,
    website_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE faq_docs (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL,
    question TEXT,
    answer TEXT NOT NULL,
    doc_type TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

CREATE TABLE embeddings (
    id SERIAL PRIMARY KEY,
    doc_id INTEGER NOT NULL,
    vector_data TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doc_id) REFERENCES faq_docs(id) ON DELETE CASCADE
);
