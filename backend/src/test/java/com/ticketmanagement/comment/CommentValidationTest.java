package com.ticketmanagement.comment;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ticketmanagement.common.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CommentController.class)
@Import(ApiExceptionHandler.class)
class CommentValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Test
    void postComment_emptyText_returns400() throws Exception {
        String body = """
                {
                  "text": ""
                }
                """;

        mockMvc.perform(post("/api/tickets/TKT-1001/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field", is("text")))
                .andExpect(jsonPath("$.errors[0].message", is("Comment needs text")));

        verifyNoInteractions(commentService);
    }

    @Test
    void postComment_whitespaceOnlyText_returns400() throws Exception {
        String body = """
                {
                  "text": "   "
                }
                """;

        mockMvc.perform(post("/api/tickets/TKT-1001/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field", is("text")))
                .andExpect(jsonPath("$.errors[0].message", is("Comment needs text")));

        verifyNoInteractions(commentService);
    }

    @Test
    void postComment_textExceedingMaxLength_returns400() throws Exception {
        String longText = "a".repeat(5001);
        String body = """
                {
                  "text": "%s"
                }
                """.formatted(longText);

        mockMvc.perform(post("/api/tickets/TKT-1001/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].field", is("text")))
                .andExpect(jsonPath("$.errors[0].message", is("Comment must be at most 5000 characters")));

        verifyNoInteractions(commentService);
    }
}
