package com.ticketmanagement.ticket.dto;

import com.ticketmanagement.ticket.TicketPriority;
import com.ticketmanagement.ticket.TicketStatus;

public record TicketSummary(
        String ticketId,
        String title,
        TicketStatus status,
        TicketPriority priority,
        String assignee
) {
}
