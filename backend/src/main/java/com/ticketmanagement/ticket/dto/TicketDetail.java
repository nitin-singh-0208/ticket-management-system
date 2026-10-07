package com.ticketmanagement.ticket.dto;

import com.ticketmanagement.comment.dto.CommentResponse;
import com.ticketmanagement.ticket.TicketPriority;
import com.ticketmanagement.ticket.TicketStatus;
import java.time.Instant;
import java.util.List;

public record TicketDetail(
        String ticketId,
        String title,
        String description,
        TicketPriority priority,
        String assignee,
        String category,
        String resolutionNotes,
        TicketStatus status,
        Instant createdAt,
        List<CommentResponse> comments,
        List<TicketStatus> allowedNextStatuses
) {
}
