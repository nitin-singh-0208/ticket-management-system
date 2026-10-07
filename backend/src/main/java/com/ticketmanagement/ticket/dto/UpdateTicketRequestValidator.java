package com.ticketmanagement.ticket.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UpdateTicketRequestValidator implements ConstraintValidator<ValidUpdateTicket, UpdateTicketRequest> {

    @Override
    public boolean isValid(UpdateTicketRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        boolean valid = true;

        if (request.title() != null && request.title().isEmpty()) {
            addViolation(context, "title", "Title is required");
            valid = false;
        }
        if (request.description() != null && request.description().isEmpty()) {
            addViolation(context, "description", "Description is required");
            valid = false;
        }
        if (request.assignee() != null && request.assignee().isEmpty()) {
            addViolation(context, "assignee", "Assignee is required");
            valid = false;
        }

        return valid;
    }

    private static void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
