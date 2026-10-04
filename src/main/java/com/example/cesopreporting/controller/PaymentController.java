package com.example.cesopreporting.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.cesopreporting.model.PaymentRequest;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final List<PaymentRequest> payments = new ArrayList<>();

    @PostMapping
    public String addPayment(@RequestBody PaymentRequest payment) {
        payments.add(payment);
        return "Payment collected successfully!";
    }

    @GetMapping
    public List<PaymentRequest> getPayments() {
        return payments;
    }

    @GetMapping("/count")
    public int getPaymentCount() {
        return payments.size();
    }
}