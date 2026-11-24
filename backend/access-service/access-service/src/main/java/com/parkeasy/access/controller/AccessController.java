package com.parkeasy.access.controller;

import com.parkeasy.access.dto.ScanRequest;
import com.parkeasy.access.dto.ScanResponse;
import com.parkeasy.access.dto.TicketDetails;
import com.parkeasy.access.dto.TicketRequest;
import com.parkeasy.access.dto.TicketResponse;
import com.parkeasy.access.service.AccessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/access")
public class AccessController {

    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse createTicket(Authentication auth, @Valid @RequestBody TicketRequest request) {
        return accessService.createTicket((Long) auth.getPrincipal(), request.reservationId());
    }

    @GetMapping("/lookup")
    public TicketDetails lookup(Authentication auth, @RequestParam String token) {
        return accessService.lookup(auth, token);
    }

    @PostMapping("/scan")
    public ScanResponse scan(Authentication auth, @Valid @RequestBody ScanRequest request) {
        return accessService.scan(auth, request);
    }
}