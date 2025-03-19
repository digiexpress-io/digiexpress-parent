
# Project Setup Guide

This project consists of three main applications: a **Java backend** and two **TypeScript frontends**. Below are the details on how to set up and run each component.

## 1. Backend (Java)
The **Java Backend** is the core of the project and handles all business logic and data processing.

To build and run the backend:

1. **Build the project without running tests**:
   ```bash
   mvn clean install -DskipTests
   ```

2. **Navigate to the `eveli-app` directory and start the Spring Boot application**:
   ```bash
   cd eveli-parent/eveli-app
   mvn clean spring-boot:run
   ```

## 2. Frontend Applications (TypeScript)
There are two **TypeScript frontend** applications:

### **Eveli-IDE**
- Mikki refers to it as "front office, front desk".
- Vahur refers to it as "task management".
- This is the **worker interface** for managing tasks. This is the interface designed for workers or officials who oversee and manage tasks submitted by citizens. They can review, respond to, and make decisions on applications or requests submitted through the Gamut portal. The worker interface facilitates the decision-making process, approvals, and replies to the citizen tasks.

### **Gamut**
- Mikki and Vahur call it the "portal".
- This is the **citizen interface**. This is the interface for citizens to submit requests, applications, or tasks. Citizens interact with the Gamut portal to initiate processes, make inquiries, or provide necessary information, which is then reviewed and processed by the workers using the Eveli-IDE interface.

To run any of these frontend applications:

1. **Install the required dependencies**:
   ```bash
   pnpm install
   ```

2. **Start the development server**:
   ```bash
   pnpm start
   ```

## 3. Database Setup
To set up the database using Docker, follow these steps:

1. **Navigate to the Docker setup directory**:
   ```bash
   cd /digiexpress-parent/mvn_setup/eveli-parent/eveli-local-docker
   ```

2. **Start the Docker containers**:
   ```bash
   docker compose up
   ```

## 4. Project Overview

- **Eveli-IDE**: Task management interface for workers.
- **Gamut**: Portal interface for citizens.
- **Backend**: Java-based service handling core business logic and operations.
