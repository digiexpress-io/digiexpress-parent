
# Digiexpress Setup Guide

## 1. Backend (Java)
To build and run the Java backend:

1. Build the project without running tests:
   ```bash
   mvn clean install -DskipTests
   ```

2. Navigate to the `eveli-app` directory and run the Spring Boot application:
   ```bash
   cd eveli-parent/eveli-app
   mvn clean spring-boot:run
   ```

## 2. Frontend (TypeScript)
There are two frontend projects:

### Eveli-IDE
- Mikki refers to it as "front office, front desk."
- Vahur refers to it as "task management."
- Worker interface.

### Gamut
- Mikki and Vahur call it "portal."
- Citizen interface.

To run either frontend:

1. Install dependencies:
   ```bash
   pnpm install
   ```

2. Start the development server:
   ```bash
   pnpm start
   ```

## 3. Database Setup
1. Navigate to the Docker setup directory:
   ```bash
   cd /Users/kaur/development/digiexpress-parent/mvn_setup/eveli-parent/eveli-local-docker
   ```

2. Start the Docker containers:
   ```bash
   docker compose up
   ```

## 4. Project Overview
- **Eveli-IDE**: Task management for workers.
- **Gamut**: Portal for citizens.
- **Backend**: Java-based service handling the core logic.
