package com.ticketmanagement.ticket.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.ticketmanagement.ticket.TicketPriority;
import jakarta.validation.constraints.Size;

@ValidUpdateTicket
public class UpdateTicketRequest {

    @JsonSetter(nulls = Nulls.FAIL)
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @JsonSetter(nulls = Nulls.FAIL)
    @Size(max = 5000, message = "Description must be at most 5000 characters")
    private String description;

    @JsonSetter(nulls = Nulls.FAIL)
    private TicketPriority priority;

    @JsonSetter(nulls = Nulls.FAIL)
    @Size(max = 200, message = "Assignee must be at most 200 characters")
    private String assignee;

    @JsonSetter(nulls = Nulls.FAIL)
    @Size(max = 5000, message = "Resolution notes must be at most 5000 characters")
    private String resolutionNotes;

    public String title() {
        return title;
    }

    public void setTitle(String title) {
        this.title = trim(title);
    }

    public String description() {
        return description;
    }

    public void setDescription(String description) {
        this.description = trim(description);
    }

    public TicketPriority priority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public String assignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = trim(assignee);
    }

    public String resolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = trim(resolutionNotes);
    }

    private static String trim(String value) {
        return value == null ? null : value.strip();
    }
}
