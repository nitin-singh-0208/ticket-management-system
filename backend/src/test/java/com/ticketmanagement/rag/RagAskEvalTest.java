package com.ticketmanagement.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ai-eval")
class RagAskEvalTest {

    private static final String BASE_URL = System.getProperty("ai.eval.base-url", "http://localhost:8080");

    private static final String NO_MATCH = "No relevant tickets found";

    private static final Set<String> PAYMENT_TICKETS = Set.of(
            "TKT-1001", "TKT-1002", "TKT-1003", "TKT-1004", "TKT-1005");

    private static final Set<String> HIGH_PRIORITY_PAYMENT_TICKETS = Set.of("TKT-1001", "TKT-1002");

    private static final Set<String> SHIPMENT_TICKETS = Set.of(
            "TKT-1006", "TKT-1007", "TKT-1008", "TKT-1009", "TKT-1010");

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void q1_paymentFailuresAreGrounded() throws Exception {
        JsonNode body = ask("Have we seen payment failures before?");
        assertTrue(body.path("grounded").asBoolean());
        assertTrue(intersects(ticketIds(body), PAYMENT_TICKETS));
        String answer = body.path("answer").asText("").toLowerCase();
        assertTrue(answer.contains("payment"));
        assertTrue(answer.contains("fail"));
    }

    @Test
    void q2_resolutionForTkt1001UsesIndexedContent() throws Exception {
        JsonNode body = ask("What was the resolution for ticket TKT-1001?");
        assertTrue(body.path("grounded").asBoolean());
        assertTrue(ticketIds(body).contains("TKT-1001"));
        String answer = body.path("answer").asText("");
        assertFalse(answer.contains("The refund posted."));
        assertFalse(answer.contains("The invoice was reissued."));
        assertFalse(answer.contains("A replacement was sent."));
    }

    @Test
    void q3_shipmentTrackingCausesAreGrounded() throws Exception {
        JsonNode body = ask("What are the common causes of shipment tracking issues?");
        assertTrue(body.path("grounded").asBoolean());
        assertTrue(intersects(ticketIds(body), SHIPMENT_TICKETS));
        String answer = body.path("answer").asText("").toLowerCase();
        assertTrue(answer.contains("track")
                || answer.contains("shipment")
                || answer.contains("carrier")
                || answer.contains("scan")
                || answer.contains("parcel")
                || answer.contains("deliver"));
    }

    @Test
    void q4_similarResolvedTicketsAreResolvedOrClosed() throws Exception {
        JsonNode body = ask("Show me similar resolved tickets.");
        assertTrue(body.path("grounded").asBoolean());
        List<String> statuses = statuses(body);
        assertFalse(statuses.isEmpty());
        for (String status : statuses) {
            assertTrue(status.equals("RESOLVED") || status.equals("CLOSED"), status);
        }
    }

    @Test
    void q5_highPriorityPaymentTicketsOnly() throws Exception {
        JsonNode body = ask("Which high-priority tickets are related to payment?");
        assertTrue(body.path("grounded").asBoolean());
        Set<String> ids = ticketIds(body);
        assertFalse(ids.isEmpty());
        assertTrue(PAYMENT_TICKETS.containsAll(ids));
        assertTrue(HIGH_PRIORITY_PAYMENT_TICKETS.containsAll(ids), ids.toString());
    }

    @Test
    void o1_weatherIsNoMatch() throws Exception {
        assertNoMatch(ask("What is the weather in Paris today?"));
    }

    @Test
    void o2_stockPriceIsNoMatch() throws Exception {
        assertNoMatch(ask("What is the stock price of Apple?"));
    }

    @Test
    void o3_bakingIsNoMatch() throws Exception {
        assertNoMatch(ask("How do I bake sourdough bread?"));
    }

    @Test
    void o4_capitalOfFranceIsNoMatch() throws Exception {
        JsonNode body = ask("What is the capital of France?");
        assertNoMatch(body);
        assertFalse(body.path("answer").asText("").toLowerCase().contains("paris"));
    }

    @Test
    void o5_missingTicketIsNoMatch() throws Exception {
        JsonNode body = ask("What was the resolution for TKT-9999?");
        assertNoMatch(body);
        assertFalse(body.path("answer").asText("").contains("TKT-9999"));
    }

    private static void assertNoMatch(JsonNode body) {
        assertFalse(body.path("grounded").asBoolean());
        assertEquals(NO_MATCH, body.path("answer").asText());
        assertTrue(body.path("sources").isArray());
        assertEquals(0, body.path("sources").size());
    }

    private static JsonNode ask(String question) throws Exception {
        String payload = MAPPER.writeValueAsString(MAPPER.createObjectNode().put("question", question));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/ai/ask"))
                .timeout(Duration.ofSeconds(180))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), response.body());
        return MAPPER.readTree(response.body());
    }

    private static Set<String> ticketIds(JsonNode body) {
        Set<String> ids = new java.util.LinkedHashSet<>();
        for (JsonNode source : body.path("sources")) {
            ids.add(source.path("ticketId").asText());
        }
        return ids;
    }

    private static List<String> statuses(JsonNode body) {
        List<String> statuses = new ArrayList<>();
        for (JsonNode source : body.path("sources")) {
            statuses.add(source.path("status").asText());
        }
        return statuses;
    }

    private static boolean intersects(Set<String> left, Set<String> right) {
        for (String id : left) {
            if (right.contains(id)) {
                return true;
            }
        }
        return false;
    }
}
