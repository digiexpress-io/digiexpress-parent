# Common database operations

## Contents
1. Reinitialising and repopulating the database
2. Accessing Adminer for viewing the database

### Reinitialising and repopulating the database

When the backend data models change, you need to drop the old, outdated tables, recreate new tables, and then populate them with fresh demo data.  

Following these steps will 
1. Drop all existing db tables
2. Create fresh tables
3. Populate the tables with demo data

Do a Maven install

```bash
cd tasks-parent
mvn clean install
```

If tests fail

```bash
mvn clean install -DskipTests=true
```

Start the backend

```bash
cd digiexpress-parent
mvn -f mvn_setup/digiexpress-app/pom.xml compile quarkus:dev
```

Drop and reinitialise the tables

In browser: `http://localhost:8080/q/tasks/api/reinit`  

Populate new tables with demo data

In browser: `http://localhost:8080/q/tasks/api/reinit-assets` 


### Accessing Adminer for viewing the database

The database is a PostgreSQL database running in a Docker container.

Adminer is a web UI for managing the database that is also running in a Docker container.  
It can be accessed at http://localhost:8091/

To access the database via Adminer, use the following connection details (accessing from a container):

```
system: PostgreSQL
server: thena_tasks_pg_db
user: postgres
password: postgres
database: tasks_db
```