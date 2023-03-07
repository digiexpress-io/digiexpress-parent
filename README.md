
# Getting started for local development

## Build application

```
mvn install
``` 

## Create database

Ensure that postgres is running. Alternatively you can use postgres docker container:

```
docker run -d --name digiexpress-postgres -p 5432:5432 -e POSTGRES_PASSWORD=postgres postgres
```

Initialize database, for this connect to database:

```
psql -p 5432 -U postgres -h localhost
```

In psql execute following commands to setup database:

```
create database digiexpress encoding 'UTF8' lc_collate='en_US.UTF8' lc_ctype='en_US.UTF8' template template0;
revoke connect on database digiexpress from public;
create user digiexpress;
grant all privileges on database "digiexpress" to digiexpress;
\password digiexpress
```
Use password `example` for digiexpress user.


## Start backend

``` 
cd extensions/app-spring
mvn spring-boot:run
```

## Start ui

```
cd composer-ui
yarn start
```

UI will be accessible at `http://localhost:3000/portal`.


# Building
## Build profiles

### Test coverage

```bash
mvn -Pdefault,coverage clean install
```

### Build composer UI

```bash
mvn -Pdefault,composer-ui clean install
```
