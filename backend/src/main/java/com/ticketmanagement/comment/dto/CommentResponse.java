package com.ticketmanagement.comment.dto;

import java.time.Instant;

public record CommentResponse(
        Long id,
        String text,
        Instant createdAt
) {
}
