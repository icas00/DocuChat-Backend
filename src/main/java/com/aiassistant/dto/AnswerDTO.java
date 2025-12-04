package com.aiassistant.dto;

import java.util.List;

public class AnswerDTO {
    private String text;
    private List<String> sources;
    private double similarity;
    private boolean fromCache = false; // Default to false

    public AnswerDTO() {
    }

    public AnswerDTO(String text, List<String> sources, double similarity) {
        this.text = text;
        this.sources = sources;
        this.similarity = similarity;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }
}
