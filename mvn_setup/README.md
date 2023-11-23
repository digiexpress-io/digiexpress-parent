# Build docker image locally
TODO

# Run docker image
```shell
docker-compose up -d
```

### If you need just database
```shell
docker-compose up -d db adminer
```

#### Adminer

Adminer is available on http://localhost:8091/ 

| Attribute | Value        |
|-----------|--------------|
| System    | `PostgreSQL` |
| Server    | `db`         |
| Username  | `postgres`   |
| Password  | `postgres`   |
| Database  | `tasks_db`   |

# Running application

## Locally

### Using dev UI from CDN

```shell
DIGIEXPRESS_ASSETS_URL=https://cdn.resys.io/digiexpress-io/tasks-ui/dev/ \
DIGIEXPRESS_INDEX_PAGE=index.html \
./mvnw quarkus:dev -rf :digiexpress-app
```

### Using Local react-scripts server

Start server on project `tasks-ui`.
```shell
yarn start
```

In this folder
```shell
sdk env
./mvnw quarkus:dev -rf :digiexpress-app
```
open http://localhost:8080/portal/. If you need to setup database http://localhost:8080/q/demo/api/reinit


## Colima

If you are using MacOS, install Colima and set variables

```shell
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
```



