# Dev App using postgresql as FORM storage

# Datasource configuration

## Generate password
Random password: ``` openssl rand -base64 8 ```


## Create database via shell
```
create database assets_db encoding 'UTF8' lc_collate='fi_FI.UTF8' lc_ctype='fi_FI.UTF8' template template0;
revoke connect on database assets_db from public;
create user assets_db;
grant all privileges on database "assets_db" to assets_db;
\password assets_db
password: assets_db_password
```

### Quarkus datasource config
```
  datasource:
    db-kind: postgresql 
    username: assets_db
    password: csvFAbq3RPM=
    reactive:
      max-size: 3
      url: postgresql://localhost:5432/assets_db
```

### Spring datasource config
```
    pg:
      enabled: true
      autoCreate: true
      repositoryName: "test-repo-1" 
      branchSpecifier: "main"
      pgHost: "localhost"
      pgDb: "assets_db"
      pgUser: "assets_db"
      pgPass: "assets_db_password"
```

## Starting PG instance via docker
docker command: ``` docker-compose -f docker/stack-pg.yml up -d ```  
adminer url: ```http://localhost:8091/```  
default postgres port ```5432```


## Droping PG tables  
Drop order:
``` 
 1. nested_10_refs
 2. nested_10_tags
 3. nested_10_commits
 4. nested_10_treeitems
 5. nested_10_trees
 6. nested_10_blobs
 7. repos
```