![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License)
[![Backend](https://img.shields.io/maven-central/v/io.digiexpress/digiexpress-parent.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.digiexpress/digiexpress-parent)
[![Gamut](https://img.shields.io/npm/v/@dxs-ts/gamut?label=Gamut@latest)](https://www.npmjs.com/package/@dxs-ts/gamut)
[![Eveli IDE](https://img.shields.io/npm/v/@dxs-ts/eveli-ide?label=Eveli%20IDE@latest)](https://www.npmjs.com/package/@dxs-ts/eveli-ide)


# DigiExpress-parent

## Overview

DigiExpress is an all-in-one solution that covers a full range of organizational management requirements:
1. Online data collection via customizable forms (Dialob)
2. Business process automation to streamline workflows (theWrench)
3. Content management: Creating end-user content and linking it with forms and services (the Stencil)
4. User portal for providing content and forms to users (theStencil)
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

___

### Project Setup Guide

This project consists of three main applications: a **Java backend** and two **TypeScript frontends**. Below are the details on how to set up and run each component in the correct order.

---

> **⚠️ Note for Windows users:**  
> Docker setup may not work directly in native Windows environments. However, it works inside **Windows Subsystem for Linux (WSL)**. If you encounter issues starting the database containers, try running the setup inside WSL.


#### 1. Database Setup

Before starting the backend, make sure the database is up and running.

1. **Navigate to the Docker setup directory**:
   ```bash
   cd mvn_setup/eveli-parent/eveli-local-docker
   ```

2. **Start the Docker containers**:
   ```bash
   docker compose up
   ```


#### 2. Backend (Java)

The **Java Backend** is the core of the project and handles all business logic and data processing.

To build and run the backend:

1. **Build the project without running tests**:
   ```bash
   cd mvn_setup/
   mvn clean install -DskipTests
   ```

2. **Navigate to the `eveli-app` directory and start the Spring Boot application**:
   ```bash
   cd eveli-parent/eveli-app
   mvn spring-boot:run
   ```


#### 3. Frontend Applications (TypeScript)

There are two **TypeScript frontend** applications:

##### Frontdesk (Eveli-IDE)
- This is the **worker interface** for managing tasks. It’s designed for officials to review, respond to, and make decisions on citizen-submitted applications via the **Portal**.

##### Portal (Gamut)
- This is the **citizen interface** for submitting applications, requests, or tasks. Citizens use Gamut to initiate processes that are later handled in **Frontdesk**.

To run either of the frontend applications:

- Node version used for installing and testing this was `v22.5.1`.

1. **Navigate to the TypeScript setup folder**:
   ```bash
   cd ts_setup
   ```

2. **Install the required dependencies**:
   ```bash
   pnpm install
   ```

3. **Start the development server**:
   ```bash
   pnpm run start-gamut     # Portal (Gamut)
   pnpm run start-eveli     # Frontdesk (Eveli-IDE)  
   ```


#### 4. Project Overview

- **Eveli-IDE**: Frontdesk interface for workers. 
- **Gamut**: Portal interface for citizens.
- **Backend**: Java-based service handling core business logic and operations.
