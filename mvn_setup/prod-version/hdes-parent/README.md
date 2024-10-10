![Apache License, Version 2.0, January 2004](https://img.shields.io/github/license/apache/maven.svg?label=License&style=for-the-badge)
[![Maven Central](https://img.shields.io/maven-central/v/io.resys.hdes/hdes-bom.svg?label=Maven%20Central&style=for-the-badge)](https://search.maven.org/artifact/io.resys.hdes/hdes-bom)
![Build Status](https://img.shields.io/github/actions/workflow/status/the-wrench-io/hdes-parent/3.y-pre-commit.yaml?style=for-the-badge)

# HDES
Language for defining and connecting flows, decision tables and services

## Building the project locally  
After cloning the repository, open a terminal in the project root and run
```
mvn clean install
```
If you are running a Windows machine, carriage returns that do not appear in UNIX-based systems may cause certain tests to fail, so instead run 
``` 
mvn clean install -DskipTests=true
```


## Running the project as a Spring Boot application
1. Open a terminal and navigate into the [spring-app](https://github.com/the-wrench-io/hdes-parent/tree/3.y/hdes-dev/spring-app) folder
``` 
cd hdes-dev
cd spring-app
```
2. Configure a datasource in the [application.yml](https://github.com/the-wrench-io/hdes-parent/blob/3.y/hdes-dev/spring-app/src/main/resources/application.yml) file by setting ***ONLY ONE*** of the `enabled` fields to `true`:
```
    inmemory: 
      enabled: false
    git:
      enabled: false
      privateKey: "path-to-git-private-key-related-files: .known_hosts; id_rsa; id_rsa.known_hosts"
      repositoryUrl: "ssh-git-url" 
      branchSpecifier: "main"
      repositoryPath: "~/clone-git-repo-to"
      path: src/main/resources
    pg:
      enabled: false
      autoCreate: true
      repositoryName: "test-repo-1" 
      branchSpecifier: "main"
      pgHost: "localhost"
      pgDb: "db-name"
      pgUser: "db-user"
      pgPass: "db-user-pass"
```
- `inmemory` - read-only version
- `git` - use a repository as a database
- `pg` - use a PostgreSQL database - configuration guidelines available [here](https://github.com/the-wrench-io/hdes-parent/blob/3.y/hdes-dev/README_PG.MD)

3. Run the Spring Boot application
```
mvn spring-boot:run
```
4. Navigate to <http://localhost:8081>
