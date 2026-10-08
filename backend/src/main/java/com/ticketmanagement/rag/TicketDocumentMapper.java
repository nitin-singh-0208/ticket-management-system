package com.ticketmanagement.rag;

import com.ticketmanagement.comment.Comment;
import com.ticketmanagement.ticket.Ticket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

@Component
public class TicketDocumentMapper {

    static final int MAX_CHUNK_TOKENS = 800;

    static final int OVERLAP_TOKENS = 100;

    public List<Document> toDocuments(Ticket ticket, List<Comment> comments) {
        List<Document> documents = new ArrayList<>();
        documents.addAll(chunksFor(ticket, "description", ticket.getDescription()));
        if (comments != null) {
            for (Comment comment : comments) {
                documents.addAll(chunksFor(ticket, "comment", comment.getText()));
            }
        }
        if (hasText(ticket.getResolutionNotes())) {
            documents.addAll(chunksFor(ticket, "resolution", ticket.getResolutionNotes()));
        }
        return documents;
    }

    static int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return (text.length() + 3) / 4;
    }

    static int maxBodyCharacters(String header) {
        int maxChunkCharacters = MAX_CHUNK_TOKENS * 4 - 3;
        return maxChunkCharacters - header.length() - 1;
    }

    static int overlapCharacters() {
        return OVERLAP_TOKENS * 4 - 3;
    }

    private List<Document> chunksFor(Ticket ticket, String sourceType, String sectionText) {
        if (!hasText(sectionText) && !"description".equals(sourceType)) {
            return List.of();
        }
        String text = sectionText == null ? "" : sectionText.strip();
        String header = header(ticket, sourceType);
        Map<String, Object> metadata = metadata(ticket, sourceType);
        List<Document> documents = new ArrayList<>();
        for (String body : chunkBodies(header, text)) {
            String content = body.isEmpty() ? header : header + "\n" + body;
            documents.add(new Document(content, new LinkedHashMap<>(metadata)));
        }
        return documents;
    }

    private static List<String> chunkBodies(String header, String text) {
        if (text.isEmpty()) {
            return List.of("");
        }
        int bodyMax = maxBodyCharacters(header);
        List<String> paragraphs = paragraphs(text);
        List<String> bodies = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (paragraph.length() > bodyMax) {
                if (!current.isEmpty()) {
                    bodies.add(current.toString());
                    current.setLength(0);
                }
                bodies.addAll(splitOversized(paragraph, bodyMax));
                continue;
            }
            if (current.isEmpty()) {
                current.append(paragraph);
                continue;
            }
            String candidate = current + "\n\n" + paragraph;
            if (candidate.length() <= bodyMax) {
                current.setLength(0);
                current.append(candidate);
            } else {
                bodies.add(current.toString());
                current.setLength(0);
                current.append(paragraph);
            }
        }
        if (!current.isEmpty()) {
            bodies.add(current.toString());
        }
        return bodies;
    }

    private static List<String> splitOversized(String paragraph, int bodyMax) {
        int overlap = overlapCharacters();
        if (overlap >= bodyMax) {
            overlap = Math.max(1, bodyMax / 5);
        }
        List<String> parts = new ArrayList<>();
        int start = 0;
        while (start < paragraph.length()) {
            int end = Math.min(paragraph.length(), start + bodyMax);
            parts.add(paragraph.substring(start, end));
            if (end >= paragraph.length()) {
                break;
            }
            int next = end - overlap;
            if (next <= start) {
                next = start + 1;
            }
            start = next;
        }
        return parts;
    }

    private static List<String> paragraphs(String text) {
        String[] parts = text.replace("\r\n", "\n").replace('\r', '\n').split("\\n\\s*\\n");
        List<String> paragraphs = new ArrayList<>();
        for (String part : parts) {
            String paragraph = part.strip();
            if (!paragraph.isEmpty()) {
                paragraphs.add(paragraph);
            }
        }
        return paragraphs;
    }

    private static String header(Ticket ticket, String sourceType) {
        return "Ticket " + ticket.getTicketId()
                + " | " + ticket.getTitle()
                + " | status " + ticket.getStatus().name()
                + " | priority " + ticket.getPriority().name()
                + " | category " + ticket.getCategory()
                + " | section " + sourceType;
    }

    private static Map<String, Object> metadata(Ticket ticket, String sourceType) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("ticketId", ticket.getTicketId());
        metadata.put("status", ticket.getStatus().name());
        metadata.put("priority", ticket.getPriority().name());
        metadata.put("assignee", ticket.getAssignee());
        metadata.put("category", ticket.getCategory());
        metadata.put("sourceType", sourceType);
        return metadata;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
