package com.ticketmanagement.ticket.dto;

import com.ticketmanagement.ticket.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be at most 200 characters")
        String title,

        @NotBlank(message = "Description is required")
        @Size(max = 5000, message = "Description must be at most 5000 characters")
        String description,

        @NotNull(message = "Priority is required")
        TicketPriority priority,

        @NotBlank(message = "Assignee is required")
        @Size(max = 200, message = "Assignee must be at most 200 characters")
        String assignee,

        @NotBlank(message = "Category is required")
        @Size(max = 100, message = "Category must be at most 100 characters")
        String category,

        @Size(max = 5000, message = "Resolution notes must be at most 5000 characters")
        String resolutionNotes
) {
    public CreateTicketRequest {
        title = trim(title);
        description = trim(description);
        assignee = trim(assignee);
        category = trim(category);
        resolutionNotes = trimToNull(resolutionNotes);
    }

    private static String trim(String value) {
        return value == null ? null : value.strip();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String stripped = value.strip();
        return stripped.isEmpty() ? null : stripped;
    }
}
