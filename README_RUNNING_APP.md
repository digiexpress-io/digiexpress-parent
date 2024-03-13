# Installing and running the application

## Contents

* Installing backend dependencies
* Installing frontend dependencies
* Running the application
* Ports used
* Where to view all REST endpoints

## Installing backend dependencies

```bash
cd digiexpress-parent/mvn_setup
mvn clean install
```

If tests fail, run 

```bash
mvn clean install -DskipTests=true
```

## Installing frontend dependencies

```bash
cd digiexpress-parent/ts_setup/tasks_ui
corepack pnpm install
```

# Running the application

## Start backend

```bash
cd digiexpress-parent/
mvn -f mvn_setup/digiexpress-app/pom.xml compile quarkus:dev
```

## Start UI

```bash
cd digiexpress-parent/ 
corepack pnpm -C ts_setup/tasks-ui start
```

## Ports used

* Backend db: port 8080 http://localhost:8080/    
* Adminer: port 8091 http://localhost:8091/  
* TODO Frontend: port 5173 http://localhost:5173/   

## Where to view all REST endpoints

REST endpoints can be seen here (when backend is running)    
http://localhost:8080/