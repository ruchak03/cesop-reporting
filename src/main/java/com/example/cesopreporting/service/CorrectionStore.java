package com.example.cesopreporting.service;

import java.util.HashMap;
import java.util.Map;

import com.example.cesopreporting.model.PaymentRequest;

public class CorrectionStore {

    private static final Map<String, PaymentRequest> corrections =
            new HashMap<>();

    private CorrectionStore() {
        // Utility class
    }

    public static void saveCorrection(
            String paymentId,
            PaymentRequest correctedPayment) {

        corrections.put(paymentId, correctedPayment);
    }

    public static PaymentRequest getCorrection(String paymentId) {
        return corrections.get(paymentId);
    }

    public static boolean hasCorrection(String paymentId) {
        return corrections.containsKey(paymentId);
    }
}