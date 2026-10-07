package com.ticketmanagement.ticket;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED;

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED_NEXT = Map.of(
            OPEN, EnumSet.of(IN_PROGRESS, CANCELLED),
            IN_PROGRESS, EnumSet.of(RESOLVED, CANCELLED),
            RESOLVED, EnumSet.of(CLOSED),
            CLOSED, Set.of(),
            CANCELLED, Set.of()
    );

    public Set<TicketStatus> allowedNext() {
        return ALLOWED_NEXT.get(this);
    }
}
