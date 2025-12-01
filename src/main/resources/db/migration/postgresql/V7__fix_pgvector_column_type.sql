-- Keep the vector column as-is, don't change it
-- The issue is with Hibernate, not the database
-- We'll fix this in the Java code instead

-- Just ensure the index exists
CREATE INDEX IF NOT EXISTS embeddings_vector_idx 
ON embeddings USING ivfflat (vector_data_pgvector vector_cosine_ops) 
WITH (lists = 100);
