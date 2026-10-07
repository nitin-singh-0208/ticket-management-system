package com.ticketmanagement.ticket;

import com.ticketmanagement.ticket.dto.CreateTicketRequest;
import com.ticketmanagement.ticket.dto.StatusChangeRequest;
import com.ticketmanagement.ticket.dto.TicketDetail;
import com.ticketmanagement.ticket.dto.UpdateTicketRequest;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketDetail> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketDetail detail = ticketService.createTicket(request);
        URI location = URI.create("/api/tickets/" + detail.ticketId());
        return ResponseEntity.created(location).body(detail);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketDetail> getTicket(@PathVariable String ticketId) {
        TicketDetail detail = ticketService.getTicket(ticketId);
        return ResponseEntity.ok(detail);
    }

    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketDetail> updateTicket(
            @PathVariable String ticketId,
            @Valid @RequestBody UpdateTicketRequest request) {
        TicketDetail detail = ticketService.updateTicket(ticketId, request);
        return ResponseEntity.ok(detail);
    }

    @PatchMapping("/{ticketId}/status")
    public ResponseEntity<TicketDetail> changeStatus(
            @PathVariable String ticketId,
            @Valid @RequestBody StatusChangeRequest request) {
        TicketDetail detail = ticketService.changeStatus(ticketId, request.status());
        return ResponseEntity.ok(detail);
    }
}
