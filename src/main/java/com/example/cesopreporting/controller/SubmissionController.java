package com.example.cesopreporting.controller;

import java.io.File;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cesopreporting.validator.CesopXmlValidator;

@RestController
public class SubmissionController {

    @PostMapping("/submit")
    public String submitXml() {

        File xmlFile = new File("generated-cesop.xml");

        if (!xmlFile.exists()) {
            return "Submission failed: CESOP XML file not found.";
        }

        boolean valid = CesopXmlValidator.validate(
                xmlFile.getAbsolutePath()
        );

        if (!valid) {
            return "Submission failed: CESOP XML is invalid.";
        }

        return "CESOP XML submitted successfully!";
    }

    @GetMapping("/submission-status")
    public String submissionStatus() {
        return "CESOP XML submission status: READY";
    }
}