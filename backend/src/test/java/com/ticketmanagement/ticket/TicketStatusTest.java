package com.ticketmanagement.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TicketStatusTest {

    @Test
    void allowedNext_open() {
        assertEquals(EnumSet.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED), TicketStatus.OPEN.allowedNext());
    }

    @Test
    void allowedNext_inProgress() {
        assertEquals(EnumSet.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED), TicketStatus.IN_PROGRESS.allowedNext());
    }

    @Test
    void allowedNext_resolved() {
        assertEquals(EnumSet.of(TicketStatus.CLOSED), TicketStatus.RESOLVED.allowedNext());
    }

    @Test
    void allowedNext_closed() {
        assertEquals(Set.of(), TicketStatus.CLOSED.allowedNext());
    }

    @Test
    void allowedNext_cancelled() {
        assertEquals(Set.of(), TicketStatus.CANCELLED.allowedNext());
    }

    @ParameterizedTest(name = "{0} -> {1} is allowed")
    @MethodSource("allowedTransitions")
    void transition_allowed(TicketStatus from, TicketStatus to) {
        assertTrue(from.allowedNext().contains(to));
    }

    @ParameterizedTest(name = "{0} -> {1} is refused")
    @MethodSource("refusedTransitions")
    void transition_refused(TicketStatus from, TicketStatus to) {
        assertFalse(from.allowedNext().contains(to));
    }

    private static Stream<Arguments> allowedTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CLOSED)
        );
    }

    private static Stream<Arguments> refusedTransitions() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.OPEN),
                Arguments.of(TicketStatus.OPEN, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.OPEN),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.CANCELLED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.OPEN),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.IN_PROGRESS),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.RESOLVED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.CLOSED),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.CANCELLED)
        );
    }
}
