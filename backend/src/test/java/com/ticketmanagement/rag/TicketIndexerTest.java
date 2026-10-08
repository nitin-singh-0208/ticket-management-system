package com.ticketmanagement.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ticketmanagement.comment.CommentRepository;
import com.ticketmanagement.comment.CommentService;
import com.ticketmanagement.comment.dto.CreateCommentRequest;
import com.ticketmanagement.ticket.Ticket;
import com.ticketmanagement.ticket.TicketPriority;
import com.ticketmanagement.ticket.TicketRepository;
import com.ticketmanagement.ticket.TicketService;
import com.ticketmanagement.ticket.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.dto.TicketDetail;
import com.ticketmanagement.ticket.dto.UpdateTicketRequest;
import com.ticketmanagement.ticket.event.TicketChangedEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

class TicketIndexerTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Reindex {

        @Mock
        private VectorStore vectorStore;

        @Mock
        private TicketRepository ticketRepository;

        @Mock
        private CommentRepository commentRepository;

        private TicketIndexer indexer;

        @BeforeEach
        void setUp() {
            indexer = new TicketIndexer(
                    ticketRepository,
                    commentRepository,
                    new TicketDocumentMapper(),
                    vectorStore,
                    new ObjectProvider<>() {
                        @Override
                        public TicketIndexer getObject() {
                            return indexer;
                        }
                    });
        }

        @Test
        void reindexDeletesByTicketIdThenAddsFreshDocuments() {
            Ticket ticket = sampleTicket();
            when(ticketRepository.findByTicketId("TKT-1001")).thenReturn(Optional.of(ticket));
            when(commentRepository.findByTicket_TicketIdOrderByCreatedAtAscIdAsc("TKT-1001")).thenReturn(List.of());

            indexer.reindex("TKT-1001");

            Filter.Expression expected = new FilterExpressionBuilder().eq("ticketId", "TKT-1001").build();
            ArgumentCaptor<List<Document>> documents = ArgumentCaptor.captor();
            InOrder order = inOrder(vectorStore);
            order.verify(vectorStore).delete(expected);
            order.verify(vectorStore).add(documents.capture());
            assertFalse(documents.getValue().isEmpty());
            assertTrue(documents.getValue().get(0).getText().startsWith(
                    "Ticket TKT-1001 | Payment declined at checkout | status OPEN | priority HIGH | category Payment | section description\n"));
            assertEquals("TKT-1001", documents.getValue().get(0).getMetadata().get("ticketId"));
            verify(ticketRepository).findByTicketId("TKT-1001");
            verify(commentRepository).findByTicket_TicketIdOrderByCreatedAtAscIdAsc("TKT-1001");
        }

        @Test
        void reloadMethodIsReadOnlyTransactional() throws Exception {
            Method method = TicketIndexer.class.getMethod("loadDocuments", String.class);
            Transactional transactional = method.getAnnotation(Transactional.class);
            assertTrue(transactional != null && transactional.readOnly());
        }
    }

    @Nested
    @SpringJUnitConfig(ReadOnlyReload.Config.class)
    class ReadOnlyReload {

        @Autowired
        private TicketIndexer indexer;

        @Autowired
        private TicketRepository ticketRepository;

        @Autowired
        private CommentRepository commentRepository;

        @Autowired
        private VectorStore vectorStore;

        @Test
        void listenerReloadsTicketAndCommentsInsideReadOnlyTransaction() {
            Ticket ticket = sampleTicket();
            when(ticketRepository.findByTicketId("TKT-1001")).thenAnswer(invocation -> {
                assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
                assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
                return Optional.of(ticket);
            });
            when(commentRepository.findByTicket_TicketIdOrderByCreatedAtAscIdAsc("TKT-1001")).thenAnswer(invocation -> {
                assertTrue(TransactionSynchronizationManager.isActualTransactionActive());
                assertTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly());
                return List.of();
            });
            doAnswer(invocation -> {
                assertFalse(TransactionSynchronizationManager.isActualTransactionActive());
                return null;
            }).when(vectorStore).delete(any(Filter.Expression.class));

            indexer.onTicketChanged(new TicketChangedEvent("TKT-1001"));

            verify(ticketRepository).findByTicketId("TKT-1001");
            verify(commentRepository).findByTicket_TicketIdOrderByCreatedAtAscIdAsc("TKT-1001");
        }

        @Configuration
        @EnableTransactionManagement
        static class Config {

            @Bean
            TicketRepository ticketRepository() {
                return mock(TicketRepository.class);
            }

            @Bean
            CommentRepository commentRepository() {
                return mock(CommentRepository.class);
            }

            @Bean
            VectorStore vectorStore() {
                return mock(VectorStore.class);
            }

            @Bean
            TicketDocumentMapper ticketDocumentMapper() {
                return new TicketDocumentMapper();
            }

            @Bean
            PlatformTransactionManager transactionManager() {
                return new ReadOnlyCapableTransactionManager();
            }

            @Bean
            TicketIndexer ticketIndexer(
                    TicketRepository ticketRepository,
                    CommentRepository commentRepository,
                    TicketDocumentMapper ticketDocumentMapper,
                    VectorStore vectorStore,
                    ObjectProvider<TicketIndexer> self) {
                return new TicketIndexer(
                        ticketRepository, commentRepository, ticketDocumentMapper, vectorStore, self);
            }
        }
    }

    @Nested
    @SpringBootTest
    @Testcontainers
    @RecordApplicationEvents
    class WhenIndexingDisabled {

        @Container
        @ServiceConnection
        static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres"));

        @MockitoBean
        private EmbeddingModel embeddingModel;

        @MockitoBean
        private ChatModel chatModel;

        @Autowired
        private ApplicationContext applicationContext;

        @Autowired
        private TicketService ticketService;

        @Autowired
        private CommentService commentService;

        @Autowired
        private ApplicationEvents applicationEvents;

        @Test
        void ticketIndexerIsNotLoadedWhenIndexingDisabled() {
            assertTrue(applicationContext.getBeansOfType(TicketIndexer.class).isEmpty());
            assertTrue(applicationContext.getBeansOfType(VectorStoreBackfill.class).isEmpty());
        }

        @Test
        void updateTicketPublishesTicketChangedEvent() {
            TicketDetail created = ticketService.createTicket(new CreateTicketRequest(
                    "Payment declined at checkout",
                    "Card network returned a payment failure.",
                    TicketPriority.HIGH,
                    "Jordan Lee",
                    "Payment",
                    null));
            applicationEvents.clear();

            UpdateTicketRequest update = new UpdateTicketRequest();
            update.setDescription("Updated checkout failure details.");
            ticketService.updateTicket(created.ticketId(), update);

            List<TicketChangedEvent> events = applicationEvents.stream(TicketChangedEvent.class).toList();
            assertEquals(1, events.size());
            assertEquals(created.ticketId(), events.get(0).ticketId());
        }

        @Test
        void addCommentPublishesTicketChangedEvent() {
            TicketDetail created = ticketService.createTicket(new CreateTicketRequest(
                    "Shipment delayed",
                    "Package has not scanned in two days.",
                    TicketPriority.MEDIUM,
                    "Jordan Lee",
                    "Shipment",
                    null));
            applicationEvents.clear();

            commentService.addComment(created.ticketId(), new CreateCommentRequest("Carrier confirmed a hub delay."));

            List<TicketChangedEvent> events = applicationEvents.stream(TicketChangedEvent.class).toList();
            assertEquals(1, events.size());
            assertEquals(created.ticketId(), events.get(0).ticketId());
        }
    }

    private static Ticket sampleTicket() {
        Ticket ticket = new Ticket();
        try {
            Field field = Ticket.class.getDeclaredField("ticketId");
            field.setAccessible(true);
            field.set(ticket, "TKT-1001");
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
        ticket.setTitle("Payment declined at checkout");
        ticket.setDescription("Card network returned a payment failure during checkout.");
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setAssignee("Jordan Lee");
        ticket.setCategory("Payment");
        ticket.setStatus(com.ticketmanagement.ticket.TicketStatus.OPEN);
        return ticket;
    }

    private static final class ReadOnlyCapableTransactionManager extends AbstractPlatformTransactionManager {

        @Override
        protected Object doGetTransaction() {
            return new Object();
        }

        @Override
        protected void doBegin(Object transaction, org.springframework.transaction.TransactionDefinition definition) {
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
        }
    }
}
