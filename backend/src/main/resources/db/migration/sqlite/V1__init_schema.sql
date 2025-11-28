-- This script defines the database schema for H2 (in dev mode).

CREATE TABLE clients (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    name TEXT NOT NULL,
    api_key TEXT NOT NULL UNIQUE,
    website_url TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE faq_docs (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    client_id INTEGER NOT NULL,
    question TEXT,
    answer TEXT NOT NULL,
    doc_type TEXT,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

CREATE TABLE embeddings (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    doc_id INTEGER NOT NULL,
    vector_data TEXT NOT NULL,
    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (doc_id) REFERENCES faq_docs(id) ON DELETE CASCADE
);
