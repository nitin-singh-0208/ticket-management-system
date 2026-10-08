package com.ticketmanagement.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ticketmanagement.rag.dto.AskResponse;
import com.ticketmanagement.rag.dto.AskSource;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

@ExtendWith(MockitoExtension.class)
class RagAskServiceTest {

    private static final String QUESTION = "Have we seen payment failures before?";

    private static final String NO_MATCH = "No relevant tickets found";

    @Mock
    private VectorStore vectorStore;

    @Mock
    private ChatModel chatModel;

    private RagAskService service;

    @BeforeEach
    void setUp() {
        RagProperties properties = new RagProperties();
        properties.setTopK(4);
        properties.setSimilarityThreshold(0.72);
        service = new RagAskService(vectorStore, chatModel, properties);
    }

    @Test
    void emptyRetrieval_skipsChatModelAndReturnsNoMatch() {
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of());

        AskResponse response = service.ask(QUESTION);

        assertEquals(NO_MATCH, response.answer());
        assertFalse(response.grounded());
        assertTrue(response.sources().isEmpty());
        verifyNoInteractions(chatModel);
        assertSearchUsesConfiguredRetrieval();
    }

    @Test
    void answerCitesNothingRetrieved_returnsNoMatch() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(chunk("TKT-1001", "Payment declined at checkout", "OPEN", 0.9,
                        "Card network returned a payment failure during checkout.")));
        String modelText = "Customers sometimes have trouble paying at checkout.";
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse(modelText));

        AskResponse response = service.ask(QUESTION);

        assertEquals(NO_MATCH, response.answer());
        assertFalse(response.grounded());
        assertTrue(response.sources().isEmpty());
        assertFalse(response.answer().contains(modelText));
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void answerCitesIdNotRetrieved_returnsNoMatch() {
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(chunk("TKT-1001", "Payment declined at checkout", "OPEN", 0.9,
                        "Card network returned a payment failure during checkout.")));
        String modelText = "TKT-1001 matches, and TKT-9999 also matches.";
        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse(modelText));

        AskResponse response = service.ask(QUESTION);

        assertEquals(NO_MATCH, response.answer());
        assertFalse(response.grounded());
        assertTrue(response.sources().isEmpty());
        assertFalse(response.answer().contains("TKT-9999"));
        assertFalse(response.answer().contains("TKT-1001"));
        verify(chatModel).call(any(Prompt.class));
    }

    @Test
    void sourcesDeduplicatedPerTicket_keepsHighestScoreAndSnippet() {
        String highBody = "A".repeat(350);
        String lowBody = "LOW-SCORE-BODY-SHOULD-NOT-APPEAR";
        String header1001 = header("TKT-1001", "Payment declined at checkout", "OPEN", "HIGH", "Payment", "description");
        Document high = chunk("TKT-1001", "Payment declined at checkout", "OPEN", 0.9, highBody);
        Document low = chunk("TKT-1001", "Payment declined at checkout", "OPEN", 0.7, lowBody);
        Document other = chunk("TKT-1002", "Duplicate capture", "IN_PROGRESS", 0.8,
                "A second charge was captured for the same order.");
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(low, high, other));
        when(chatModel.call(any(Prompt.class))).thenReturn(
                chatResponse("Both TKT-1001 and TKT-1002 show payment failures."));

        AskResponse response = service.ask(QUESTION);

        assertTrue(response.grounded());
        assertEquals(2, response.sources().size());
        AskSource first = source(response, "TKT-1001");
        assertEquals(0.9, first.score());
        assertEquals("Payment declined at checkout", first.title());
        assertEquals("OPEN", first.status());
        assertEquals("A".repeat(300), first.snippet());
        assertEquals(300, first.snippet().length());
        assertFalse(first.snippet().contains("Ticket TKT-1001"));
        assertFalse(first.snippet().contains(lowBody));
        AskSource second = source(response, "TKT-1002");
        assertEquals(0.8, second.score());
        assertEquals("Duplicate capture", second.title());
        assertEquals("IN_PROGRESS", second.status());
        assertEquals("A second charge was captured for the same order.", second.snippet());

        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(promptCaptor.capture());
        Prompt prompt = promptCaptor.getValue();
        assertFalse(prompt.getSystemMessage().getText().contains("TKT-1001"));
        assertFalse(prompt.getSystemMessage().getText().contains(QUESTION));
        assertTrue(prompt.getUserMessage().getText().contains(header1001));
        assertTrue(prompt.getUserMessage().getText().contains(QUESTION));
        assertFalse(prompt.getUserMessage().getText().contains("search_query:"));
        verify(chatModel, never()).call(any(String.class));
        assertSearchUsesConfiguredRetrieval();
    }

    private void assertSearchUsesConfiguredRetrieval() {
        ArgumentCaptor<SearchRequest> captor = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(captor.capture());
        SearchRequest request = captor.getValue();
        assertEquals(QUESTION, request.getQuery());
        assertFalse(request.getQuery().startsWith("search_query:"));
        assertEquals(4, request.getTopK());
        assertEquals(0.72, request.getSimilarityThreshold());
    }

    private static AskSource source(AskResponse response, String ticketId) {
        return response.sources().stream()
                .filter(source -> ticketId.equals(source.ticketId()))
                .findFirst()
                .orElseThrow();
    }

    private static ChatResponse chatResponse(String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }

    private static Document chunk(String ticketId, String title, String status, double score, String body) {
        String content = header(ticketId, title, status, "HIGH", "Payment", "description") + "\n" + body;
        return Document.builder()
                .text(content)
                .metadata(Map.of(
                        "ticketId", ticketId,
                        "status", status,
                        "priority", "HIGH",
                        "assignee", "Alex Kim",
                        "category", "Payment",
                        "sourceType", "description"))
                .score(score)
                .build();
    }

    private static String header(
            String ticketId, String title, String status, String priority, String category, String sourceType) {
        return "Ticket " + ticketId
                + " | " + title
                + " | status " + status
                + " | priority " + priority
                + " | category " + category
                + " | section " + sourceType;
    }
}
