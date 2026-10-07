package com.ticketmanagement.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommentRequest(
        @NotBlank(message = "Comment needs text")
        @Size(max = 5000, message = "Comment must be at most 5000 characters")
        String text
) {
    public CreateCommentRequest {
        text = text == null ? null : text.strip();
    }
}
