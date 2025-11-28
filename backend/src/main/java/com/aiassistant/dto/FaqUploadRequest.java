package com.aiassistant.dto;

import lombok.Data;
import java.util.List;

// The whole request body for uploading multiple FAQs.
@Data
public class FaqUploadRequest {
    private List<FaqEntry> entries;
}
