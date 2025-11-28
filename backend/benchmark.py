import requests
import time
import random

# CONFIGURATION
BASE_URL = "http://localhost:8080/api"
CLIENT_ID = 1
API_KEY = "TEST_KEY"  # The key we set in the migration script

# ---------------------------------------------------------
# HELPER FUNCTIONS
# ---------------------------------------------------------
def generate_batch(size, start_index):
    """Generates dummy legal/medical data"""
    batch = []
    for i in range(start_index, start_index + size):
        batch.append({
            "question": f"What is the liability coverage for case ID-{i}?",
            "answer": f"For case ID-{i}, the policy covers up to $50,000 in damages under Section 4, Clause {i % 10}. Reference: DOC-{i}"
        })
    return {"entries": batch}

def run_test_step(step_name, doc_count, start_index):
    print(f"\n--- 🧪 {step_name}: Testing with {doc_count} Documents ---")
    
    # 1. GENERATE
    data = generate_batch(doc_count, start_index)
    
    # 2. UPLOAD (Measure API Latency)
    t0 = time.time()
    try:
        res = requests.post(f"{BASE_URL}/clients/{CLIENT_ID}/faq", json=data)
        if res.status_code != 200:
            print(f"❌ Upload Failed: {res.text}")
            return
    except Exception as e:
        print(f"❌ Connection Refused. Is Spring Boot running?")
        return
    upload_time = time.time() - t0
    print(f"📤 Upload Time:   {upload_time:.4f}s ({(upload_time/doc_count)*1000:.2f}ms per doc)")

    # 3. INDEX (Measure Embedding Latency)
    print("🧠 Indexing (Generating Embeddings)...")
    t0 = time.time()
    res = requests.post(f"{BASE_URL}/clients/{CLIENT_ID}/index")
    if res.status_code != 200:
        print(f"❌ Indexing Failed: {res.text}")
        return
    index_time = time.time() - t0
    print(f"⚙️ Indexing Time: {index_time:.4f}s ({(index_time/doc_count)*1000:.2f}ms per doc)")

    # 4. SEARCH (Measure RAG Latency)
    # We ask a specific question to see if the vector search slows down
    search_payload = {
        "apiKey": API_KEY,
        "message": f"What is the liability coverage for case ID-{start_index + int(doc_count/2)}?", # Ask about a doc in the middle
        "history": []
    }
    
    t0 = time.time()
    res = requests.post(f"{BASE_URL}/widget/chat", json=search_payload)
    search_time = time.time() - t0
    
    if res.status_code == 200:
        print(f"🔍 Search Time:   {search_time:.4f}s")
    else:
        print(f"❌ Search Failed: {res.status_code}")

    return upload_time, index_time, search_time

# ---------------------------------------------------------
# MAIN EXECUTION
# ---------------------------------------------------------
if __name__ == "__main__":
    print("🚀 STARTING STEP-WISE STRESS TEST")
    
    # STEP 1: WARM UP (50 Docs)
    # Allows JVM to warm up and DB connections to pool
    run_test_step("Warmup", 50, 0)
    
    # STEP 2: MEDIUM LOAD (500 Docs)
    # This checks if your API handles larger JSON bodies correctly
    time.sleep(2)
    run_test_step("Medium Load", 500, 100)
    
    # STEP 3: HEAVY LOAD (2000 Docs)
    # This checks if the Embedding Model (Mistral) times out or slows down
    time.sleep(2)
    run_test_step("Heavy Load", 2000, 1000)
