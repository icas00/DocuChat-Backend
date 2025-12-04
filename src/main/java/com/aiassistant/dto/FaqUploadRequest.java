package com.aiassistant.dto;

import java.util.List;

// The whole request body for uploading multiple FAQs.
public class FaqUploadRequest {
    private List<FaqEntry> entries;

    public FaqUploadRequest() {
    }

    public List<FaqEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<FaqEntry> entries) {
        this.entries = entries;
    }
}
