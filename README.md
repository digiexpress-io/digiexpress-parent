![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)
[![Backend](https://img.shields.io/maven-central/v/io.digiexpress/digiexpress-parent.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.digiexpress/digiexpress-parent)
[![Gamut](https://img.shields.io/npm/v/@dxs-ts/gamut?label=Gamut@latest)](https://www.npmjs.com/package/@dxs-ts/gamut)
[![Eveli IDE](https://img.shields.io/npm/v/@dxs-ts/eveli-ide?label=Eveli%20IDE@latest)](https://www.npmjs.com/package/@dxs-ts/eveli-ide)


# DigiExpress-parent

## Overview

DigiExpress is an all-in-one solution that covers a full range of organizational management requirements:
1. Online data collection via customizable forms (Dialob)
2. Business process automation to streamline workflows (the Wrench)
3. Content management: Creating end-user content and linking it with forms and services (the Stencil)
4. User portal for providing content and forms to users (the Stencil)
5. Worker / employee front office portal for handing tasks, communicating with customers, etc. 
6. Task management system
7. [Access / user-rights management](/docs/README_ACCESS_MGMT.md)
8. Audit trail for tracking user access to resources, tasks, etc. 

Data is managed via Thena: a JSON storage framework with GIT-like features on top of a relational database.

### Project structure: High level

* `mvn_setup`: Backend projects built with Maven and released to Maven central repository 
https://central.sonatype.com/artifact/io.digiexpress/digiexpress-parent

* `ts_setup`: Frontend components and UI service layer
* `bazel_setup`: Backend projects built with Bazel (under development)

## Documentation 

### Contributing

1. [Contribution guidelines](/docs/README_CONTRIBUTION_GUIDELINES.md): Branch organisation, creating feature branches, creating issues, making pull requests
2. [How to report bugs](/docs/README_BUG_REPORT.md)


### Licensing 
DigiExpress is [Apache 2.0](/LICENSE) licensed.



### Project Setup Guide

This project consists of three main applications: a **Java backend** and two **TypeScript frontends**. Below are the details on how to set up and run each component.

#### 1. Backend (Java)
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

#### 2. Frontend Applications (TypeScript)
There are two **TypeScript frontend** applications:

##### **Eveli-IDE**
- Mikki refers to it as "front office, front desk".
- Vahur refers to it as "task management".
- This is the **worker interface** for managing tasks. This is the interface designed for workers or officials who oversee and manage tasks submitted by citizens. They can review, respond to, and make decisions on applications or requests submitted through the Gamut portal. The worker interface facilitates the decision-making process, approvals, and replies to the citizen tasks.

##### **Gamut**
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

#### 3. Database Setup
To set up the database using Docker, follow these steps:

1. **Navigate to the Docker setup directory**:
   ```bash
   cd /digiexpress-parent/mvn_setup/eveli-parent/eveli-local-docker
   ```

2. **Start the Docker containers**:
   ```bash
   docker compose up
   ```

#### 4. Project Overview

- **Eveli-IDE**: Task management interface for workers.
- **Gamut**: Portal interface for citizens.
- **Backend**: Java-based service handling core business logic and operations.

