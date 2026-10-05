package com.example.cesopreporting.service;

public class DipReferenceStore {

    private static String lastTransferTicketId;

    public static void saveTransferTicketId(String transferTicketId) {
        lastTransferTicketId = transferTicketId;
    }

    public static String getLastTransferTicketId() {
        return lastTransferTicketId;
    }
}