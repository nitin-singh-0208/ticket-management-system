package com.ticketmanagement.comment;

import com.ticketmanagement.comment.dto.CommentResponse;
import com.ticketmanagement.comment.dto.CreateCommentRequest;
import com.ticketmanagement.common.TicketNotFoundException;
import com.ticketmanagement.ticket.Ticket;
import com.ticketmanagement.ticket.TicketRepository;
import com.ticketmanagement.ticket.event.TicketChangedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CommentService(
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CommentResponse addComment(String ticketId, CreateCommentRequest request) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        Comment comment = new Comment();
        comment.setTicket(ticket);
        comment.setText(request.text());

        Comment saved = commentRepository.save(comment);
        eventPublisher.publishEvent(new TicketChangedEvent(ticket.getTicketId()));
        return toResponse(saved);
    }

    private static CommentResponse toResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getText(), comment.getCreatedAt());
    }
}
