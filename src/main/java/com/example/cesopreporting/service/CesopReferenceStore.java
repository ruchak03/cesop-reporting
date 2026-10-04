package com.example.cesopreporting.service;

import java.util.HashMap;
import java.util.Map;

public class CesopReferenceStore {

    private static final Map<String, String> docRefIds =
            new HashMap<>();

    private static final Map<String, String> messageRefIds =
            new HashMap<>();

    public static void saveReference(
            String paymentId,
            String docRefId,
            String messageRefId) {

        docRefIds.put(paymentId, docRefId);
        messageRefIds.put(paymentId, messageRefId);
    }

    public static String getDocRefId(String paymentId) {
        return docRefIds.get(paymentId);
    }

    public static String getMessageRefId(String paymentId) {
        return messageRefIds.get(paymentId);
    }
}