package com.example.cesopreporting.submission;

import de.itzbund.ozg.bzst.post.dip.v2.ApplicationType;
import de.itzbund.ozg.bzst.post.dip.v2.ConsignmentItemType;
import de.itzbund.ozg.bzst.post.dip.v2.ConsignmentType;
import de.itzbund.ozg.bzst.post.dip.v2.CustomerIdentifierType;
import de.itzbund.ozg.bzst.post.dip.v2.DipBodyType;
import de.itzbund.ozg.bzst.post.dip.v2.DipHeaderType;
import de.itzbund.ozg.bzst.post.dip.v2.DipType;
import de.itzbund.ozg.bzst.post.dip.v2.EnvironmentType;
import de.itzbund.ozg.bzst.post.dip.v2.IdentityProviderType;
import de.itzbund.ozg.bzst.post.dip.v2.ObjectFactory;

import com.example.cesopreporting.service.DipReferenceStore;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

public class DipSubmissionGenerator {

    public static String generateDipXml() {

        try {

            // -------------------------------------------------
            // 1. Create DIP root
            // -------------------------------------------------

            DipType dip = new DipType();

            dip.setVersion("2.0");


            // -------------------------------------------------
            // 2. Create DIP Header
            // -------------------------------------------------

            DipHeaderType header = new DipHeaderType();

            // Local test environment
            header.setEnvironment(EnvironmentType.TEST);


            // -------------------------------------------------
            // 3. Create Consignment
            // -------------------------------------------------

            ConsignmentType consignment = new ConsignmentType();


            // -------------------------------------------------
            // 4. Customer Identifier
            // -------------------------------------------------

            CustomerIdentifierType customerIdentifier =
                    new CustomerIdentifierType();

            customerIdentifier.setIdentityProvider(
                    IdentityProviderType.ELSTER
            );

            // Local test identifier
            customerIdentifier.setIdentifier("LOCALTEST");

            consignment.setCustomerIdentifier(
                    customerIdentifier
            );


            // -------------------------------------------------
            // 5. Creation Time
            // -------------------------------------------------

            OffsetDateTime now =
                    OffsetDateTime.now(ZoneOffset.UTC);

            XMLGregorianCalendar creationTime =
                    DatatypeFactory
                            .newInstance()
                            .newXMLGregorianCalendar(
                                    now.toString()
                            );

            consignment.setCreationTime(
                    creationTime
            );


            // -------------------------------------------------
            // 6. Generate Transfer Ticket ID
            // -------------------------------------------------
            //
            // This identifies this DIP submission.
            //
            // Example:
            // CESOP-12345678-....
            //
            // This is stored so that a later resubmission
            // can reference this submission.
            // -------------------------------------------------

            String transferTicketId =
                    "CESOP-" + UUID.randomUUID();

            consignment.setTransferticketId(
                    transferTicketId
            );


            // Save the latest transfer ticket
            DipReferenceStore.saveTransferTicketId(
                    transferTicketId
            );


            // -------------------------------------------------
            // 7. Add Consignment to Header
            // -------------------------------------------------

            header.setConsignment(
                    consignment
            );


            // -------------------------------------------------
            // 8. Application
            // -------------------------------------------------

            ApplicationType application =
                    new ApplicationType();

            application.setCode("CESOP");

            header.setApplication(
                    application
            );


            // -------------------------------------------------
            // 9. Add Header to DIP
            // -------------------------------------------------

            dip.setHeader(
                    header
            );


            // -------------------------------------------------
            // 10. Create DIP Body
            // -------------------------------------------------

            DipBodyType body =
                    new DipBodyType();

            ConsignmentItemType item =
                    new ConsignmentItemType();

            item.setConsignmentItemPosition(
                    BigInteger.ZERO
            );


            // -------------------------------------------------
            // 11. Read generated CESOP XML
            // -------------------------------------------------

            File cesopFile =
                    new File("generated-cesop.xml");

            if (!cesopFile.exists()) {

                throw new RuntimeException(
                        "generated-cesop.xml does not exist. "
                        + "Run /generate first."
                );
            }


            // -------------------------------------------------
            // 12. Parse CESOP XML
            // -------------------------------------------------

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            Document document =
                    factory
                            .newDocumentBuilder()
                            .parse(cesopFile);

            Element cesopElement =
                    document.getDocumentElement();


            // -------------------------------------------------
            // 13. Put CESOP XML inside DIP consignment item
            // -------------------------------------------------

            item.setData(
                    cesopElement
            );

            body.getConsignmentItem().add(
                    item
            );

            dip.setBody(
                    body
            );


            // -------------------------------------------------
            // 14. Create JAXB Context
            // -------------------------------------------------

            JAXBContext dipContext =
                    JAXBContext.newInstance(
                            DipType.class,
                            ObjectFactory.class
                    );


            // -------------------------------------------------
            // 15. Create Marshaller
            // -------------------------------------------------

            Marshaller marshaller =
                    dipContext.createMarshaller();

            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT,
                    true
            );

            marshaller.setProperty(
                    Marshaller.JAXB_ENCODING,
                    "UTF-8"
            );


            // -------------------------------------------------
            // 16. Create DIP JAXBElement
            // -------------------------------------------------

            ObjectFactory objectFactory =
                    new ObjectFactory();

            jakarta.xml.bind.JAXBElement<DipType> dipElement =
                    objectFactory.createDip(dip);


            // -------------------------------------------------
            // 17. Save DIP XML
            // -------------------------------------------------

            File outputFile =
                    new File(
                            "generated-dip-submission.xml"
                    );

            marshaller.marshal(
                    dipElement,
                    outputFile
            );


            // -------------------------------------------------
            // 18. Remove duplicate namespace declarations
            // -------------------------------------------------

            removeDuplicateNamespaces(
                    outputFile
            );


            // -------------------------------------------------
            // 19. Return generated file path
            // -------------------------------------------------

            return outputFile.getAbsolutePath();


        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "DIP submission XML generation failed",
                    e
            );
        }
    }


    // =========================================================
    // Remove duplicate namespace declarations
    // =========================================================

    private static void removeDuplicateNamespaces(
            File file
    ) throws Exception {

        String xml =
                Files.readString(
                        file.toPath(),
                        StandardCharsets.UTF_8
                );

        String namespacePrefix =
                "xmlns:ns2=\"";

        int firstIndex =
                xml.indexOf(namespacePrefix);

        if (firstIndex == -1) {
            return;
        }

        int searchFrom =
                firstIndex + namespacePrefix.length();

        while (true) {

            int nextIndex =
                    xml.indexOf(
                            namespacePrefix,
                            searchFrom
                    );

            if (nextIndex == -1) {
                break;
            }

            int valueEnd =
                    xml.indexOf(
                            "\"",
                            nextIndex +
                                    namespacePrefix.length()
                    );

            if (valueEnd == -1) {
                break;
            }

            int attributeEnd =
                    valueEnd + 1;

            xml =
                    xml.substring(
                            0,
                            nextIndex
                    )
                    +
                    xml.substring(
                            attributeEnd
                    );

            searchFrom =
                    nextIndex;
        }

        Files.writeString(
                file.toPath(),
                xml,
                StandardCharsets.UTF_8
        );
    }
}