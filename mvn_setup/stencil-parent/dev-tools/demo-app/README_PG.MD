# build
mvn package -Pnative
docker build -f src/main/docker/Dockerfile.native -t demo-app:1 .

# create db via shell
create database my_stencil encoding 'UTF8' lc_collate='fi_FI.UTF8' lc_ctype='fi_FI.UTF8' template template0;
revoke connect on database my_stencil from public;
create user my_stencil;
grant all privileges on database "my_stencil" to my_stencil;
\password my_stencil
password: my_stencil_password


## Extensions

* quarkus-stencil-sc: Static content: Release can be compiled. One service that enables a "GET" and returns "Site".
* quarkus-stencil-composer: Example of UI Integration. 
* quarkus-stencil-composer-pg: Provides backend.

## Extension configurations

```
quarkus:

  stencil-composer:
    server-path: /q/ide-services

  stencil-composer-pg:
    service-path: /q/ide-services
    repo:
      repo-name: test-assets

  datasource:
    db-kind: postgresql 
    username: my_stencil
    password: joo4XooHllK2thei
    reactive:
      max-size: 3
      url: postgresql://localhost:5432/my_stencil
    
```


# run locally
docker run -i --rm -p 8080:8080 demo-app:1
## run using maven
mvn compile quarkus:dev

## pg - start postgre db if it's not running
docker-compose -f dev-tools/demo-app/src/main/resources/docker/stack-pg.yml up -d
http://localhost:8091/


## default postgre port
5432

## Drop table 

 `localhost:8091`  
 
 Drop order:
 
 1. nested_10_refs
 2. nested_10_tags
 3. nested_10_commits
 4. nested_10_treeitems
 5. nested_10_trees
 6. nested_10_blobs
 7. repos


