package com.ticketmanagement.ticket;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.stream.Stream;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class TicketStatusTransitionTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"));

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TicketRepository ticketRepository;

    @Test
    void savedTicket_hasNonNullTicketId() {
        Ticket ticket = new Ticket();
        ticket.setTitle("Status test");
        ticket.setDescription("Description");
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setAssignee("Alex Kim");
        ticket.setCategory("Payment");
        ticket.setStatus(TicketStatus.OPEN);

        Ticket saved = ticketRepository.save(ticket);
        ticketRepository.flush();

        assertNotNull(saved.getTicketId());
    }

    @ParameterizedTest(name = "{0} -> {1} allowed={2}")
    @MethodSource("transitionMatrix")
    void patchStatus_transitionMatrix(TicketStatus from, TicketStatus to, boolean allowed) throws Exception {
        Ticket ticket = new Ticket();
        ticket.setTitle("Transition test");
        ticket.setDescription("Description");
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setAssignee("Alex Kim");
        ticket.setCategory("Payment");
        ticket.setStatus(from);

        Ticket saved = ticketRepository.save(ticket);
        ticketRepository.flush();
        String ticketId = saved.getTicketId();

        String body = String.format("{\"status\": \"%s\"}", to.name());

        if (allowed) {
            mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is(to.name())));

            Ticket reloaded = ticketRepository.findByTicketId(ticketId).orElseThrow();
            assert reloaded.getStatus() == to;
        } else {
            String expectedDetail = String.format(
                    "Changing status from %s to %s is not allowed.", from.name(), to.name());

            mockMvc.perform(patch("/api/tickets/" + ticketId + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.detail", is(expectedDetail)));

            Ticket reloaded = ticketRepository.findByTicketId(ticketId).orElseThrow();
            assert reloaded.getStatus() == from;
        }
    }

    @Test
    void patchStatus_unknownTicket_returns404() throws Exception {
        mockMvc.perform(patch("/api/tickets/TKT-9999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail", is("Ticket TKT-9999 was not found.")));
    }

    private static Stream<Arguments> transitionMatrix() {
        return Stream.of(
                Arguments.of(TicketStatus.OPEN, TicketStatus.OPEN, false),
                Arguments.of(TicketStatus.OPEN, TicketStatus.IN_PROGRESS, true),
                Arguments.of(TicketStatus.OPEN, TicketStatus.RESOLVED, false),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CLOSED, false),
                Arguments.of(TicketStatus.OPEN, TicketStatus.CANCELLED, true),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.OPEN, false),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.IN_PROGRESS, false),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, true),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CLOSED, false),
                Arguments.of(TicketStatus.IN_PROGRESS, TicketStatus.CANCELLED, true),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.OPEN, false),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.IN_PROGRESS, false),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.RESOLVED, false),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CLOSED, true),
                Arguments.of(TicketStatus.RESOLVED, TicketStatus.CANCELLED, false),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.OPEN, false),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.IN_PROGRESS, false),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.RESOLVED, false),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.CLOSED, false),
                Arguments.of(TicketStatus.CLOSED, TicketStatus.CANCELLED, false),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.OPEN, false),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.IN_PROGRESS, false),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.RESOLVED, false),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.CLOSED, false),
                Arguments.of(TicketStatus.CANCELLED, TicketStatus.CANCELLED, false)
        );
    }
}
