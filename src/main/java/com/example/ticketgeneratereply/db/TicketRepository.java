package com.example.ticketgeneratereply.db;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByStatus(Ticket.TicketStatus status);
    List<Ticket> findByTenantId(String tenantId);
}

