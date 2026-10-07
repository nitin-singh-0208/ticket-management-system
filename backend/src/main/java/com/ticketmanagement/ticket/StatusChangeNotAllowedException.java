package com.ticketmanagement.ticket;

public class StatusChangeNotAllowedException extends RuntimeException {

    private final TicketStatus currentStatus;
    private final TicketStatus requestedStatus;

    public StatusChangeNotAllowedException(TicketStatus currentStatus, TicketStatus requestedStatus) {
        super("Changing status from " + currentStatus + " to " + requestedStatus + " is not allowed.");
        this.currentStatus = currentStatus;
        this.requestedStatus = requestedStatus;
    }

    public TicketStatus getCurrentStatus() {
        return currentStatus;
    }

    public TicketStatus getRequestedStatus() {
        return requestedStatus;
    }
}
