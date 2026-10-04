package com.example.cesopreporting.correction;
import eu.taxud.isotypes.v1.MSCountryCodeType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.CESOP;
import eu.europa.ec.taxud.fiscalis.cesop.v1.MessageSpecType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.MessageTypeIndicType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.MessageTypeType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.PaymentDataBodyType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.PSPIdType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.PSPType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.ReportedPayeeType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.ReportingPeriodType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.TAXIdentifierType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.AccountIdentifierType;

import eu.taxud.commontypes.v1.AddressType;
import eu.taxud.commontypes.v1.DocSpecType;
import eu.taxud.commontypes.v1.DocTypeIndicEnumType;
import eu.taxud.commontypes.v1.NameType;
import eu.taxud.commontypes.v1.NameTypeEnumType;
import eu.taxud.commontypes.v1.PSPIdTypeType;
import eu.taxud.isotypes.v1.CountryCodeType;
import eu.taxud.isotypes.v1.MSCountryCodeType;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import javax.xml.datatype.DatatypeFactory;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class CorrectionXmlGenerator {

    public static String generateCorrectionXml(
            String originalMessageRefId,
            String originalDocRefId) {

        try {

            // =========================================
            // CESOP root
            // =========================================

            CESOP cesop = new CESOP();

            cesop.setVersion(
                    new java.math.BigDecimal("4.03")
            );


            // =========================================
            // MessageSpec
            // =========================================

            MessageSpecType messageSpec =
                    new MessageSpecType();

            messageSpec.setTransmittingCountry(
        MSCountryCodeType.DE
);

            messageSpec.setMessageType(
                    MessageTypeType.PMT
            );

            // CESOP101 = correction
            messageSpec.setMessageTypeIndic(
                    MessageTypeIndicType.CESOP_101
            );

            // New message ID
            messageSpec.setMessageRefId(
                    UUID.randomUUID().toString()
            );

            // Original message ID
            messageSpec.setCorrMessageRefId(
                    originalMessageRefId
            );


            // =========================================
            // SAME reporting period as original
            // =========================================

            ReportingPeriodType period =
                    new ReportingPeriodType();

            period.setQuarter(3);
            period.setYear("2026");

            messageSpec.setReportingPeriod(period);


            // =========================================
            // Timestamp
            // =========================================

            OffsetDateTime now =
                    OffsetDateTime.now(ZoneOffset.UTC);

            String timestamp = String.format(
                    "%04d-%02d-%02dT%02d:%02d:%02d.%03dZ",
                    now.getYear(),
                    now.getMonthValue(),
                    now.getDayOfMonth(),
                    now.getHour(),
                    now.getMinute(),
                    now.getSecond(),
                    now.getNano() / 1_000_000
            );

            messageSpec.setTimestamp(
                    DatatypeFactory
                            .newInstance()
                            .newXMLGregorianCalendar(
                                    timestamp
                            )
            );

            cesop.setMessageSpec(messageSpec);


            // =========================================
            // PaymentDataBody
            // =========================================

            PaymentDataBodyType paymentDataBody =
                    new PaymentDataBodyType();


            // =========================================
            // Reporting PSP
            // =========================================

            PSPType reportingPSP =
                    new PSPType();

            PSPIdType pspId =
                    new PSPIdType();

            pspId.setValue(
                    "TRSYDEFFXXX"
            );

            pspId.setPSPIdType(
                    PSPIdTypeType.BIC
            );

            reportingPSP.setPSPId(pspId);

            paymentDataBody.setReportingPSP(
                    reportingPSP
            );


            // =========================================
            // Corrected Payee
            // =========================================

            ReportedPayeeType payee =
                    new ReportedPayeeType();


            // -----------------------------------------
            // Name
            // -----------------------------------------

            NameType name =
                    new NameType();

            name.setValue(
                    "Corrected Test Payee"
            );

            name.setNameType(
                    NameTypeEnumType.LEGAL
            );

            payee.getName().add(name);


            // -----------------------------------------
            // Country
            // -----------------------------------------

            payee.setCountry(
                    CountryCodeType.DE
            );


            // -----------------------------------------
            // Address
            // -----------------------------------------

            AddressType address =
                    new AddressType();

            address.setCountryCode(
                    CountryCodeType.DE
            );

            address.setAddressFree(
                    "Test Street 1, 10115 Berlin"
            );

            payee.getAddress().add(address);


            // -----------------------------------------
            // TAX Identification
            // -----------------------------------------

            TAXIdentifierType tax =
                    new TAXIdentifierType();

            payee.setTAXIdentification(tax);


            // -----------------------------------------
            // Account Identifier
            // -----------------------------------------

            AccountIdentifierType account =
                    new AccountIdentifierType();

            account.setValue(
                    "DE89370400440532013000"
            );

            account.setCountryCode(
                    CountryCodeType.DE
            );

            payee.getAccountIdentifier()
                    .add(account);


            // =========================================
            // DocSpec
            // =========================================

            DocSpecType docSpec =
                    new DocSpecType();

            // CESOP2 = corrected data
            docSpec.setDocTypeIndic(
                    DocTypeIndicEnumType.CESOP_2
            );

            // New unique DocRefId
            docSpec.setDocRefId(
                    UUID.randomUUID().toString()
            );

            // Original payee DocRefId
            docSpec.setCorrDocRefId(
                    originalDocRefId
            );

            payee.setDocSpec(docSpec);


            // Add corrected payee
            paymentDataBody
                    .getReportedPayee()
                    .add(payee);

            cesop.setPaymentDataBody(
                    paymentDataBody
            );


            // =========================================
            // Generate XML
            // =========================================

            JAXBContext context =
                    JAXBContext.newInstance(
                            CESOP.class
                    );

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


            File outputFile =
                    new File(
                            "generated-correction.xml"
                    );

            marshaller.marshal(
                    cesop,
                    outputFile
            );

            return outputFile.getAbsolutePath();

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Correction XML generation failed: "
                    + e.getMessage(),
                    e
            );
        }
    }
}