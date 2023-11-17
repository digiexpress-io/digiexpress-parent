# Build docker image

docker build -f src/main/docker/Dockerfile.jvm -t digiexpress-dev-app:1-snapshot .


# Run docker image

docker-compose -f src/main/docker/tasks-docker.yml up -d 

## Colima

```sh
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
export DOCKER_HOST="unix://${HOME}/.colima/default/docker.sock"
```