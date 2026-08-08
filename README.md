# Enterprise Electric Vehicle (EV) Warranty System

[![Java 21](https://img.shields.io/badge/Java-21_LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=for-the-badge)](LICENSE)
[![OpenAPI 3.0](https://img.shields.io/badge/OpenAPI-3.0-85EA2D?style=for-the-badge&logo=openapi-initiative&logoColor=black)](http://localhost:8080/swagger-ui.html)

A enterprise-grade EV Warranty Management & Telemetry Analytics platform designed to connect Original Equipment Manufacturers (OEMs) with authorized Service Centers (SCs). Built on modern Java 21 LTS and Spring Boot 3.2, the system streamlines warranty claim lifecycles, supply chain inventory, safety recalls, and AI-driven predictive maintenance.

---

## 📋 Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Key Features](#2-key-features)
3. [Real-World Business Workflows](#3-real-world-business-workflows)
   - [Workflow 1: Warranty Claim Lifecycle](#workflow-1-warranty-claim-lifecycle)
   - [Workflow 2: AI Predictive Maintenance & Risk Scoring](#workflow-2-ai-predictive-maintenance--risk-scoring)
   - [Workflow 3: Recall Campaign Broadcast & Execution](#workflow-3-recall-campaign-broadcast--execution)
4. [System Architecture & Package Layout](#4-system-architecture--package-layout)
5. [Technology Stack](#5-technology-stack)
6. [Getting Started & Installation](#6-getting-started--installation)
   - [Option A: Docker Compose (Recommended)](#option-a-docker-compose-recommended)
   - [Option B: Manual Local Development](#option-b-manual-local-development)
7. [Test Accounts & Access Matrix](#7-test-accounts--access-matrix)
8. [API Documentation & Swagger UI](#8-api-documentation--swagger-ui)
9. [License](#9-license)

---

## 1. Executive Summary

As the Electric Vehicle ecosystem rapidly expands, managing battery degradation, high-voltage powertrain warranties, component failures, and safety recalls requires centralized data exchange between automakers and repair centers.

**EV Warranty System** provides:
* **OEM Oversight**: Centralized approval workflow for warranty reimbursements, part catalogs, and safety recall broadcasts.
* **Service Center Operational Efficiency**: Seamless claim filing, vehicle history tracking, customer appointments, and inventory allocation.
* **AI Predictive Risk Engine**: Statistical analysis of fleet failure trends, battery health parameters, and mileage thresholds to forecast component breakdowns before catastrophic failure occurs.

---

## 2. Key Features

- 🔐 **Role-Based Access Control (RBAC)**: Fine-grained security for `ADMIN`, `EVM_STAFF`, `SC_STAFF`, and `SC_TECHNICIAN`.
- ⚡ **AI Failure Prediction & Telemetry Risk Scoring**: Heuristic and statistical risk modeling based on mileage, vehicle model history, and component repeat failure rates.
- 📋 **End-to-End Warranty Claim Processing**: Multi-stage state machine (`SUBMITTED` ➔ `UNDER_REVIEW` ➔ `APPROVED` / `REJECTED` ➔ `IN_PROGRESS` ➔ `COMPLETED`).
- 🏬 **Parts & Supply Chain Inventory**: Real-time stock tracking across authorized service centers with low-stock alerts.
- 📢 **Safety Recalls & Service Campaigns**: Target affected VIN ranges and vehicle models with automated repair scheduling.
- 📊 **Real-time Analytics Dashboard**: Role-tailored dashboards powered by interactive charts and OpenAPI endpoints.

---

## 3. Real-World Business Workflows

### Workflow 1: Warranty Claim Lifecycle

The following sequence illustrates how a warranty claim moves from customer intake at a Service Center through OEM review, technician assignment, and final repair completion:

```mermaid
sequenceDiagram
    autonumber
    actor Customer
    actor SCStaff as SC Staff
    actor EVMStaff as OEM (EVM) Staff
    actor SCTech as SC Technician
    participant System as EV Warranty Platform

    Customer->>SCStaff: Brings vehicle with battery/BMS issue
    SCStaff->>System: Searches VIN & verifies Warranty Eligibility
    SCStaff->>System: Submits Warranty Claim (Status: DRAFT / SUBMITTED)
    System-->>EVMStaff: Notifies OEM of pending claim
    
    alt OEM Review Phase
        EVMStaff->>System: Evaluates Diagnosis Notes & Total Cost
        alt Approved Claim
            EVMStaff->>System: Approve Claim (Status: APPROVED)
        else Rejected Claim
            EVMStaff->>System: Reject Claim (Status: REJECTED)
        end
    end

    opt Repair & Execution (If Approved)
        SCStaff->>System: Assign Technician to Claim
        SCTech->>System: Begins Repair & Replaces Component (Status: IN_PROGRESS)
        SCTech->>System: Marks Repair Complete (Status: COMPLETED)
        System->>System: Deducts Part Inventory & Updates Service History
    end
```

---

### Workflow 2: AI Predictive Maintenance & Risk Scoring

The AI Engine analyzes fleet telemetry and past claim records to detect abnormal component failure trends and generate actionable recommendations:

```mermaid
flowchart TD
    A[Vehicle Intake / Service Arrival] --> B{Fetch Fleet Claims & Telemetry Data}
    B --> C[Aggregate Failure Rates by Vehicle Model]
    B --> D[Compute Average Failure Mileage per Part]

    C & D --> E[AI Anomaly & Risk Multiplier Engine]
    
    E --> F{Mileage > 80% Fleet Failure Threshold?}
    F -- Yes --> G[Apply Predictive Risk Multiplier +40%]
    F -- No --> H[Standard Failure Probability]
    
    E --> I{Repeat Failure Detected on VIN?}
    I -- Yes --> J[Apply Repeat Anomaly Multiplier +100%]
    I -- No --> H

    G & J & H --> K[Calculate Final Probability & Severity Score]
    
    K --> L{Risk Level Classification}
    L -- > 60% Risk --> M[CRITICAL: Immediate Diagnostic Required]
    L -- 35% - 60% Risk --> N[HIGH: Inspect at Next Maintenance Interval]
    L -- 15% - 35% Risk --> O[MEDIUM: Monitor Component Efficiency]
    L -- < 15% Risk --> P[LOW: Routine Monitoring]
```

---

### Workflow 3: Recall Campaign Broadcast & Execution

Automakers broadcast safety recall campaigns for specific vehicle models or VIN lists, enabling automated service appointments at local service centers:

```mermaid
flowchart LR
    A[OEM Manufacturer] -->|Create Service Campaign| B(Define Affected Models & VIN Ranges)
    B -->|Publish Campaign| C[System Broadcasts Active Recall]
    
    C --> D{Service Center Vehicle Search}
    D -->|VIN Matched| E[Flag Recall Eligibility]
    E --> F[Book Customer Appointment]
    F --> G[Perform Recall Remedy & Complete Claim]
    G --> H[Update Campaign Completion Counter]
```

---

## 4. System Architecture & Package Layout

The application follows a **Domain-Driven / Feature-Based Modular Architecture** under `com.oem.evwarranty.*`:

```text
com.oem.evwarranty/
├── EvWarrantyApplication.java
│
├── common/                               # Cross-cutting concerns & shared infrastructure
│   ├── config/                           # SecurityConfig, WebConfig, OpenApiConfig, GlobalModelAdvice
│   └── exception/                        # ResourceNotFoundException, BusinessLogicException, GlobalExceptionHandler
│
└── domain/                               # Self-contained business domains
    ├── analytics/                        # AI Failure Prediction, Reports & Dashboards
    ├── audit/                            # System Audit Logging
    ├── campaign/                         # Service Campaigns & Safety Recalls
    ├── claim/                            # Warranty Claims, Policies & Appointments
    ├── customer/                         # Vehicle Owners & Customers
    ├── inventory/                        # Parts Catalog, Stock Management & Supply Chain
    ├── user/                             # Authentication, Users & Role-Based Access Control
    └── vehicle/                          # Electric Vehicles & Component Telemetry
```

---

## 5. Technology Stack

* **Core Language**: Java 21 (LTS)
* **Backend Framework**: Spring Boot 3.2.0 (Spring MVC, Spring Data JPA, Spring Security 6)
* **Frontend Template Engine**: Thymeleaf + Bootstrap 5 + jQuery
* **Database**: PostgreSQL 17
* **API Specs**: OpenAPI 3.0 (Springdoc OpenAPI / Swagger UI)
* **Build & Containerization**: Apache Maven & Docker Compose

---

## 6. Getting Started & Installation

### Prerequisites

* JDK 21 or higher
* Docker & Docker Compose (Optional, for containerized run)
* Git

---

### Option A: Docker Compose (Recommended)

To launch the full stack (Spring Boot Application + PostgreSQL Database) in detached mode:

1. Clone the repository:
   ```bash
   git clone https://github.com/KaitoDeus/EV-Warranty-System.git
   cd EV-Warranty-System
   ```

2. Start services via Docker Compose:
   ```bash
   docker-compose up -d --build
   ```

3. Access the application at `http://localhost:8080`.

---

### Option B: Manual Local Development

1. Start PostgreSQL database container only:
   ```bash
   docker-compose up -d evwarranty-db
   ```

2. Run the Spring Boot application using Maven:
   ```bash
   mvn spring-boot:run
   ```

3. Access the web interface at `http://localhost:8080`.

---

## 7. Test Accounts & Access Matrix

You can sign in with the following pre-configured credentials to evaluate role-based permissions:

| Role | Username | Password | Access Scope & Operations |
| :--- | :--- | :--- | :--- |
| **System Administrator** | `admin` | `password123` | Full system administration, user management, global dashboards & audit logs |
| **Service Center Staff** | `scstaff` | `password123` | Customer management, vehicle intake, warranty claim creation & appointment booking |
| **Service Center Tech** | `sctech` | `password123` | AI risk prediction analysis, diagnostic execution & repair status updates |
| **OEM Manufacturer Staff**| `evmstaff` | `password123` | Claim approvals/rejections, part catalog, inventory allocation & recall campaigns |

---

## 8. API Documentation & Swagger UI

The system exposes RESTful API endpoints documented via OpenAPI 3.0 specs.

When the application is running, open your browser to access the interactive Swagger UI:
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

Raw OpenAPI JSON definition:
👉 **[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)**

---

## 9. License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

```text
Copyright (c) 2026 EV Warranty System Contributors
```
