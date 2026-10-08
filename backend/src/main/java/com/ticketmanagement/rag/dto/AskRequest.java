package com.ticketmanagement.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AskRequest(
        @NotBlank(message = "Question is required")
        @Size(max = 2000, message = "Question must be at most 2000 characters")
        String question) {

    public AskRequest {
        question = question == null ? null : question.strip();
    }
}
