package com.ticketmanagement.rag;

import com.ticketmanagement.ticket.Ticket;
import com.ticketmanagement.ticket.TicketRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.rag.indexing.enabled", havingValue = "true", matchIfMissing = true)
public class VectorStoreBackfill implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreBackfill.class);

    private final JdbcTemplate jdbcTemplate;
    private final TicketRepository ticketRepository;
    private final TicketIndexer ticketIndexer;

    public VectorStoreBackfill(
            JdbcTemplate jdbcTemplate,
            TicketRepository ticketRepository,
            TicketIndexer ticketIndexer) {
        this.jdbcTemplate = jdbcTemplate;
        this.ticketRepository = ticketRepository;
        this.ticketIndexer = ticketIndexer;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (storeHasChunks()) {
            log.info("Vector store already contains chunks; skipping ticket backfill");
            return;
        }
        List<Ticket> tickets = ticketRepository.findAll();
        log.info("Backfilling vector store from {} tickets", tickets.size());
        for (Ticket ticket : tickets) {
            try {
                ticketIndexer.reindex(ticket.getTicketId());
            } catch (RuntimeException exception) {
                log.error("Failed to backfill ticket {}", ticket.getTicketId(), exception);
            }
        }
    }

    private boolean storeHasChunks() {
        Long count = jdbcTemplate.queryForObject("select count(*) from vector_store", Long.class);
        return count != null && count > 0;
    }
}
