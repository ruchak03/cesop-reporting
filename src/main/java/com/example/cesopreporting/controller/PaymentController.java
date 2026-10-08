package com.example.cesopreporting.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cesopreporting.model.PaymentRequest;
import com.example.cesopreporting.service.PaymentStore;
import com.example.cesopreporting.validator.PaymentValidator;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    // ---------------------------------------------------------
    // Collect payment
    // ---------------------------------------------------------

    @PostMapping
    public String addPayment(
            @RequestBody PaymentRequest payment) {

        // Validate payment before storing it
        String validationError =
                PaymentValidator.validate(payment);

        if (validationError != null) {

            return "Payment validation failed: "
                    + validationError;
        }


        // Store only valid payment
        PaymentStore.addPayment(payment);

        return "Payment collected successfully!";
    }


    // ---------------------------------------------------------
    // Get all payments
    // ---------------------------------------------------------

    @GetMapping
    public List<PaymentRequest> getPayments() {

        return PaymentStore.getPayments();
    }


    // ---------------------------------------------------------
    // Get payment count
    // ---------------------------------------------------------

    @GetMapping("/count")
    public int getPaymentCount() {

        return PaymentStore.getCount();
    }
}