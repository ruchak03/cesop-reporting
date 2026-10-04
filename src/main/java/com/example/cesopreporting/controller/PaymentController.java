package com.example.cesopreporting.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.cesopreporting.model.PaymentRequest;
import com.example.cesopreporting.service.PaymentStore;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @PostMapping
    public String addPayment(
            @RequestBody PaymentRequest payment) {

        PaymentStore.addPayment(payment);

        return "Payment collected successfully!";
    }

    @GetMapping
    public List<PaymentRequest> getPayments() {

        return PaymentStore.getPayments();
    }

    @GetMapping("/count")
    public int getPaymentCount() {

        return PaymentStore.getCount();
    }
}