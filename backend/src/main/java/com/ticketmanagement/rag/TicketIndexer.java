package com.ticketmanagement.rag;

import com.ticketmanagement.comment.Comment;
import com.ticketmanagement.comment.CommentRepository;
import com.ticketmanagement.common.TicketNotFoundException;
import com.ticketmanagement.ticket.Ticket;
import com.ticketmanagement.ticket.TicketRepository;
import com.ticketmanagement.ticket.event.TicketChangedEvent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@ConditionalOnProperty(name = "app.rag.indexing.enabled", havingValue = "true", matchIfMissing = true)
public class TicketIndexer {

    private static final Logger log = LoggerFactory.getLogger(TicketIndexer.class);

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final TicketDocumentMapper documentMapper;
    private final VectorStore vectorStore;
    private final ObjectProvider<TicketIndexer> self;

    public TicketIndexer(
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            TicketDocumentMapper documentMapper,
            VectorStore vectorStore,
            ObjectProvider<TicketIndexer> self) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.documentMapper = documentMapper;
        this.vectorStore = vectorStore;
        this.self = self;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTicketChanged(TicketChangedEvent event) {
        try {
            reindex(event.ticketId());
        } catch (RuntimeException exception) {
            log.warn("Failed to re-index ticket {}", event.ticketId(), exception);
        }
    }

    public void reindex(String ticketId) {
        List<Document> documents = self.getObject().loadDocuments(ticketId);
        vectorStore.delete(new FilterExpressionBuilder().eq("ticketId", ticketId).build());
        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
    }

    @Transactional(readOnly = true)
    public List<Document> loadDocuments(String ticketId) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        List<Comment> comments = commentRepository.findByTicket_TicketIdOrderByCreatedAtAscIdAsc(ticketId);
        return documentMapper.toDocuments(ticket, comments);
    }
}
