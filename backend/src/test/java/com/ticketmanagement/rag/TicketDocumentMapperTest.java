package com.ticketmanagement.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ticketmanagement.comment.Comment;
import com.ticketmanagement.ticket.Ticket;
import com.ticketmanagement.ticket.TicketPriority;
import com.ticketmanagement.ticket.TicketStatus;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

class TicketDocumentMapperTest {

    private final TicketDocumentMapper mapper = new TicketDocumentMapper();

    @Test
    void chunkContentStartsWithHeaderThenSectionText() {
        Ticket ticket = sampleTicket();
        ticket.setDescription("Card network returned a payment failure during checkout.");

        List<Document> documents = mapper.toDocuments(ticket, List.of());

        assertEquals(1, documents.size());
        assertEquals("""
                Ticket TKT-1001 | Payment declined at checkout | status OPEN | priority HIGH | category Payment | section description
                Card network returned a payment failure during checkout.\
                """.stripIndent().strip(), documents.get(0).getText());
        assertFalse(documents.get(0).getText().contains("search_document:"));
        assertMetadata(documents.get(0), "description");
    }

    @Test
    void everyChunkCarriesTicketMetadata() {
        Ticket ticket = sampleTicket();
        ticket.setDescription("Checkout failed.");
        ticket.setResolutionNotes("Retried the authorization.");
        Comment comment = new Comment();
        comment.setText("Customer confirmed the charge was declined.");

        List<Document> documents = mapper.toDocuments(ticket, List.of(comment));

        assertEquals(3, documents.size());
        assertMetadata(documents.get(0), "description");
        assertMetadata(documents.get(1), "comment");
        assertMetadata(documents.get(2), "resolution");
        assertTrue(documents.get(1).getText().startsWith(
                "Ticket TKT-1001 | Payment declined at checkout | status OPEN | priority HIGH | category Payment | section comment\n"));
        assertTrue(documents.get(2).getText().contains("Retried the authorization."));
        assertFalse(documents.get(0).getText().contains("Jordan Lee"));
    }

    @Test
    void resolutionIsSkippedWhenNotesAreNullOrBlank() {
        Ticket ticket = sampleTicket();
        ticket.setDescription("Checkout failed.");
        ticket.setResolutionNotes(null);
        assertEquals(1, mapper.toDocuments(ticket, List.of()).size());

        ticket.setResolutionNotes("   ");
        assertEquals(1, mapper.toDocuments(ticket, List.of()).size());

        ticket.setResolutionNotes("\n\t");
        assertEquals(1, mapper.toDocuments(ticket, List.of()).size());
    }

    @Test
    void paragraphsThatFitStayInOneChunk() {
        Ticket ticket = sampleTicket();
        ticket.setDescription("First paragraph.\n\nSecond paragraph.");

        List<Document> documents = mapper.toDocuments(ticket, List.of());

        assertEquals(1, documents.size());
        assertTrue(documents.get(0).getText().endsWith("First paragraph.\n\nSecond paragraph."));
        assertTrue(TicketDocumentMapper.estimateTokens(documents.get(0).getText()) <= TicketDocumentMapper.MAX_CHUNK_TOKENS);
    }

    @Test
    void longSectionSplitsOnParagraphBoundariesWithoutOverlap() {
        Ticket ticket = sampleTicket();
        String header = header(ticket, "description");
        int bodyMax = TicketDocumentMapper.maxBodyCharacters(header);
        String first = "q".repeat(bodyMax / 2);
        String second = "z".repeat(bodyMax / 2);
        ticket.setDescription(first + "\n\n" + second);

        List<Document> documents = mapper.toDocuments(ticket, List.of());

        assertEquals(2, documents.size());
        assertEquals(header + "\n" + first, documents.get(0).getText());
        assertEquals(header + "\n" + second, documents.get(1).getText());
        assertFalse(body(documents.get(1)).contains("q"));
        assertWithinTokenLimit(documents);
    }

    @Test
    void oversizedParagraphSplitsWithOverlap() {
        Ticket ticket = sampleTicket();
        String header = header(ticket, "description");
        int bodyMax = TicketDocumentMapper.maxBodyCharacters(header);
        int overlap = TicketDocumentMapper.overlapCharacters();
        String paragraph = "c".repeat(bodyMax + overlap + 50);
        ticket.setDescription(paragraph);

        List<Document> documents = mapper.toDocuments(ticket, List.of());

        assertTrue(documents.size() >= 2);
        String firstBody = body(documents.get(0));
        String secondBody = body(documents.get(1));
        assertEquals(paragraph.substring(firstBody.length() - overlap, firstBody.length()),
                secondBody.substring(0, overlap));
        assertWithinTokenLimit(documents);
        for (Document document : documents) {
            assertMetadata(document, "description");
            assertFalse(document.getText().contains("search_document:"));
        }
    }

    private static void assertWithinTokenLimit(List<Document> documents) {
        for (Document document : documents) {
            assertTrue(TicketDocumentMapper.estimateTokens(document.getText()) <= TicketDocumentMapper.MAX_CHUNK_TOKENS,
                    document.getText().length() + " characters exceeded the chunk limit");
        }
    }

    private static String body(Document document) {
        String text = document.getText();
        int newline = text.indexOf('\n');
        return text.substring(newline + 1);
    }

    private static void assertMetadata(Document document, String sourceType) {
        Map<String, Object> metadata = document.getMetadata();
        assertEquals("TKT-1001", metadata.get("ticketId"));
        assertEquals("OPEN", metadata.get("status"));
        assertEquals("HIGH", metadata.get("priority"));
        assertEquals("Jordan Lee", metadata.get("assignee"));
        assertEquals("Payment", metadata.get("category"));
        assertEquals(sourceType, metadata.get("sourceType"));
    }

    private static String header(Ticket ticket, String sourceType) {
        return "Ticket " + ticket.getTicketId()
                + " | " + ticket.getTitle()
                + " | status " + ticket.getStatus().name()
                + " | priority " + ticket.getPriority().name()
                + " | category " + ticket.getCategory()
                + " | section " + sourceType;
    }

    private static Ticket sampleTicket() {
        Ticket ticket = new Ticket();
        setTicketId(ticket, "TKT-1001");
        ticket.setTitle("Payment declined at checkout");
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setAssignee("Jordan Lee");
        ticket.setCategory("Payment");
        ticket.setStatus(TicketStatus.OPEN);
        return ticket;
    }

    private static void setTicketId(Ticket ticket, String ticketId) {
        try {
            Field field = Ticket.class.getDeclaredField("ticketId");
            field.setAccessible(true);
            field.set(ticket, ticketId);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
