-- Enable pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;

-- Add vector column to embeddings table (1536 dimensions for OpenAI text-embedding-3-small)
ALTER TABLE embeddings ADD COLUMN IF NOT EXISTS vector_data_pgvector vector(1536);

-- Create index for fast cosine similarity search
-- Using IVFFlat index with 100 lists (good for ~10k-100k vectors)
CREATE INDEX IF NOT EXISTS embeddings_vector_idx 
ON embeddings USING ivfflat (vector_data_pgvector vector_cosine_ops) 
WITH (lists = 100);

-- Add comment for documentation
COMMENT ON COLUMN embeddings.vector_data_pgvector IS 'Vector embedding for similarity search using pgvector (1536 dimensions)';
