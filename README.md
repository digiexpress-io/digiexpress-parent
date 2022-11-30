# Digiexpress

## Local development

### Prerequisites

- Java 17.0.4.1
- Maven 3.8.1+
- Both can be installed using [SDKMAN](https://sdkman.io/install)

```
  sdk install java 11.0.7-zulu
  sdk install maven
```

### Running the application

- Navigate to the project root directory
- Run `mvn clean install` to build the project
- Navigate to the Spring Boot application module `cd digiexpress-dev` `cd spring-app`
- Run `mvn spring-boot:run` to start the application backend
- Navigate to the React application module `cd digiexpress-composer` `cd digiexpress-composer-ui`
- Run `yarn install` to install the dependencies
- Run `yarn start` to start the application frontend
- Navigate to `http://localhost:3000` to view the application
