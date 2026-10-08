package com.ticketmanagement.ticket;

import com.ticketmanagement.comment.Comment;
import com.ticketmanagement.comment.CommentRepository;
import com.ticketmanagement.comment.dto.CommentResponse;
import com.ticketmanagement.common.TicketNotFoundException;
import com.ticketmanagement.ticket.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.dto.TicketDetail;
import com.ticketmanagement.ticket.dto.TicketPage;
import com.ticketmanagement.ticket.dto.TicketSummary;
import com.ticketmanagement.ticket.dto.UpdateTicketRequest;
import com.ticketmanagement.ticket.event.TicketChangedEvent;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TicketService(
            TicketRepository ticketRepository,
            CommentRepository commentRepository,
            ApplicationEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.commentRepository = commentRepository;
        this.eventPublisher = eventPublisher;
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
        TicketDetail detail = toDetail(saved);
        eventPublisher.publishEvent(new TicketChangedEvent(saved.getTicketId()));
        return detail;
    }

    public TicketPage listTickets(String q, TicketStatus status, Integer page, Integer size) {
        String likePattern = TicketSpecifications.toLikePattern(q);
        Specification<Ticket> specification = TicketSpecifications.withFilters(likePattern, status);

        if (page == null && size == null) {
            List<Ticket> tickets = ticketRepository.findAll(specification);
            List<TicketSummary> content = tickets.stream().map(TicketService::toSummary).toList();
            int total = content.size();
            return new TicketPage(content, 0, total, total, total == 0 ? 0 : 1);
        }

        int pageNumber = page == null ? 0 : page;
        Page<Ticket> result = ticketRepository.findAll(specification, PageRequest.of(pageNumber, size));
        List<TicketSummary> content = result.getContent().stream().map(TicketService::toSummary).toList();
        return new TicketPage(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
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
        TicketDetail detail = toDetail(saved);
        eventPublisher.publishEvent(new TicketChangedEvent(saved.getTicketId()));
        return detail;
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
        TicketDetail detail = toDetail(saved);
        eventPublisher.publishEvent(new TicketChangedEvent(saved.getTicketId()));
        return detail;
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

    private static TicketSummary toSummary(Ticket ticket) {
        return new TicketSummary(
                ticket.getTicketId(),
                ticket.getTitle(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getAssignee());
    }
}
