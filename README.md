
# Getting started

## Build

```
mvn install
``` 

## Create database

Ensure that postgres is running. Alternatively you can start postgres container:

```
docker run -d --name digiexpress-postgres -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres
```

Initialize database:

```
psql -p 5432 -U postgres -h localhost
create database digiexpress encoding 'UTF8' lc_collate='en_US.UTF8' lc_ctype='en_US.UTF8' template template0;
revoke connect on database digiexpress from public;
create user digiexpress;
grant all privileges on database "digiexpress" to digiexpress;
\password digiexpress
```
Use password `example`.


## Start backend

``` 
cd extensions/app-spring
mvn spring-boot:run
```

## Start ui

```
cd composer-ui
yarn
yarn start
```
