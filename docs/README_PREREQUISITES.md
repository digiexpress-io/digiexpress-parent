# Prerequisites

## Contents
* List of required/recommended software
* Installation guides 

//TODO


--------

## List of required software

* **Java**: <JAVA_VERSION>
* **pnpm** for package management: https://pnpm.io/
* **Maven**
* **Node**
* **SDKMAN** SDK/JDK manager: https://sdkman.io/usage
* **PostgreSQL database** running in docker container 
* **Adminer**: https://www.adminer.org/

---

## Installation guides

1. SDKMAN 
https://sdkman.io/install

For convenience, you can create an .sdkmanrc file in the `/mvn_setup` directory. This will enable automatic switching to the specified Java version 
whenever you navigate to the `/mvn_setup`directory via your terminal.  

See guide here: https://sdkman.io/usage --> **Env Command** section

Your .sdkmanrc file should contain these values:

```
java=<JAVA_VERSION> 
quarkus=<QUARKUS_VERSION>
```

You'll know it's working if the terminal informs of the following when you navigate into the `/mvn_setup` directory.

```bash
Using java version <JAVA_VERSION> in this shell.
Using quarkus version <QUARKUS_VERSION> in this shell.
```

//TODO  

