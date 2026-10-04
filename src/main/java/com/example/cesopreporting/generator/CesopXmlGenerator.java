package com.example.cesopreporting.generator;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;

import eu.europa.ec.taxud.fiscalis.cesop.v1.CESOP;
import eu.europa.ec.taxud.fiscalis.cesop.v1.MessageSpecType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.MessageTypeIndicType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.MessageTypeType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.PaymentDataBodyType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.PSPIdType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.PSPType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.ReportingPeriodType;

import eu.taxud.commontypes.v1.PSPIdTypeType;
import eu.taxud.isotypes.v1.MSCountryCodeType;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

public class CesopXmlGenerator {

    public static String generateXml() {

        try {

            // -----------------------------
            // 1. Create root CESOP object
            // -----------------------------
            CESOP cesop = new CESOP();

            // CESOP schema version
            cesop.setVersion(new java.math.BigDecimal("4.03"));

            // -----------------------------
            // 2. Create MessageSpec
            // -----------------------------
            MessageSpecType messageSpec = new MessageSpecType();

            // Germany
            messageSpec.setTransmittingCountry(
                    MSCountryCodeType.DE
            );

            // Payment message
            messageSpec.setMessageType(
                    MessageTypeType.PMT
            );

            // No payment data for this first test
            messageSpec.setMessageTypeIndic(
                    MessageTypeIndicType.CESOP_102
            );

            // Unique UUID v4
            messageSpec.setMessageRefId(
                    UUID.randomUUID().toString()
            );

            // Reporting period
            ReportingPeriodType reportingPeriod =
                    new ReportingPeriodType();

            reportingPeriod.setQuarter(3);
            reportingPeriod.setYear("2026");

            messageSpec.setReportingPeriod(
                    reportingPeriod
            );

            // Timestamp
           OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

String timestamp = now
        .withNano((now.getNano() / 1_000_000) * 1_000_000)
        .toString();

messageSpec.setTimestamp(
        DatatypeFactory.newInstance()
                .newXMLGregorianCalendar(timestamp)
);

            // Add MessageSpec to CESOP
            cesop.setMessageSpec(messageSpec);

            // -----------------------------
            // 3. Create PaymentDataBody
            // -----------------------------
            PaymentDataBodyType paymentDataBody =
                    new PaymentDataBodyType();

            // -----------------------------
            // 4. Create Reporting PSP
            // -----------------------------
            PSPType reportingPSP =
                    new PSPType();

            PSPIdType pspId =
                    new PSPIdType();

            pspId.setValue("TRSYDEFFXXX");

            pspId.setPSPIdType(
                    PSPIdTypeType.BIC
            );

            reportingPSP.setPSPId(pspId);

            paymentDataBody.setReportingPSP(
                    reportingPSP
            );

            // Add PaymentDataBody
            cesop.setPaymentDataBody(
                    paymentDataBody
            );

            // -----------------------------
            // 5. Convert JAXB object to XML
            // -----------------------------
            JAXBContext context =
                    JAXBContext.newInstance(CESOP.class);

            Marshaller marshaller =
                    context.createMarshaller();

            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    true
            );

            marshaller.setProperty(
                    Marshaller.JAXB_ENCODING,
                    "UTF-8"
            );

            // Output file
            File outputFile =
                    new File(
                        "generated-cesop.xml"
                    );

            marshaller.marshal(
                    cesop,
                    outputFile
            );

            return outputFile.getAbsolutePath();

        } catch (Exception e) {

            throw new RuntimeException(
                    "CESOP XML generation failed: "
                    + e.getMessage(),
                    e
            );
        }
    }
}