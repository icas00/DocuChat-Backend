package com.aiassistant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// splits docs into chunks for rag
@Service
public class DocumentChunker {

    private static final Logger log = LoggerFactory.getLogger(DocumentChunker.class);

    private static final int DEFAULT_MAX_TOKENS = 500;
    private static final int DEFAULT_OVERLAP_TOKENS = 50;
    private static final String[] SEPARATORS = { "\n\n", "\n", ". ", " ", "" };

    // 1 token is about 4 chars
    private static final int CHARS_PER_TOKEN = 4;

    // chunks doc while keeping q&a structure
    public List<DocumentChunk> chunkDocument(String text, Long sourceDocId) {
        return chunkDocument(text, sourceDocId, DEFAULT_MAX_TOKENS, DEFAULT_OVERLAP_TOKENS);
    }

    // chunks with custom limits
    public List<DocumentChunk> chunkDocument(String text, Long sourceDocId, int maxTokens, int overlapTokens) {
        log.info("Chunking document (ID: {}) with maxTokens={}, overlap={}", sourceDocId, maxTokens, overlapTokens);

        List<DocumentChunk> allChunks = new ArrayList<>();

        // try to split by q&a first
        List<String> sections = splitByQAPattern(text);

        log.debug("Split document into {} Q&A sections", sections.size());

        int chunkIndex = 0;
        for (int sectionIdx = 0; sectionIdx < sections.size(); sectionIdx++) {
            String section = sections.get(sectionIdx);

            // if its small enough keep it
            if (estimateTokens(section) <= maxTokens) {
                allChunks.add(new DocumentChunk(
                        section,
                        sourceDocId,
                        chunkIndex++,
                        "Section " + (sectionIdx + 1)));
            } else {
                // too big so split it recursively
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

    // split by question markers
    private List<String> splitByQAPattern(String text) {
        List<String> sections = new ArrayList<>();

        // regex for questions
        Pattern pattern = Pattern.compile("^(Q:|Question:|\\d+\\.|\\*\\*Q:|\\*\\*Question:)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        List<Integer> splitPoints = new ArrayList<>();
        splitPoints.add(0); // start of file

        while (matcher.find()) {
            splitPoints.add(matcher.start());
        }

        // no questions found so treat as one block
        if (splitPoints.size() == 1) {
            sections.add(text);
            return sections;
        }

        // split at markers
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

    // recursive split using separators
    private List<String> recursiveSplit(String text, int maxTokens, int overlapTokens) {
        List<String> chunks = new ArrayList<>();

        int maxChars = maxTokens * CHARS_PER_TOKEN;
        int overlapChars = overlapTokens * CHARS_PER_TOKEN;

        // fits in one chunk
        if (text.length() <= maxChars) {
            chunks.add(text);
            return chunks;
        }

        // try separators big to small
        for (String separator : SEPARATORS) {
            if (separator.isEmpty()) {
                // last resort split by chars
                chunks.addAll(splitByCharacterCount(text, maxChars, overlapChars));
                return chunks;
            }

            String[] parts = text.split(Pattern.quote(separator));

            if (parts.length > 1) {
                // found separator so merge parts
                chunks.addAll(mergeParts(parts, separator, maxChars, overlapChars));
                return chunks;
            }
        }

        // fallback to char split
        chunks.addAll(splitByCharacterCount(text, maxChars, overlapChars));
        return chunks;
    }

    // merge parts into chunks
    private List<String> mergeParts(String[] parts, String separator, int maxChars, int overlapChars) {
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String part : parts) {
            String withSeparator = part + separator;

            // if too big save and start new
            if (currentChunk.length() + withSeparator.length() > maxChars && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());

                // start new chunk with overlap
                String overlap = getOverlap(currentChunk.toString(), overlapChars);
                currentChunk = new StringBuilder(overlap);
            }

            currentChunk.append(withSeparator);
        }

        // add the last bit
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    // get overlap from end
    private String getOverlap(String text, int overlapChars) {
        if (text.length() <= overlapChars) {
            return text;
        }
        return text.substring(text.length() - overlapChars);
    }

    // split by char count
    private List<String> splitByCharacterCount(String text, int maxChars, int overlapChars) {
        List<String> chunks = new ArrayList<>();

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            chunks.add(text.substring(start, end));
            start = end - overlapChars; // move back for overlap

            if (start >= text.length() - overlapChars) {
                break; // skip tiny chunks
            }
        }

        return chunks;
    }

    // estimate tokens
    private int estimateTokens(String text) {
        return text.length() / CHARS_PER_TOKEN;
    }

    // chunk with metadata
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
