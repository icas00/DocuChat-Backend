-- Fix vector dimension to match sentence-transformers/multi-qa-mpnet-base-dot-v1 (768 dims)
-- Previous definition was 1536 (OpenAI standard), causing mismatch errors

-- Drop the index first as it depends on the column type
DROP INDEX IF EXISTS embeddings_vector_idx;

-- Alter the column type to vector(768) using a USING clause to handle conversion if needed
-- Since the column is likely empty or has invalid data, we can just cast or reset
ALTER TABLE embeddings 
ALTER COLUMN vector_data_pgvector TYPE vector(768) 
USING (vector_data_pgvector::text::vector(768));

-- Recreate the index with the correct dimension
CREATE INDEX IF NOT EXISTS embeddings_vector_idx 
ON embeddings USING ivfflat (vector_data_pgvector vector_cosine_ops) 
WITH (lists = 100);

-- Update comment
COMMENT ON COLUMN embeddings.vector_data_pgvector IS 'Vector embedding (768 dimensions for multi-qa-mpnet-base-dot-v1)';
