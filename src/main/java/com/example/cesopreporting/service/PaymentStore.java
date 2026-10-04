package com.example.cesopreporting.service;

import java.util.ArrayList;
import java.util.List;

import com.example.cesopreporting.model.PaymentRequest;

public class PaymentStore {

    private static final List<PaymentRequest> payments =
            new ArrayList<>();

    public static void addPayment(PaymentRequest payment) {
        payments.add(payment);
    }

    public static List<PaymentRequest> getPayments() {
        return payments;
    }

    public static int getCount() {
        return payments.size();
    }
}