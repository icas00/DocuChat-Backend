package com.aiassistant.dto;

// A single question/answer pair for the FAQ upload.
public class FaqEntry {
    private String question;
    private String answer;

    public FaqEntry() {
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
