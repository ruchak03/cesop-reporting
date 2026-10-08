package com.example.cesopreporting.validator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import com.example.cesopreporting.model.PaymentRequest;

public class PaymentValidator {

    private PaymentValidator() {
        // Utility class
    }

    public static String validate(PaymentRequest payment) {

        if (payment == null) {
            return "Payment data is required.";
        }

        // ---------------------------------------------------------
        // Payment ID
        // ---------------------------------------------------------

        if (isBlank(payment.getPaymentId())) {
            return "Payment ID is required.";
        }


        // ---------------------------------------------------------
        // Payee Name
        // ---------------------------------------------------------

        if (isBlank(payment.getPayeeName())) {
            return "Payee name is required.";
        }


        // ---------------------------------------------------------
        // Payee Country
        // ---------------------------------------------------------

        if (isBlank(payment.getPayeeCountry())) {
            return "Payee country is required.";
        }

        if (payment.getPayeeCountry().length() != 2) {
            return "Payee country must be a 2-letter country code.";
        }


        // ---------------------------------------------------------
        // Payer Country
        // ---------------------------------------------------------

        if (isBlank(payment.getPayerCountry())) {
            return "Payer country is required.";
        }

        if (payment.getPayerCountry().length() != 2) {
            return "Payer country must be a 2-letter country code.";
        }


        // ---------------------------------------------------------
        // Amount
        // ---------------------------------------------------------

        if (isBlank(payment.getAmount())) {
            return "Amount is required.";
        }

        try {

            BigDecimal amount =
                    new BigDecimal(payment.getAmount());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                return "Amount must be greater than zero.";
            }

        } catch (NumberFormatException e) {

            return "Amount must be a valid number.";
        }


        // ---------------------------------------------------------
        // Currency
        // ---------------------------------------------------------

        if (isBlank(payment.getCurrency())) {
            return "Currency is required.";
        }

        if (payment.getCurrency().length() != 3) {
            return "Currency must be a 3-letter currency code.";
        }


        // ---------------------------------------------------------
        // Payment Date
        // ---------------------------------------------------------

        if (isBlank(payment.getPaymentDate())) {
            return "Payment date is required.";
        }

        try {

            LocalDate.parse(
                    payment.getPaymentDate()
            );

        } catch (DateTimeParseException e) {

            return "Payment date must be in YYYY-MM-DD format.";
        }


        // ---------------------------------------------------------
        // Payment Method
        // ---------------------------------------------------------

        if (isBlank(payment.getPaymentMethod())) {
            return "Payment method is required.";
        }

        String method =
                payment.getPaymentMethod();

        if (!method.equalsIgnoreCase("CARD")
                && !method.equalsIgnoreCase("BANK_TRANSFER")
                && !method.equalsIgnoreCase("DIRECT_DEBIT")
                && !method.equalsIgnoreCase("OTHER")) {

            return "Invalid payment method.";
        }


        // ---------------------------------------------------------
        // All validations passed
        // ---------------------------------------------------------

        return null;
    }


    private static boolean isBlank(String value) {

        return value == null
                || value.trim().isEmpty();
    }
}