package com.example.cesopreporting.validator;

import java.io.File;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class DipResubmissionXmlValidator {

    public static boolean validate(String xmlFilePath) {

        try {

            File xsdFile =
                    new File(
                            "src/main/resources/xsd/dip.xsd"
                    );

            SchemaFactory factory =
                    SchemaFactory.newInstance(
                            XMLConstants.W3C_XML_SCHEMA_NS_URI
                    );

            Schema schema =
                    factory.newSchema(xsdFile);

            schema.newValidator().validate(
                    new javax.xml.transform.stream.StreamSource(
                            new File(xmlFilePath)
                    )
            );

            return true;

        } catch (Exception e) {

            System.out.println(
                    "DIP resubmission XML validation failed:"
            );

            e.printStackTrace();

            return false;
        }
    }
}