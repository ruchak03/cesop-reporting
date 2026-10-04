package com.example.cesopreporting;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cesopreporting.generator.CesopXmlGenerator;
import com.example.cesopreporting.validator.CesopXmlValidator;
import com.example.cesopreporting.validator.DipXmlValidator;
import com.example.cesopreporting.submission.DipSubmissionGenerator;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, CESOP!";
    }

    @GetMapping("/validate")
    public String validateXml() {

        boolean valid = CesopXmlValidator.validate(
                "generated-cesop.xml"
        );

        if (valid) {
            return "CESOP XML is valid!";
        } else {
            return "CESOP XML is invalid!";
        }
    }

    @GetMapping("/generate")
    public String generateXml() {

        try {

            String filePath =
                    CesopXmlGenerator.generateXml();

            return "CESOP XML generated successfully!<br>"
                    + "File: " + filePath;

        } catch (Exception e) {

            return "XML generation failed: "
                    + e.getMessage();
        }
    }

    @GetMapping("/submission/generate")
    public String generateSubmission() {

        try {

            String filePath =
                    DipSubmissionGenerator.generateDipXml();

            return "DIP submission XML generated successfully!<br>"
                    + "File: " + filePath;

        } catch (Exception e) {

            return "DIP submission XML generation failed: "
                    + e;
        }
    }

    @GetMapping("/submission/validate")
public String validateSubmission() {

    boolean valid = DipXmlValidator.validate(
            "generated-dip-submission.xml"
    );

    if (valid) {
        return "DIP submission XML is valid!";
    } else {
        return "DIP submission XML is invalid!";
    }
}
}