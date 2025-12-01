---
description: Verify Phase 2 Fix (Batching & Cleanup)
---

# Verify Phase 2 Fix

This workflow cleans up the messy data for Client 50, re-uploads the handbook, and triggers indexing to verify batch processing.

## 1. Clear Old Data
// turbo
Run this command to delete all existing documents and embeddings for Client 50.
```bash
curl -X DELETE http://localhost:7860/api/clients/50/data
```

## 2. Upload Handbook
// turbo
Upload the clean `fitzone_handbook.txt` file.
```bash
curl -X POST -F "file=@fitzone_handbook.txt" http://localhost:7860/api/clients/50/documents
```

## 3. Trigger Indexing
// turbo
Trigger the indexing process. Watch your application logs! You should see "Processing batch of..." messages instead of hundreds of individual calls.
```bash
curl -X POST http://localhost:7860/api/clients/50/index
```
