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


