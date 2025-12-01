package com.aiassistant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for chunking documents into semantic units optimized for RAG.
 * Uses recursive character splitting with configurable token limits and
 * overlap.
 */
@Slf4j
@Service
public class DocumentChunker {

    private static final int DEFAULT_MAX_TOKENS = 500;
    private static final int DEFAULT_OVERLAP_TOKENS = 50;
    private static final String[] SEPARATORS = { "\n\n", "\n", ". ", " ", "" };

    // Rough approximation: 1 token ≈ 4 characters for English text
    private static final int CHARS_PER_TOKEN = 4;

    /**
     * Chunk a document into semantic units, preserving Q&A structure.
     * 
     * @param text        The document text to chunk
     * @param sourceDocId The ID of the source document (for metadata)
     * @return List of chunks with metadata
     */
    public List<DocumentChunk> chunkDocument(String text, Long sourceDocId) {
        return chunkDocument(text, sourceDocId, DEFAULT_MAX_TOKENS, DEFAULT_OVERLAP_TOKENS);
    }

    /**
     * Chunk a document with custom token limits.
     * 
     * @param text          The document text to chunk
     * @param sourceDocId   The ID of the source document
     * @param maxTokens     Maximum tokens per chunk
     * @param overlapTokens Overlap between consecutive chunks
     * @return List of chunks with metadata
     */
    public List<DocumentChunk> chunkDocument(String text, Long sourceDocId, int maxTokens, int overlapTokens) {
        log.info("Chunking document (ID: {}) with maxTokens={}, overlap={}", sourceDocId, maxTokens, overlapTokens);

        List<DocumentChunk> allChunks = new ArrayList<>();

        // First, try to split by Q&A patterns to preserve semantic units
        List<String> sections = splitByQAPattern(text);

        log.debug("Split document into {} Q&A sections", sections.size());

        int chunkIndex = 0;
        for (int sectionIdx = 0; sectionIdx < sections.size(); sectionIdx++) {
            String section = sections.get(sectionIdx);

            // If section is small enough, keep it as one chunk
            if (estimateTokens(section) <= maxTokens) {
                allChunks.add(new DocumentChunk(
                        section,
                        sourceDocId,
                        chunkIndex++,
                        "Section " + (sectionIdx + 1)));
            } else {
                // Section too large, apply recursive splitting
                List<String> subChunks = recursiveSplit(section, maxTokens, overlapTokens);
                for (String subChunk : subChunks) {
                    allChunks.add(new DocumentChunk(
                            subChunk,
                            sourceDocId,
                            chunkIndex++,
                            "Section " + (sectionIdx + 1)));
                }
            }
        }

        log.info("Created {} chunks from document ID: {}", allChunks.size(), sourceDocId);
        return allChunks;
    }

    /**
     * Split text by Q&A patterns (Question:, Q:, etc.)
     */
    private List<String> splitByQAPattern(String text) {
        List<String> sections = new ArrayList<>();

        // Pattern to match question markers at start of line
        Pattern pattern = Pattern.compile("^(Q:|Question:|\\d+\\.|\\*\\*Q:|\\*\\*Question:)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        List<Integer> splitPoints = new ArrayList<>();
        splitPoints.add(0); // Start of document

        while (matcher.find()) {
            splitPoints.add(matcher.start());
        }

        // If no Q&A patterns found, treat entire text as one section
        if (splitPoints.size() == 1) {
            sections.add(text);
            return sections;
        }

        // Split at each question marker
        for (int i = 0; i < splitPoints.size(); i++) {
            int start = splitPoints.get(i);
            int end = (i + 1 < splitPoints.size()) ? splitPoints.get(i + 1) : text.length();

            String section = text.substring(start, end).trim();
            if (!section.isEmpty()) {
                sections.add(section);
            }
        }

        return sections;
    }

    /**
     * Recursively split text using hierarchical separators.
     * Tries larger separators first (paragraphs, sentences, words).
     */
    private List<String> recursiveSplit(String text, int maxTokens, int overlapTokens) {
        List<String> chunks = new ArrayList<>();

        int maxChars = maxTokens * CHARS_PER_TOKEN;
        int overlapChars = overlapTokens * CHARS_PER_TOKEN;

        // If text fits in one chunk, return it
        if (text.length() <= maxChars) {
            chunks.add(text);
            return chunks;
        }

        // Try each separator in order (largest to smallest)
        for (String separator : SEPARATORS) {
            if (separator.isEmpty()) {
                // Last resort: split by character count
                chunks.addAll(splitByCharacterCount(text, maxChars, overlapChars));
                return chunks;
            }

            String[] parts = text.split(Pattern.quote(separator));

            if (parts.length > 1) {
                // Separator found, merge parts into chunks
                chunks.addAll(mergeParts(parts, separator, maxChars, overlapChars));
                return chunks;
            }
        }

        // Fallback: split by character count
        chunks.addAll(splitByCharacterCount(text, maxChars, overlapChars));
        return chunks;
    }

    /**
     * Merge text parts into chunks, respecting max size and overlap.
     */
    private List<String> mergeParts(String[] parts, String separator, int maxChars, int overlapChars) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String part : parts) {
            String withSeparator = part + separator;

            // If adding this part would exceed max, save current chunk and start new one
            if (currentChunk.length() + withSeparator.length() > maxChars && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());

                // Start new chunk with overlap from previous chunk
                String overlap = getOverlap(currentChunk.toString(), overlapChars);
                currentChunk = new StringBuilder(overlap);
            }

            currentChunk.append(withSeparator);
        }

        // Add final chunk
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * Get overlap text from end of previous chunk.
     */
    private String getOverlap(String text, int overlapChars) {
        if (text.length() <= overlapChars) {
            return text;
        }
        return text.substring(text.length() - overlapChars);
    }

    /**
     * Split text by character count (last resort).
     */
    private List<String> splitByCharacterCount(String text, int maxChars, int overlapChars) {
        List<String> chunks = new ArrayList<>();

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            chunks.add(text.substring(start, end));
            start = end - overlapChars; // Move back by overlap amount

            if (start >= text.length() - overlapChars) {
                break; // Avoid tiny trailing chunks
            }
        }

        return chunks;
    }

    /**
     * Estimate token count (rough approximation: 1 token ≈ 4 chars).
     */
    private int estimateTokens(String text) {
        return text.length() / CHARS_PER_TOKEN;
    }

    /**
     * Represents a document chunk with metadata.
     */
    public static class DocumentChunk {
        private final String text;
        private final Long sourceDocId;
        private final int chunkIndex;
        private final String sectionTitle;

        public DocumentChunk(String text, Long sourceDocId, int chunkIndex, String sectionTitle) {
            this.text = text;
            this.sourceDocId = sourceDocId;
            this.chunkIndex = chunkIndex;
            this.sectionTitle = sectionTitle;
        }

        public String getText() {
            return text;
        }

        public Long getSourceDocId() {
            return sourceDocId;
        }

        public int getChunkIndex() {
            return chunkIndex;
        }

        public String getSectionTitle() {
            return sectionTitle;
        }
    }
}
