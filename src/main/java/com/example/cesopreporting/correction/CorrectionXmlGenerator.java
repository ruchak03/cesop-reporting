package com.example.cesopreporting.correction;

import java.io.File;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import eu.europa.ec.taxud.fiscalis.cesop.v1.CESOP;
import eu.europa.ec.taxud.fiscalis.cesop.v1.MessageSpecType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.MessageTypeIndicType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.MessageTypeType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.PaymentDataBodyType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.PSPIdType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.PSPType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.ReportedPayeeType;
import eu.europa.ec.taxud.fiscalis.cesop.v1.ReportingPeriodType;

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

import com.example.cesopreporting.model.PaymentRequest;
import com.example.cesopreporting.service.CesopReferenceStore;
import com.example.cesopreporting.service.PaymentStore;

public class CorrectionXmlGenerator {

    private static final String CESOP_NS =
            "urn:ec.europa.eu:taxud:fiscalis:cesop:v1";

    private static final String COMMON_NS =
            "urn:eu:taxud:commontypes:v1";

    private static final String XSI_NS =
            "http://www.w3.org/2001/XMLSchema-instance";

    public static String generateCorrectionXml(String paymentId) {

        try {

            // =========================================================
            // FIND ORIGINAL PAYMENT
            // =========================================================

            PaymentRequest originalPayment = null;

            for (PaymentRequest payment : PaymentStore.getPayments()) {

                if (paymentId.equals(payment.getPaymentId())) {
                    originalPayment = payment;
                    break;
                }
            }

            if (originalPayment == null) {

                throw new RuntimeException(
                        "Payment not found: " + paymentId
                );
            }

            // =========================================================
            // GET ORIGINAL CESOP REFERENCES
            // =========================================================

            String originalDocRefId =
                    CesopReferenceStore.getDocRefId(paymentId);

            String originalMessageRefId =
                    CesopReferenceStore.getMessageRefId(paymentId);

            if (originalDocRefId == null
                    || originalMessageRefId == null) {

                throw new RuntimeException(
                        "Original CESOP reference IDs not found for payment "
                                + paymentId
                                + ". Generate the initial CESOP XML first."
                );
            }

            // =========================================================
            // CESOP ROOT
            // =========================================================

            CESOP cesop = new CESOP();

            cesop.setVersion(
                    new BigDecimal("4.03")
            );

            // =========================================================
            // MESSAGE SPEC
            // =========================================================

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

            // New message reference ID
            String newMessageRefId =
                    UUID.randomUUID().toString();

            messageSpec.setMessageRefId(
                    newMessageRefId
            );

            // Reference to original message
            messageSpec.setCorrMessageRefId(
                    originalMessageRefId
            );

            // =========================================================
            // REPORTING PERIOD
            // =========================================================

            ReportingPeriodType period =
                    new ReportingPeriodType();

            period.setQuarter(3);
            period.setYear("2026");

            messageSpec.setReportingPeriod(period);

            // =========================================================
            // TIMESTAMP
            // =========================================================

            OffsetDateTime now =
                    OffsetDateTime.now(ZoneOffset.UTC);

            String timestamp =
                    now.withNano(
                            (now.getNano() / 1_000_000) * 1_000_000
                    ).toString();

            messageSpec.setTimestamp(
                    DatatypeFactory
                            .newInstance()
                            .newXMLGregorianCalendar(timestamp)
            );

            cesop.setMessageSpec(messageSpec);

            // =========================================================
            // PAYMENT DATA BODY
            // =========================================================

            PaymentDataBodyType paymentDataBody =
                    new PaymentDataBodyType();

            // =========================================================
            // REPORTING PSP
            // =========================================================

            PSPType reportingPSP =
                    new PSPType();

            PSPIdType pspId =
                    new PSPIdType();

            /*
             * Test/example BIC.
             * Do not use this as a real production identifier.
             */
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

            // =========================================================
            // CORRECTED PAYEE
            // =========================================================

            ReportedPayeeType payee =
                    new ReportedPayeeType();

            // ---------------------------------------------------------
            // Name
            // ---------------------------------------------------------

            NameType name =
                    new NameType();

            name.setValue(
                    originalPayment.getPayeeName()
            );

            name.setNameType(
                    NameTypeEnumType.BUSINESS
            );

            payee.getName().add(name);

            // ---------------------------------------------------------
            // Country
            // ---------------------------------------------------------

            payee.setCountry(
                    CountryCodeType.valueOf(
                            originalPayment.getPayeeCountry()
                    )
            );

            // ---------------------------------------------------------
            // Address
            // ---------------------------------------------------------

            AddressType address =
                    new AddressType();

            address.setCountryCode(
                    CountryCodeType.valueOf(
                            originalPayment.getPayeeCountry()
                    )
            );

            address.setAddressFree(
                    "Corrected address for test"
            );

            payee.getAddress().add(address);

            // =========================================================
            // DOC SPEC
            // =========================================================

            DocSpecType docSpec =
                    new DocSpecType();

            // CESOP2 = corrected data
            docSpec.setDocTypeIndic(
                    DocTypeIndicEnumType.CESOP_2
            );

            // New document reference ID
            String newDocRefId =
                    UUID.randomUUID().toString();

            docSpec.setDocRefId(
                    newDocRefId
            );

            // Reference to original document
            docSpec.setCorrDocRefId(
                    originalDocRefId
            );

            payee.setDocSpec(docSpec);

            // Add payee
            paymentDataBody
                    .getReportedPayee()
                    .add(payee);

            cesop.setPaymentDataBody(
                    paymentDataBody
            );

            // =========================================================
            // GENERATE BASIC XML
            // =========================================================

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

            File outputFile =
                    new File("generated-correction.xml");

            marshaller.marshal(
                    cesop,
                    outputFile
            );

            // =========================================================
            // FIX CESOP ELEMENT ORDER
            // =========================================================
            //
            // Required order inside ReportedPayee:
            //
            // Name
            // Country
            // Address
            // TAXIdentification
            // AccountIdentifier
            // ReportedTransaction
            // DocSpec
            //
            // JAXB creates DocSpec before we add the transaction.
            // Therefore we use DOM to place AccountIdentifier and
            // ReportedTransaction before DocSpec.
            // =========================================================

            fixCorrectionElementOrder(
                    outputFile,
                    originalPayment
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

    // ================================================================
    // FIX CORRECTION XML ELEMENT ORDER
    // ================================================================

    private static void fixCorrectionElementOrder(
            File xmlFile,
            PaymentRequest payment)
            throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        Document document =
                factory
                        .newDocumentBuilder()
                        .parse(xmlFile);

        Element root =
                document.getDocumentElement();

        // ---------------------------------------------------------
        // Find ReportedPayee
        // ---------------------------------------------------------

        Element reportedPayee =
                findFirstElement(
                        root,
                        "ReportedPayee"
                );

        if (reportedPayee == null) {

            throw new RuntimeException(
                    "ReportedPayee not found in correction XML."
            );
        }

        // ---------------------------------------------------------
        // Find DocSpec
        // ---------------------------------------------------------

        Element docSpec =
                findFirstElement(
                        reportedPayee,
                        "DocSpec"
                );

        if (docSpec == null) {

            throw new RuntimeException(
                    "DocSpec not found in correction XML."
            );
        }

        // ---------------------------------------------------------
        // AccountIdentifier
        // ---------------------------------------------------------

        Element accountIdentifier =
                document.createElementNS(
                        CESOP_NS,
                        "cesop:AccountIdentifier"
                );

        accountIdentifier.setAttributeNS(
                XSI_NS,
                "xsi:nil",
                "true"
        );

        // ---------------------------------------------------------
        // Create ReportedTransaction
        // ---------------------------------------------------------

        Element transaction =
                document.createElementNS(
                        CESOP_NS,
                        "cesop:ReportedTransaction"
                );

        transaction.setAttribute(
                "IsRefund",
                "false"
        );

        // ---------------------------------------------------------
        // TransactionIdentifier
        // ---------------------------------------------------------

        Element transactionIdentifier =
                document.createElementNS(
                        CESOP_NS,
                        "cesop:TransactionIdentifier"
                );

        transactionIdentifier.setTextContent(
                payment.getPaymentId()
        );

        transaction.appendChild(
                transactionIdentifier
        );

        // ---------------------------------------------------------
        // DateTime
        // ---------------------------------------------------------

        Element dateTime =
                document.createElementNS(
                        CESOP_NS,
                        "cesop:DateTime"
                );

        dateTime.setAttribute(
                "transactionDateType",
                "CESOP701"
        );

        dateTime.setTextContent(
                payment.getPaymentDate()
                        + "T00:00:00Z"
        );

        transaction.appendChild(
                dateTime
        );

        // ---------------------------------------------------------
        // Amount
        // ---------------------------------------------------------

        Element amount =
                document.createElementNS(
                        CESOP_NS,
                        "cesop:Amount"
                );

        amount.setAttribute(
                "currency",
                payment.getCurrency()
        );

        amount.setTextContent(
                payment.getAmount()
        );

        transaction.appendChild(amount);

        // ---------------------------------------------------------
        // Physical premises
        // ---------------------------------------------------------

        Element physicalPremises =
                document.createElementNS(
                        CESOP_NS,
                        "cesop:InitiatedAtPhysicalPremisesOfMerchant"
                );

        physicalPremises.setTextContent(
                "false"
        );

        transaction.appendChild(
                physicalPremises
        );

        // ---------------------------------------------------------
        // Payer Member State
        // ---------------------------------------------------------

        if (payment.getPayerCountry() == null
                || payment.getPayerCountry().isBlank()) {

            throw new RuntimeException(
                    "Payer country is required for payment "
                            + payment.getPaymentId()
            );
        }

        Element payerMS =
                document.createElementNS(
                        CESOP_NS,
                        "cesop:PayerMS"
                );

        payerMS.setAttribute(
                "PayerMSSource",
                "Other"
        );

        payerMS.setTextContent(
                payment.getPayerCountry()
        );

        transaction.appendChild(payerMS);

        // =========================================================
        // INSERT IN CORRECT ORDER
        // =========================================================
        //
        // AccountIdentifier must be before DocSpec.
        // ReportedTransaction must also be before DocSpec.
        // =========================================================

        reportedPayee.insertBefore(
                accountIdentifier,
                docSpec
        );

        reportedPayee.insertBefore(
                transaction,
                docSpec
        );

        // =========================================================
        // NAMESPACE DECLARATIONS
        // =========================================================

        root.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:cesop",
                CESOP_NS
        );

        root.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:cm",
                COMMON_NS
        );

        root.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:xsi",
                XSI_NS
        );

        // =========================================================
        // SAVE XML
        // =========================================================

        TransformerFactory transformerFactory =
                TransformerFactory.newInstance();

        Transformer transformer =
                transformerFactory.newTransformer();

        transformer.setOutputProperty(
                OutputKeys.INDENT,
                "yes"
        );

        transformer.setOutputProperty(
                OutputKeys.ENCODING,
                "UTF-8"
        );

        transformer.transform(
                new DOMSource(document),
                new StreamResult(xmlFile)
        );
    }

    // ================================================================
    // FIND FIRST ELEMENT
    // ================================================================

    private static Element findFirstElement(
            Element parent,
            String localName) {

        org.w3c.dom.NodeList nodes =
                parent.getElementsByTagNameNS(
                        CESOP_NS,
                        localName
                );

        if (nodes.getLength() == 0) {
            return null;
        }

        return (Element) nodes.item(0);
    }
}