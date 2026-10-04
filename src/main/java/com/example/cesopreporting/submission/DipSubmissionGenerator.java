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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NamedNodeMap;

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

            // ---------------------------------------------------------
            // 1. Create DIP
            // ---------------------------------------------------------

            DipType dip = new DipType();

            dip.setVersion("2.0");


            // ---------------------------------------------------------
            // 2. Header
            // ---------------------------------------------------------

            DipHeaderType header = new DipHeaderType();

            header.setEnvironment(
                    EnvironmentType.TEST
            );


            // ---------------------------------------------------------
            // 3. Consignment
            // ---------------------------------------------------------

            ConsignmentType consignment =
                    new ConsignmentType();

            CustomerIdentifierType customerIdentifier =
                    new CustomerIdentifierType();

            customerIdentifier.setIdentityProvider(
                    IdentityProviderType.ELSTER
            );

            customerIdentifier.setIdentifier(
                    "LOCALTEST"
            );

            consignment.setCustomerIdentifier(
                    customerIdentifier
            );


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


            consignment.setTransferticketId(
                    "CESOP-" + UUID.randomUUID()
            );

            header.setConsignment(
                    consignment
            );


            // ---------------------------------------------------------
            // 4. Application
            // ---------------------------------------------------------

            ApplicationType application =
                    new ApplicationType();

            application.setCode("CESOP");

            header.setApplication(
                    application
            );

            dip.setHeader(header);


            // ---------------------------------------------------------
            // 5. DIP Body
            // ---------------------------------------------------------

            DipBodyType body =
                    new DipBodyType();

            ConsignmentItemType item =
                    new ConsignmentItemType();

            item.setConsignmentItemPosition(
                    BigInteger.ZERO
            );


            // ---------------------------------------------------------
            // 6. Read generated CESOP XML
            // ---------------------------------------------------------

            File cesopFile =
                    new File("generated-cesop.xml");

            if (!cesopFile.exists()) {

                throw new RuntimeException(
                        "generated-cesop.xml does not exist. "
                        + "Run /generate first."
                );
            }


            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(true);

            Document document =
                    factory
                            .newDocumentBuilder()
                            .parse(cesopFile);


            Element cesopElement =
                    document.getDocumentElement();


            // ---------------------------------------------------------
            // 7. Put CESOP XML into DIP data
            // ---------------------------------------------------------

            item.setData(
                    cesopElement
            );


            body.getConsignmentItem().add(
                    item
            );

            dip.setBody(body);


            // ---------------------------------------------------------
            // 8. Create JAXB context
            // ---------------------------------------------------------

            JAXBContext dipContext =
                    JAXBContext.newInstance(
                            DipType.class,
                            ObjectFactory.class
                    );


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


            // ---------------------------------------------------------
            // 9. Create DIP root
            // ---------------------------------------------------------

            ObjectFactory objectFactory =
                    new ObjectFactory();

            jakarta.xml.bind.JAXBElement<DipType> dipElement =
                    objectFactory.createDip(dip);


            // ---------------------------------------------------------
            // 10. Generate DIP XML
            // ---------------------------------------------------------

            File outputFile =
                    new File(
                            "generated-dip-submission.xml"
                    );

            marshaller.marshal(
                    dipElement,
                    outputFile
            );


            // ---------------------------------------------------------
            // 11. Clean duplicate namespace declarations
            // ---------------------------------------------------------

            removeDuplicateNamespaces(
                    outputFile
            );


            return outputFile.getAbsolutePath();


        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "DIP submission XML generation failed",
                    e
            );
        }
    }


    // -------------------------------------------------------------
    // Remove duplicate xmlns:ns2 declarations
    // -------------------------------------------------------------

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
                firstIndex
                        + namespacePrefix.length();


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
                            nextIndex
                                    + namespacePrefix.length()
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