package com.example.cesopreporting.generator;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import org.w3c.dom.Node;

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

import com.example.cesopreporting.model.PaymentRequest;
import com.example.cesopreporting.service.CesopReferenceStore;
import com.example.cesopreporting.service.PaymentStore;

public class CesopXmlGenerator {

    private static final String CESOP_NS =
            "urn:ec.europa.eu:taxud:fiscalis:cesop:v1";

    private static final String COMMON_NS =
            "urn:eu:taxud:commontypes:v1";

    private static final String XSI_NS =
            "http://www.w3.org/2001/XMLSchema-instance";

    public static String generateXml() {

        try {

            List<PaymentRequest> payments =
                    PaymentStore.getPayments();

            // =========================================
            // 1. Create CESOP header
            // =========================================

            CESOP cesop = new CESOP();

            cesop.setVersion(
                    new BigDecimal("4.03")
            );

            MessageSpecType messageSpec =
                    new MessageSpecType();

            messageSpec.setTransmittingCountry(
                    MSCountryCodeType.DE
            );

            messageSpec.setMessageType(
                    MessageTypeType.PMT
            );

            /*
             * CESOP100 = new payment data
             * CESOP102 = no data to report
             */

            if (payments.isEmpty()) {

                messageSpec.setMessageTypeIndic(
                        MessageTypeIndicType.CESOP_102
                );

            } else {

                messageSpec.setMessageTypeIndic(
                        MessageTypeIndicType.CESOP_100
                );
            }

            // Generate and remember the MessageRefId

            String messageRefId =
                    UUID.randomUUID().toString();

            messageSpec.setMessageRefId(
                    messageRefId
            );

            // =========================================
            // Reporting period
            // =========================================

            ReportingPeriodType reportingPeriod =
                    new ReportingPeriodType();

            reportingPeriod.setQuarter(3);
            reportingPeriod.setYear("2026");

            messageSpec.setReportingPeriod(
                    reportingPeriod
            );

            // =========================================
            // Timestamp
            // =========================================

            OffsetDateTime now =
                    OffsetDateTime.now(ZoneOffset.UTC);

            String timestamp =
                    now.withNano(
                            (now.getNano() / 1_000_000)
                                    * 1_000_000
                    ).toString();

            messageSpec.setTimestamp(
                    DatatypeFactory
                            .newInstance()
                            .newXMLGregorianCalendar(
                                    timestamp
                            )
            );

            cesop.setMessageSpec(
                    messageSpec
            );

            // =========================================
            // 2. PaymentDataBody
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

            /*
             * Example/test BIC only.
             * Not a production credential.
             */

            pspId.setValue(
                    "TRSYDEFFXXX"
            );

            pspId.setPSPIdType(
                    PSPIdTypeType.BIC
            );

            reportingPSP.setPSPId(
                    pspId
            );

            paymentDataBody.setReportingPSP(
                    reportingPSP
            );

            cesop.setPaymentDataBody(
                    paymentDataBody
            );

            // =========================================
            // 3. Convert JAXB object to XML
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
                    new File("generated-cesop.xml");

            marshaller.marshal(
                    cesop,
                    outputFile
            );

            // =========================================
            // 4. Add payment data
            // =========================================

            if (!payments.isEmpty()) {

                addPaymentData(
                        outputFile,
                        payments,
                        messageRefId
                );
            }

            return outputFile.getAbsolutePath();

        } catch (Exception e) {

            throw new RuntimeException(
                    "CESOP XML generation failed: "
                            + e.getMessage(),
                    e
            );
        }
    }

    private static void addPaymentData(
            File xmlFile,
            List<PaymentRequest> payments,
            String messageRefId)
            throws Exception {

        // =========================================
        // Read generated XML
        // =========================================

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        Document document =
                factory.newDocumentBuilder()
                        .parse(xmlFile);

        Element root =
                document.getDocumentElement();

        // =========================================
        // Find PaymentDataBody
        // =========================================

        Element paymentDataBody =
                findChild(
                        root,
                        "PaymentDataBody"
                );

        if (paymentDataBody == null) {

            throw new RuntimeException(
                    "PaymentDataBody not found in CESOP XML."
            );
        }

        // =========================================
        // Group payments by payee
        // =========================================

        Map<String, List<PaymentRequest>> groupedPayments =
                new LinkedHashMap<>();

        for (PaymentRequest payment : payments) {

            String key =
                    payment.getPayeeName()
                            + "|"
                            + payment.getPayeeCountry();

            groupedPayments
                    .computeIfAbsent(
                            key,
                            k -> new java.util.ArrayList<>()
                    )
                    .add(payment);
        }

        // =========================================
        // Create ReportedPayee
        // =========================================

        for (List<PaymentRequest> payeePayments
                : groupedPayments.values()) {

            PaymentRequest firstPayment =
                    payeePayments.get(0);

            Element reportedPayee =
                    document.createElementNS(
                            CESOP_NS,
                            "cesop:ReportedPayee"
                    );

            // =====================================
            // Name
            // =====================================

            Element name =
                    document.createElementNS(
                            CESOP_NS,
                            "cesop:Name"
                    );

            name.setAttribute(
                    "nameType",
                    "BUSINESS"
            );

            name.setTextContent(
                    firstPayment.getPayeeName()
            );

            reportedPayee.appendChild(
                    name
            );

            // =====================================
            // Country
            // =====================================

            Element country =
                    document.createElementNS(
                            CESOP_NS,
                            "cesop:Country"
                    );

            country.setTextContent(
                    firstPayment.getPayeeCountry()
            );

            reportedPayee.appendChild(
                    country
            );

            // =====================================
            // Address
            // =====================================

            Element address =
                    document.createElementNS(
                            CESOP_NS,
                            "cesop:Address"
                    );

            address.setAttributeNS(
                    XSI_NS,
                    "xsi:nil",
                    "true"
            );

            reportedPayee.appendChild(
                    address
            );

            // =====================================
            // TAXIdentification
            // =====================================

            Element taxIdentification =
                    document.createElementNS(
                            CESOP_NS,
                            "cesop:TAXIdentification"
                    );

            taxIdentification.setAttributeNS(
                    XSI_NS,
                    "xsi:nil",
                    "true"
            );

            reportedPayee.appendChild(
                    taxIdentification
            );

            // =====================================
            // AccountIdentifier
            // =====================================

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

            reportedPayee.appendChild(
                    accountIdentifier
            );

            // =====================================
            // Transactions
            // =====================================

            for (PaymentRequest payment
                    : payeePayments) {

                Element transaction =
                        createTransaction(
                                document,
                                payment
                        );

                reportedPayee.appendChild(
                        transaction
                );
            }

            // =====================================
            // DocSpec
            // =====================================

            Element docSpec =
                    document.createElementNS(
                            CESOP_NS,
                            "cesop:DocSpec"
                    );

            Element docTypeIndic =
                    document.createElementNS(
                            COMMON_NS,
                            "cm:DocTypeIndic"
                    );

            docTypeIndic.setTextContent(
                    "CESOP1"
            );

            docSpec.appendChild(
                    docTypeIndic
            );

            // Generate and remember original DocRefId

            String generatedDocRefId =
                    UUID.randomUUID().toString();

            Element docRefId =
                    document.createElementNS(
                            COMMON_NS,
                            "cm:DocRefId"
                    );

            docRefId.setTextContent(
                    generatedDocRefId
            );

            docSpec.appendChild(
                    docRefId
            );

            reportedPayee.appendChild(
                    docSpec
            );

            // =====================================
            // Save references for correction
            // =====================================

            for (PaymentRequest payment
                    : payeePayments) {

                CesopReferenceStore.saveReference(
                        payment.getPaymentId(),
                        generatedDocRefId,
                        messageRefId
                );
            }

            // =====================================
            // Add ReportedPayee
            // =====================================

            paymentDataBody.appendChild(
                    reportedPayee
            );
        }

        // =========================================
        // Namespace declarations
        // =========================================

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

        // =========================================
        // Save XML
        // =========================================

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

    private static Element createTransaction(
            Document document,
            PaymentRequest payment) {

        Element transaction =
                document.createElementNS(
                        CESOP_NS,
                        "cesop:ReportedTransaction"
                );

        transaction.setAttribute(
                "IsRefund",
                "false"
        );

        // =========================================
        // TransactionIdentifier
        // =========================================

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

        // =========================================
        // DateTime
        // =========================================

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

        // =========================================
        // Amount
        // =========================================

        Element amount =
                document.createElementNS(
                        CESOP_NS,
                        "cesop:Amount"
                );

        amount.setAttribute(
                "currency",
                payment.getCurrency()
        );

        /*
         * CESOP requires the amount to have exactly
         * two digits after the decimal point.
         *
         * Examples:
         * 200    -> 200.00
         * 50.5   -> 50.50
         * 10.25  -> 10.25
         */

        BigDecimal amountValue =
                new BigDecimal(payment.getAmount());

        amountValue = amountValue.setScale(
                2,
                RoundingMode.HALF_UP
        );

        amount.setTextContent(
                amountValue.toPlainString()
        );

        transaction.appendChild(
                amount
        );

        // =========================================
        // PaymentMethod
        // =========================================

        if (payment.getPaymentMethod() != null
                && !payment.getPaymentMethod().isBlank()) {

            Element paymentMethod =
                    document.createElementNS(
                            CESOP_NS,
                            "cesop:PaymentMethod"
                    );

            Element paymentMethodType =
                    document.createElementNS(
                            COMMON_NS,
                            "cm:PaymentMethodType"
                    );

            String method =
                    payment.getPaymentMethod();

            if ("CARD".equalsIgnoreCase(method)) {

                paymentMethodType.setTextContent(
                        "Card payment"
                );

            } else if ("BANK_TRANSFER".equalsIgnoreCase(method)) {

                paymentMethodType.setTextContent(
                        "Bank transfer"
                );

            } else if ("DIRECT_DEBIT".equalsIgnoreCase(method)) {

                paymentMethodType.setTextContent(
                        "Direct debit"
                );

            } else if ("OTHER".equalsIgnoreCase(method)) {

                paymentMethodType.setTextContent(
                        "Other"
                );

            } else {

                throw new RuntimeException(
                        "Unsupported payment method: "
                                + method
                );
            }

            paymentMethod.appendChild(
                    paymentMethodType
            );

            transaction.appendChild(
                    paymentMethod
            );
        }

        // =========================================
        // Physical premises
        // =========================================

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

        // =========================================
        // PayerMS
        // =========================================

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

        transaction.appendChild(
                payerMS
        );

        return transaction;
    }

    private static Element findChild(
            Element parent,
            String localName) {

        for (Node node
                : new IterableNodeList(
                        parent.getChildNodes())) {

            if (node instanceof Element element) {

                if (localName.equals(
                        element.getLocalName())) {

                    return element;
                }
            }
        }

        return null;
    }

    private static class IterableNodeList
            implements Iterable<Node> {

        private final org.w3c.dom.NodeList nodeList;

        IterableNodeList(
                org.w3c.dom.NodeList nodeList) {

            this.nodeList = nodeList;
        }

        @Override
        public java.util.Iterator<Node> iterator() {

            return new java.util.Iterator<Node>() {

                private int index = 0;

                @Override
                public boolean hasNext() {
                    return index < nodeList.getLength();
                }

                @Override
                public Node next() {
                    return nodeList.item(index++);
                }
            };
        }
    }
}