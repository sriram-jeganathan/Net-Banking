# Net Banking System

A **console-based Net Banking System** developed in **Java** as part of the **3rd Semester Project-Based Learning (PBL)**. The project aims to simulate the basic workflow of an online banking application using a modular and object-oriented design.

> **Current Status:** UI Prototype (First Review)

---

## Project Objective

The objective of this project is to design a simple banking application that allows users to navigate through different banking modules using a console-based interface. In the current phase, the project focuses on application structure, page navigation, and user interface design.

---

## Current Features

- Console-based user interface
- Login page
- Dashboard
- Account Summary page
- Transfer Money page
- Transaction History page
- Login History page
- Bank Statement page
- Navigation between all pages
- Modular project structure using packages

> **Note:** The current implementation contains placeholder pages for demonstration purposes. Business logic and database functionality will be added in future reviews.

---

## Technologies Used

| Technology | Purpose |
|------------|---------|
| Java 21 | Programming Language |
| Maven | Build & Dependency Management |
| MariaDB | Database (Future Integration) |
| JDBC | Database Connectivity (Future Integration) |
| Git | Version Control |

---

## Project Structure

```
NetBanking/
│
├── pom.xml
│
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── sri/
│                   └── netbanking/
│                       ├── ui/
│                       │   ├── LoginPage.java
│                       │   ├── DashboardPage.java
│                       │   ├── AccountSummaryPage.java
│                       │   ├── TransferPage.java
│                       │   ├── TransactionHistoryPage.java
│                       │   ├── LoginHistoryPage.java
│                       │   └── StatementPage.java
│                       │
│                       ├── utils/
│                       │   └── ConsoleUtil.java
│                       │
│                       └── Main.java
│
└── README.md
```

---

## Application Flow

```
Login
   │
   ▼
Dashboard
   ├── Account Summary
   ├── Transfer Money
   ├── Transaction History
   ├── Login History
   ├── Bank Statement
   └── Logout
         │
         ▼
       Login
```

---

## Future Development

The following features will be implemented in the next development phases:

- User Authentication
- Database Connectivity using JDBC
- Account Management
- Money Transfer
- Transaction Storage
- Login History Storage
- PDF Bank Statement Generation
- Password Hashing
- Exception Handling
- Input Validation

---

## Build Instructions

### Compile

```bash
mvn compile
```

### Run

```bash
mvn exec:java
```

---

## Learning Outcomes

This project demonstrates the use of:

- Object-Oriented Programming (OOP)
- Java Packages
- Modular Programming
- Console-Based User Interface Design
- Maven Project Management
- Software Project Structure

---

## Disclaimer

This project is developed solely for academic purposes as part of a Project-Based Learning course. It is a simulation of a banking system and is not intended for real-world banking operations.

---

## Developers

| Name | Class | Roll Number |
|------|-------|-------------|
| **Sriram Jeganathan** | CSE L | 2104251040966 |
| **A Vimanthan** | CSE L | 2104251040013 |

---
**Status:** First Review Prototype