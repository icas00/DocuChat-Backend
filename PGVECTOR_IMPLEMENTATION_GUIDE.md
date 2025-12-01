# pgvector Integration - Phase 2 Implementation Guide

## ✅ COMPLETED STEPS:

### 1. Database Setup (DONE ✅)
You've successfully run these SQL commands in Neon:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
ALTER TABLE embeddings ADD COLUMN vector_data_pgvector vector(1536);
CREATE INDEX ON embeddings USING ivfflat (vector_data_pgvector vector_cosine_ops) WITH (lists = 100);
```

### 2. Dependencies Added (DONE ✅)
- Added pgvector dependency to pom.xml
- Created Flyway migration: V6__add_pgvector_support.sql

### 3. Model Updated (DONE ✅)
- Added `vectorDataPgvector` field to Embedding.java

### 4. Repository Updated (DONE ✅)
- Added `findTopKSimilarByClientId()` method to EmbeddingRepository.java

---

## 🔧 REMAINING STEPS:

### Step 1: Update EmbeddingService.java

Add this helper method at the end of the class (before the closing brace):

```java
/**
 * Convert float array to pgvector string format: "[0.1, 0.2, 0.3, ...]"
 */
private String vectorToPgVectorString(float[] vector) {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < vector.length; i++) {
        if (i > 0) sb.append(",");
        sb.append(vector[i]);
    }
    sb.append("]");
    return sb.toString();
}
```

### Step 2: Update the embedding storage (line ~66-69)

Change this:
```java
embedding.setVectorData(objectMapper.writeValueAsString(vector));
// Store chunk metadata in embedding (we'll add fields later)
```

To this:
```java
// Store in JSON format (backward compatibility)
embedding.setVectorData(objectMapper.writeValueAsString(vector));
// Also store in pgvector format for fast similarity search
embedding.setVectorDataPgvector(vectorToPgVectorString(vector));
```

### Step 3: Replace findRelevantDocs method (line ~93-131)

Replace the entire method with:
```java
public List<FaqDoc> findRelevantDocs(Long clientId, float[] queryVector, int k) {
    log.info("Finding relevant docs for client ID: {} using pgvector", clientId);

    if (queryVector == null || queryVector.length == 0) {
        log.error("Invalid query vector provided. Returning no documents.");
        return List.of();
    }

    // Limit K to 5 for optimal performance
    final int MAX_K = Math.min(k, 5);
    
    long startTime = System.currentTimeMillis();
    
    // Use pgvector for fast similarity search (20x faster than in-memory)
    String queryVectorString = vectorToPgVectorString(queryVector);
    List<Embedding> similarEmbeddings = embeddingRepository.findTopKSimilarByClientId(
        clientId, 
        queryVectorString, 
        MAX_K
    );
    
    long searchTime = System.currentTimeMillis() - startTime;
    log.info("pgvector search completed in {}ms, found {} chunks", searchTime, similarEmbeddings.size());
    
    // Extract unique FaqDocs (in case multiple chunks from same doc)
    List<FaqDoc> results = similarEmbeddings.stream()
            .map(Embedding::getDoc)
            .distinct()
            .limit(MAX_K)
            .collect(Collectors.toList());
    
    log.info("Returning {} relevant documents", results.size());
    
    return results;
}
```

---

## 🚀 EXPECTED RESULTS:

**Before pgvector:**
- Vector search: 1.36-2.27 seconds (in-memory Java computation)
- Total query time: ~3.7 seconds

**After pgvector:**
- Vector search: **50-100ms** (database-level ANN search)
- Total query time: **~1.5 seconds** (60% faster!)

---

## 📝 TESTING:

1. Restart your Spring Boot application
2. Re-index documents (they'll now have pgvector data)
3. Send a query and check logs for "pgvector search completed in Xms"

---

## ⚠️ NOTE:

The current implementation still uses in-memory search as fallback. Once you make the above changes, it will use pgvector for 20x faster searches!
