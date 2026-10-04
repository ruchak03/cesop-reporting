package com.example.cesopreporting;
import com.example.cesopreporting.validator.CorrectionXmlValidator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.cesopreporting.generator.CesopXmlGenerator;
import com.example.cesopreporting.validator.CesopXmlValidator;
import com.example.cesopreporting.submission.DipSubmissionGenerator;
import com.example.cesopreporting.validator.DipXmlValidator;
import com.example.cesopreporting.correction.CorrectionXmlGenerator;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello, CESOP!";
    }

    // =========================================================
    // CESOP XML VALIDATION
    // =========================================================

    @GetMapping("/validate")
    public String validateXml() {

        boolean valid =
                CesopXmlValidator.validate(
                        "generated-cesop.xml"
                );

        if (valid) {
            return "CESOP XML is valid!";
        } else {
            return "CESOP XML is invalid!";
        }
    }

    // =========================================================
    // CESOP XML GENERATION
    // =========================================================

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

    // =========================================================
    // DIP SUBMISSION GENERATION
    // =========================================================

    @GetMapping("/submission/generate")
    public String generateSubmission() {

        try {

            String filePath =
                    DipSubmissionGenerator
                            .generateDipXml();

            return "DIP submission XML generated successfully!<br>"
                    + "File: " + filePath;

        } catch (Exception e) {

            return "DIP submission XML generation failed: "
                    + e.getMessage();
        }
    }

    // =========================================================
    // DIP SUBMISSION VALIDATION
    // =========================================================

    @GetMapping("/submission/validate")
    public String validateSubmission() {

        boolean valid =
                DipXmlValidator.validate(
                        "generated-dip-submission.xml"
                );

        if (valid) {

            return "DIP submission XML is valid!";

        } else {

            return "DIP submission XML is invalid!";
        }
    }

    // =========================================================
    // CORRECTION XML GENERATION
    // =========================================================

    @GetMapping("/correction/generate")
    public String generateCorrection(
            @RequestParam String paymentId) {

        try {

            String filePath =
                    CorrectionXmlGenerator
                            .generateCorrectionXml(
                                    paymentId
                            );

            return "Correction XML generated successfully!<br>"
                    + "File: " + filePath;

        } catch (Exception e) {

            return "Correction XML generation failed: "
                    + e.getMessage();
        }
    }

    @GetMapping("/correction/validate")
public String validateCorrection() {

    boolean valid =
            CorrectionXmlValidator.validate(
                    "generated-correction.xml"
            );

    if (valid) {

        return "Correction XML is valid!";

    } else {

        return "Correction XML is invalid!";
    }
}
}