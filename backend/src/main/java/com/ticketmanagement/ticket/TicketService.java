package com.ticketmanagement.ticket;

import com.ticketmanagement.common.TicketNotFoundException;
import com.ticketmanagement.ticket.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.dto.TicketDetail;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
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

    private static TicketDetail toDetail(Ticket ticket) {
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
                List.of()
        );
    }
}
