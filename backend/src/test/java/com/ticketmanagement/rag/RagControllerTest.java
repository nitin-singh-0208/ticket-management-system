package com.ticketmanagement.rag;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmanagement.common.ApiExceptionHandler;
import com.ticketmanagement.rag.dto.AskResponse;
import com.ticketmanagement.rag.dto.AskSource;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = RagController.class)
@Import(ApiExceptionHandler.class)
class RagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RagAskService ragAskService;

    @Test
    void blankQuestion_returns400BeforeServiceRuns() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("question"))
                .andExpect(jsonPath("$.errors[0].message").value("Question is required"));

        verifyNoInteractions(ragAskService);
    }

    @Test
    void whitespaceQuestion_returns400BeforeServiceRuns() throws Exception {
        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\" \\n\\t \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].field").value("question"));

        verifyNoInteractions(ragAskService);
    }

    @Test
    void validQuestion_delegatesToService() throws Exception {
        when(ragAskService.ask("Have we seen payment failures before?")).thenReturn(new AskResponse(
                "Payment failures are described in TKT-1001.",
                List.of(new AskSource(
                        "TKT-1001",
                        "Payment declined at checkout",
                        "OPEN",
                        0.82,
                        "Card network returned a payment failure during checkout.")),
                true));

        mockMvc.perform(post("/api/ai/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"  Have we seen payment failures before?  \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grounded").value(true))
                .andExpect(jsonPath("$.answer").value("Payment failures are described in TKT-1001."))
                .andExpect(jsonPath("$.sources[0].ticketId").value("TKT-1001"));

        verify(ragAskService).ask("Have we seen payment failures before?");
    }
}
