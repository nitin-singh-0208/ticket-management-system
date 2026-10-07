package com.ticketmanagement.ticket;

import com.ticketmanagement.comment.Comment;
import com.ticketmanagement.comment.CommentRepository;
import com.ticketmanagement.comment.dto.CommentResponse;
import com.ticketmanagement.common.TicketNotFoundException;
import com.ticketmanagement.ticket.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.dto.TicketDetail;
import com.ticketmanagement.ticket.dto.UpdateTicketRequest;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;

    public TicketService(TicketRepository ticketRepository, CommentRepository commentRepository) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public TicketDetail createTicket(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setTitle(request.title());
        ticket.setDescription(request.description());
        ticket.setPriority(request.priority());
        ticket.setAssignee(request.assignee());
        ticket.setCategory(request.category());
        ticket.setResolutionNotes(request.resolutionNotes());
        ticket.setStatus(TicketStatus.OPEN);

        Ticket saved = ticketRepository.save(ticket);
        return toDetail(saved);
    }

    public TicketDetail getTicket(String ticketId) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));
        return toDetail(ticket);
    }

    @Transactional
    public TicketDetail updateTicket(String ticketId, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (request.title() != null) {
            ticket.setTitle(request.title());
        }
        if (request.description() != null) {
            ticket.setDescription(request.description());
        }
        if (request.priority() != null) {
            ticket.setPriority(request.priority());
        }
        if (request.assignee() != null) {
            ticket.setAssignee(request.assignee());
        }
        if (request.resolutionNotes() != null) {
            ticket.setResolutionNotes(request.resolutionNotes().isEmpty() ? null : request.resolutionNotes());
        }

        Ticket saved = ticketRepository.save(ticket);
        return toDetail(saved);
    }

    @Transactional
    public TicketDetail changeStatus(String ticketId, TicketStatus targetStatus) {
        Ticket ticket = ticketRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new TicketNotFoundException(ticketId));

        if (!ticket.getStatus().allowedNext().contains(targetStatus)) {
            throw new StatusChangeNotAllowedException(ticket.getStatus(), targetStatus);
        }

        ticket.setStatus(targetStatus);
        Ticket saved = ticketRepository.save(ticket);
        return toDetail(saved);
    }

    private TicketDetail toDetail(Ticket ticket) {
        List<CommentResponse> comments = commentRepository
                .findByTicket_TicketIdOrderByCreatedAtAscIdAsc(ticket.getTicketId())
                .stream()
                .map(TicketService::toCommentResponse)
                .toList();

        return new TicketDetail(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getAssignee(),
                ticket.getCategory(),
                ticket.getResolutionNotes(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                comments,
                new ArrayList<>(ticket.getStatus().allowedNext())
        );
    }

    private static CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getText(), comment.getCreatedAt());
    }
}
