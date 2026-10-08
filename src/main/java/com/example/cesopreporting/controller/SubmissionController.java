package com.example.cesopreporting.controller;

import java.io.File;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.cesopreporting.correction.CorrectionXmlGenerator;
import com.example.cesopreporting.model.PaymentRequest;
import com.example.cesopreporting.service.CorrectionStore;
import com.example.cesopreporting.service.DipReferenceStore;
import com.example.cesopreporting.service.PaymentStore;
import com.example.cesopreporting.submission.DipResubmissionGenerator;
import com.example.cesopreporting.submission.DipSubmissionGenerator;
import com.example.cesopreporting.validator.CesopXmlValidator;
import com.example.cesopreporting.validator.PaymentValidator;

@RestController
public class SubmissionController {

    @PostMapping("/submit")
    public String submitXml() {

        File xmlFile = new File("generated-cesop.xml");

        if (!xmlFile.exists()) {
            return "Submission failed: CESOP XML file not found.";
        }

        boolean valid =
                CesopXmlValidator.validate(
                        xmlFile.getAbsolutePath()
                );

        if (!valid) {
            return "Submission failed: CESOP XML is invalid.";
        }

        return "CESOP XML is valid and ready for DIP submission.";
    }


    @PostMapping("/submission/generate")
    public String generateSubmission() {

        try {

            String filePath =
                    DipSubmissionGenerator.generateDipXml();

            String transferTicketId =
                    DipReferenceStore.getLastTransferTicketId();

            return "DIP submission XML generated successfully.\n\n"
                    + "File: " + filePath + "\n"
                    + "Transfer Ticket ID: "
                    + transferTicketId;

        } catch (Exception e) {

            return "DIP submission generation failed: "
                    + e.getMessage();
        }
    }


    @GetMapping("/submission/ticket")
    public String getTransferTicket() {

        String transferTicketId =
                DipReferenceStore.getLastTransferTicketId();

        if (transferTicketId == null) {
            return "No DIP submission has been generated yet.";
        }

        return "Current Transfer Ticket ID: "
                + transferTicketId;
    }


    @PostMapping("/submission/resubmit")
    public String resubmitSubmission() {

        try {

            String previousTicket =
                    DipReferenceStore.getLastTransferTicketId();

            if (previousTicket == null) {

                return "Resubmission failed: "
                        + "No previous DIP submission found.";
            }

            String filePath =
                    DipResubmissionGenerator
                            .generateResubmissionXml();

            String newTicket =
                    DipReferenceStore.getLastTransferTicketId();

            return "DIP resubmission XML generated successfully.\n\n"
                    + "File: " + filePath + "\n"
                    + "Previous Transfer Ticket: "
                    + previousTicket + "\n"
                    + "New Transfer Ticket: "
                    + newTicket + "\n"
                    + "Reference ID: "
                    + previousTicket;

        } catch (Exception e) {

            return "DIP resubmission XML generation failed: "
                    + e.getMessage();
        }
    }


    /*
     * Store corrected payment data.
     *
     * Example:
     *
     * PUT /correction/PAY008
     *
     * The original payment remains unchanged.
     */
    @PutMapping("/correction/{paymentId}")
    public String updateCorrection(
            @PathVariable String paymentId,
            @RequestBody PaymentRequest correctedPayment) {

        try {

            // Check whether the original payment exists
            PaymentRequest originalPayment =
                    PaymentStore.getPayments()
                            .stream()
                            .filter(payment ->
                                    paymentId.equals(
                                            payment.getPaymentId()))
                            .findFirst()
                            .orElse(null);

            if (originalPayment == null) {

                return "Correction failed: Payment ID "
                        + paymentId
                        + " not found.";
            }

            // Make sure the corrected request uses
            // the same payment ID
            correctedPayment.setPaymentId(paymentId);

            // Validate corrected data
            String validationError =
                    PaymentValidator.validate(
                            correctedPayment);

            if (validationError != null) {

                return "Correction validation failed: "
                        + validationError;
            }

            // Store corrected data separately
            CorrectionStore.saveCorrection(
                    paymentId,
                    correctedPayment);

            return "Corrected payment data saved successfully.\n\n"
                    + "Payment ID: " + paymentId
                    + "\n"
                    + "You can now generate the correction XML.";

        } catch (Exception e) {

            return "Correction update failed: "
                    + e.getMessage();
        }
    }


    /*
     * Generate correction XML.
     */
    @PostMapping("/correction/{paymentId}")
    public String generateCorrection(
            @PathVariable String paymentId) {

        try {

            // Check whether corrected data exists
            if (!CorrectionStore.hasCorrection(paymentId)) {

                return "Correction failed: "
                        + "No corrected payment data found for "
                        + paymentId
                        + ". Please update the correction first.";
            }

            String filePath =
                    CorrectionXmlGenerator
                            .generateCorrectionXml(paymentId);

            return "CESOP correction XML generated successfully.\n\n"
                    + "Payment ID: " + paymentId + "\n"
                    + "File: " + filePath;

        } catch (Exception e) {

            return "Correction XML generation failed: "
                    + e.getMessage();
        }
    }


    @PostMapping("/correction/validate")
    public String validateCorrection() {

        File correctionFile =
                new File("generated-correction.xml");

        if (!correctionFile.exists()) {

            return "Correction validation failed: "
                    + "generated-correction.xml not found.";
        }

        boolean valid =
                CesopXmlValidator.validate(
                        correctionFile.getAbsolutePath());

        if (!valid) {

            return "Correction XML validation failed.";
        }

        return "CESOP correction XML is valid.";
    }


    @GetMapping("/submission-status")
    public String submissionStatus() {

        String transferTicketId =
                DipReferenceStore.getLastTransferTicketId();

        if (transferTicketId == null) {

            return "DIP submission status: NOT GENERATED";
        }

        return "DIP submission status: READY\n"
                + "Current Transfer Ticket ID: "
                + transferTicketId;
    }
}