package com.ticketmanagement.ticket;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmanagement.common.ApiExceptionHandler;
import com.ticketmanagement.common.TicketNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TicketController.class, properties = "spring.jackson.deserialization.fail-on-unknown-properties=true")
@Import(ApiExceptionHandler.class)
class TicketValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @Test
    void postTicket_blankRequiredFields_returns400WithErrors() throws Exception {
        String body = """
                {
                  "title": "   ",
                  "description": "\\t  \\n",
                  "priority": "HIGH",
                  "assignee": " ",
                  "category": "   "
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Bad Request")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.detail", is("Request validation failed.")))
                .andExpect(jsonPath("$.errors", hasSize(4)))
                .andExpect(jsonPath("$.errors[?(@.field == 'title')].message", is(java.util.List.of("Title is required"))))
                .andExpect(jsonPath("$.errors[?(@.field == 'description')].message", is(java.util.List.of("Description is required"))))
                .andExpect(jsonPath("$.errors[?(@.field == 'assignee')].message", is(java.util.List.of("Assignee is required"))))
                .andExpect(jsonPath("$.errors[?(@.field == 'category')].message", is(java.util.List.of("Category is required"))));

        verifyNoInteractions(ticketService);
    }

    @Test
    void postTicket_omittedPriority_returns400WithPriorityRequired() throws Exception {
        String body = """
                {
                  "title": "Valid title",
                  "description": "Valid description",
                  "assignee": "Alex",
                  "category": "Billing"
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field", is("priority")))
                .andExpect(jsonPath("$.errors[0].message", is("Priority is required")));

        verifyNoInteractions(ticketService);
    }

    @Test
    void postTicket_fieldsExceedingMaxLength_returns400() throws Exception {
        String longTitle = "a".repeat(201);
        String longAssignee = "b".repeat(201);
        String longCategory = "c".repeat(101);
        String longDescription = "d".repeat(5001);
        String longResolutionNotes = "e".repeat(5001);

        String body = """
                {
                  "title": "%s",
                  "description": "%s",
                  "priority": "LOW",
                  "assignee": "%s",
                  "category": "%s",
                  "resolutionNotes": "%s"
                }
                """.formatted(longTitle, longDescription, longAssignee, longCategory, longResolutionNotes);

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(5)))
                .andExpect(jsonPath("$.errors[?(@.field == 'title')].message", is(java.util.List.of("Title must be at most 200 characters"))))
                .andExpect(jsonPath("$.errors[?(@.field == 'assignee')].message", is(java.util.List.of("Assignee must be at most 200 characters"))))
                .andExpect(jsonPath("$.errors[?(@.field == 'category')].message", is(java.util.List.of("Category must be at most 100 characters"))))
                .andExpect(jsonPath("$.errors[?(@.field == 'description')].message", is(java.util.List.of("Description must be at most 5000 characters"))))
                .andExpect(jsonPath("$.errors[?(@.field == 'resolutionNotes')].message", is(java.util.List.of("Resolution notes must be at most 5000 characters"))));

        verifyNoInteractions(ticketService);
    }

    @Test
    void postTicket_invalidPriorityValue_returns400() throws Exception {
        String body = """
                {
                  "title": "Payment failed",
                  "description": "Card declined",
                  "priority": "URGENT",
                  "assignee": "Alex",
                  "category": "Billing"
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Invalid value for property 'priority'")));

        verifyNoInteractions(ticketService);
    }

    @Test
    void postTicket_withStatusProperty_returns400UnknownProperty() throws Exception {
        String body = """
                {
                  "title": "Payment failed",
                  "description": "Card declined",
                  "priority": "HIGH",
                  "assignee": "Alex",
                  "category": "Billing",
                  "status": "OPEN"
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Unknown property 'status'")));

        verifyNoInteractions(ticketService);
    }

    @Test
    void postTicket_withTicketIdProperty_returns400UnknownProperty() throws Exception {
        String body = """
                {
                  "title": "Payment failed",
                  "description": "Card declined",
                  "priority": "HIGH",
                  "assignee": "Alex",
                  "category": "Billing",
                  "ticketId": "TKT-1001"
                }
                """;

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Unknown property 'ticketId'")));

        verifyNoInteractions(ticketService);
    }

    @Test
    void patchStatus_invalidStatusValue_returns400() throws Exception {
        String body = """
                {
                  "status": "DONE"
                }
                """;

        mockMvc.perform(patch("/api/tickets/TKT-1001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Invalid value for property 'status'")));

        verifyNoInteractions(ticketService);
    }

    @Test
    void patchStatus_withExtraTitleProperty_returns400UnknownProperty() throws Exception {
        String body = """
                {
                  "status": "IN_PROGRESS",
                  "title": "Changed"
                }
                """;

        mockMvc.perform(patch("/api/tickets/TKT-1001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail", is("Unknown property 'title'")));

        verifyNoInteractions(ticketService);
    }

    @Test
    void patchStatus_omittedStatus_returns400WithStatusRequired() throws Exception {
        String body = "{}";

        mockMvc.perform(patch("/api/tickets/TKT-1001/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field", is("status")))
                .andExpect(jsonPath("$.errors[0].message", is("Status is required")));

        verifyNoInteractions(ticketService);
    }

    @Test
    void getTicket_notFound_returns404WithoutStackTrace() throws Exception {
        when(ticketService.getTicket("TKT-9999"))
                .thenThrow(new TicketNotFoundException("TKT-9999"));

        mockMvc.perform(get("/api/tickets/TKT-9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Not Found")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Ticket TKT-9999 was not found.")))
                .andExpect(jsonPath("$.instance", is("/api/tickets/TKT-9999")))
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.trace").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }
}
