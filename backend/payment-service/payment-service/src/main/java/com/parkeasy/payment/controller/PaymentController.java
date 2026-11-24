package com.parkeasy.payment.controller;

import com.parkeasy.payment.dto.PaymentRequest;
import com.parkeasy.payment.dto.PaymentResponse;
import com.parkeasy.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse pay(Authentication auth, @Valid @RequestBody PaymentRequest request) {
        return paymentService.pay((Long) auth.getPrincipal(), request);
    }

    @GetMapping("/my")
    public List<PaymentResponse> my(Authentication auth) {
        return paymentService.myPayments((Long) auth.getPrincipal());
    }
}