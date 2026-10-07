package com.ticketmanagement.ticket.dto;

import java.util.List;

public record TicketPage(
        List<TicketSummary> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
