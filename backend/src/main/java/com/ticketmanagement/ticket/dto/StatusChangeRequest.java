package com.ticketmanagement.ticket.dto;

import com.ticketmanagement.ticket.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(
        @NotNull(message = "Status is required")
        TicketStatus status
) {
}
