package com.ticketmanagement.common;

import com.ticketmanagement.ticket.StatusChangeNotAllowedException;
import com.ticketmanagement.common.InvalidListQueryException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidListQueryException.class)
    ResponseEntity<ProblemDetail> handleInvalidListQuery(InvalidListQueryException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(StatusChangeNotAllowedException.class)
    ResponseEntity<ProblemDetail> handleStatusChangeNotAllowed(
            StatusChangeNotAllowedException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(
                HttpStatus.CONFLICT,
                "Changing status from " + ex.getCurrentStatus() + " to " + ex.getRequestedStatus() + " is not allowed.",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @ExceptionHandler(TicketNotFoundException.class)
    ResponseEntity<ProblemDetail> handleTicketNotFound(TicketNotFoundException ex, HttpServletRequest request) {
        ProblemDetail problem = problem(
                HttpStatus.NOT_FOUND,
                "Ticket " + ex.getTicketId() + " was not found.",
                request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        List<FieldErrorItem> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(fieldError -> {
            String message = fieldError.getDefaultMessage() == null ? "Invalid value" : fieldError.getDefaultMessage();
            errors.add(new FieldErrorItem(fieldError.getField(), message));
        });
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Request validation failed.", instance(request));
        problem.setProperty("errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, unreadableDetail(ex), instance(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        String name = "value";
        if (ex instanceof MethodArgumentTypeMismatchException mismatch && mismatch.getName() != null) {
            name = mismatch.getName();
        } else if (ex.getPropertyName() != null) {
            name = ex.getPropertyName();
        }
        ProblemDetail problem = problem(
                HttpStatus.BAD_REQUEST,
                "Invalid value for property '" + name + "'",
                instance(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private static ProblemDetail problem(HttpStatus status, String detail, String instance) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        problem.setInstance(URI.create(instance));
        problem.setType(URI.create("about:blank"));
        return problem;
    }

    private static String instance(WebRequest request) {
        if (request instanceof ServletWebRequest servletRequest) {
            return servletRequest.getRequest().getRequestURI();
        }
        String description = request.getDescription(false);
        if (description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }

    private static String unreadableDetail(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof UnrecognizedPropertyException unrecognized) {
                return "Unknown property '" + unrecognized.getPropertyName() + "'";
            }
            if (current instanceof InvalidNullException invalidNull) {
                String name = invalidNull.getPropertyName() == null
                        ? "value"
                        : invalidNull.getPropertyName().getSimpleName();
                return "Property '" + name + "' must not be null";
            }
            if (current instanceof InvalidFormatException invalidFormat) {
                return "Invalid value for property '" + propertyName(invalidFormat) + "'";
            }
            Throwable next = current.getCause();
            if (next == current) {
                break;
            }
            current = next;
        }
        return "The request body could not be read.";
    }

    private static String propertyName(InvalidFormatException invalidFormat) {
        List<JsonMappingException.Reference> path = invalidFormat.getPath();
        if (path == null || path.isEmpty()) {
            return "value";
        }
        String fieldName = path.get(path.size() - 1).getFieldName();
        return fieldName == null ? "value" : fieldName;
    }

    private record FieldErrorItem(String field, String message) {
    }
}
