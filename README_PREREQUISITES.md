# Prerequisites

## Contents
* List of required software
* Installation guides 

//TODO


--------

## List of required software

* **Java**: version 17.0.9-zulu
* **pnpm** for package management: https://pnpm.io/
* **Corepack** to manage package manager versions: https://nodejs.org/api/corepack.html
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
java=17.0.9-zulu  
quarkus=3.2.7.Final
```

You'll know it's working if the terminal informs of the following when you navigate into the `/mvn_setup` directory.

```bash
Using java version 17.0.9-zulu in this shell.
Using quarkus version 3.2.7.Final in this shell.
```

//TODO  

2. Corepack

3. Configure db

4. Congigure Adminer

