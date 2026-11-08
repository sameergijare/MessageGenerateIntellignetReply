package com.example.ticketgeneratereply.controller;


import com.example.ticketgeneratereply.db.TicketRepository;
import com.example.ticketgeneratereply.db.Message;
import com.example.ticketgeneratereply.db.Ticket;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/dashboard/tickets")
public class TicketDashboardController {

    private final TicketRepository ticketRepository;

    public TicketDashboardController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Renders the complete, initial frame wrapper layout view for the Agent Dashboard workspace.
     */
    @GetMapping
    public String getDashboard(Model model) {
        model.addAttribute("tickets", ticketRepository.findAll());
        return "dashboard/index";
    }

    /**
     * htmx Endpoint: Fetches and displays a specific ticket detail pane into the workspace frame.
     */
    @GetMapping("/{id}")
    public String getTicketDetail(@PathVariable("id") UUID id, Model model) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Requested support ticket not found."));

        model.addAttribute("ticket", ticket);
        // Extracts the latest thread message containing the active AI proposal draft
        Message latestMessage = ticket.getMessages().stream()
                .filter(m -> m.getAiProposedDraft() != null)
                .findFirst()
                .orElse(null);

        model.addAttribute("latestMessage", latestMessage);
        return "dashboard/partials/ticket-detail :: detail-pane";
    }

    /**
     * htmx Endpoint: Accepts instant alterations made to an AI draft and marks it as approved.
     * Retains the htmx response pattern by only rendering the specific message view fragment.
     */
    @PostMapping("/messages/{messageId}/approve")
    public String approveAiDraft(@PathVariable("messageId") UUID messageId,
                                 @RequestParam("editedDraftBody") String editedDraftBody,
                                 Model model) {
        // Business Logic Simulation: In production, lookup message via messageRepository
        // For compilation integrity against the shared schema mapping:
        Ticket ticket = ticketRepository.findAll().stream()
                .flatMap(t -> t.getMessages().stream())
                .filter(m -> m.getId().equals(messageId))
                .map(Message::getTicket)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Message node location not found."));

        Message message = ticket.getMessages().stream()
                .filter(m -> m.getId().equals(messageId))
                .findFirst()
                .get();

        // Commit modifications made by the verifying operator
        message.setAiProposedDraft(editedDraftBody);
        message.setIsDraftApproved(true);
        ticket.setStatus(Ticket.TicketStatus.PENDING); // Status moves out of review queue

        // Execute real-world SMTP/SendGrid delivery calls here for message.getAiProposedDraft()

        ticketRepository.save(ticket);

        model.addAttribute("ticket", ticket);
        model.addAttribute("latestMessage", message);

        // Tells htmx to patch only the isolated workspace inner section frame fragment
        return "dashboard/partials/ticket-detail :: draft-status-block";
    }
}

