package com.ticketmanagement.common;

public class InvalidListQueryException extends RuntimeException {

    public InvalidListQueryException(String message) {
        super(message);
    }
}
