package com.ticketmanagement.rag;

import com.ticketmanagement.rag.dto.AskResponse;
import com.ticketmanagement.rag.dto.AskSource;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

@Service
public class RagAskService {

    static final String NO_MATCH_ANSWER = "No relevant tickets found";

    static final String SYSTEM_PROMPT = """
            You answer questions about support tickets using only the ticket context in the user message.
            Use only facts that appear in that context. Do not use outside knowledge.
            Cite every ticket you rely on by its ticket id (TKT- followed by digits) exactly as shown in the context header.
            If you cite a ticket, the id must appear in the context. Do not cite any ticket id that is not in the context.
            If the context does not contain enough information to answer, say that the tickets do not contain the answer and do not invent facts.
            """;

    private static final Logger log = LoggerFactory.getLogger(RagAskService.class);

    private static final Pattern TICKET_ID = Pattern.compile("\\bTKT-\\d+\\b");

    private static final int SNIPPET_MAX = 300;

    private final VectorStore vectorStore;

    private final ChatModel chatModel;

    private final RagProperties properties;

    public RagAskService(VectorStore vectorStore, ChatModel chatModel, RagProperties properties) {
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
        this.properties = properties;
    }

    public AskResponse ask(String question) {
        SearchRequest request = SearchRequest.builder()
                .query(question)
                .topK(properties.getTopK())
                .similarityThreshold(properties.getSimilarityThreshold())
                .build();
        List<Document> hits = vectorStore.similaritySearch(request);
        if (hits == null) {
            hits = List.of();
        }
        for (Document hit : hits) {
            log.debug("Retrieved chunk ticketId={} score={}", ticketId(hit), hit.getScore());
        }
        if (hits.isEmpty()) {
            return noMatch();
        }

        String answer = generate(question, hits);
        Set<String> cited = citedIds(answer);
        Map<String, Document> bestByTicket = highestScoring(hits);
        if (cited.isEmpty() || !bestByTicket.keySet().containsAll(cited)) {
            return noMatch();
        }

        List<AskSource> sources = new ArrayList<>();
        for (String ticketId : cited) {
            sources.add(toSource(bestByTicket.get(ticketId)));
        }
        return new AskResponse(answer, List.copyOf(sources), true);
    }

    private String generate(String question, List<Document> hits) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            if (i > 0) {
                context.append("\n\n");
            }
            String text = hits.get(i).getText();
            context.append(text == null ? "" : text);
        }
        String user = "Ticket context:\n" + context + "\n\nQuestion:\n" + question;
        Prompt prompt = new Prompt(List.of(new SystemMessage(SYSTEM_PROMPT), new UserMessage(user)));
        ChatResponse response = chatModel.call(prompt);
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }

    private static Map<String, Document> highestScoring(List<Document> hits) {
        Map<String, Document> best = new LinkedHashMap<>();
        for (Document hit : hits) {
            String ticketId = ticketId(hit);
            Document current = best.get(ticketId);
            if (current == null || score(hit) > score(current)) {
                best.put(ticketId, hit);
            }
        }
        return best;
    }

    private static Set<String> citedIds(String answer) {
        Set<String> ids = new LinkedHashSet<>();
        if (answer == null || answer.isBlank()) {
            return ids;
        }
        Matcher matcher = TICKET_ID.matcher(answer);
        while (matcher.find()) {
            ids.add(matcher.group());
        }
        return ids;
    }

    private static AskSource toSource(Document document) {
        String text = document.getText() == null ? "" : document.getText();
        return new AskSource(ticketId(document), titleFrom(text), statusFrom(document), score(document), snippetFrom(text));
    }

    private static String ticketId(Document document) {
        Object value = document.getMetadata().get("ticketId");
        if (value != null && !value.toString().isBlank()) {
            return value.toString();
        }
        String text = document.getText() == null ? "" : document.getText();
        Matcher matcher = TICKET_ID.matcher(text);
        return matcher.find() ? matcher.group() : "";
    }

    private static String statusFrom(Document document) {
        Object value = document.getMetadata().get("status");
        return value == null ? "" : value.toString();
    }

    private static double score(Document document) {
        Double score = document.getScore();
        return score == null ? 0.0 : score;
    }

    private static String titleFrom(String content) {
        String firstLine = content.split("\\R", 2)[0];
        String[] parts = firstLine.split(" \\| ");
        return parts.length >= 2 ? parts[1] : "";
    }

    private static String snippetFrom(String content) {
        String[] parts = content.split("\\R", 2);
        if (parts.length < 2) {
            return "";
        }
        String body = parts[1];
        if (body.length() <= SNIPPET_MAX) {
            return body;
        }
        return body.substring(0, SNIPPET_MAX);
    }

    private static AskResponse noMatch() {
        return new AskResponse(NO_MATCH_ANSWER, List.of(), false);
    }
}
