# CESOP Reporting System

A Spring Boot based CESOP reporting solution for generating, validating, and preparing CESOP payment reports according to the official German BZSt CESOP and DIP schemas.

## Project Overview

This project demonstrates the complete CESOP reporting flow:

- Payment data collection
- Payment verification
- CESOP XML generation
- CESOP XML validation
- DIP submission XML generation
- DIP submission validation
- Correction XML generation
- Correction XML validation
- DIP resubmission
- DIP resubmission validation

The project uses the official BZSt CESOP and DIP XSD schemas as the reference for XML structure and validation.

## Technology Stack

- Java
- Spring Boot
- Maven
- JAXB
- XML / XSD
- REST APIs
- Embedded Tomcat
- Git / GitHub

## CESOP Version

- CESOP XML Schema: 4.03
- DIP Version: 2

## Project Flow

```text
Payment Collection
        ↓
Payment Verification
        ↓
CESOP XML Generation
        ↓
CESOP XML Validation
        ↓
DIP Submission Generation
        ↓
DIP Submission Validation
        ↓
Correction XML Generation
        ↓
Correction XML Validation
        ↓
DIP Resubmission
        ↓
DIP Resubmission Validation