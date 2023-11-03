# Build docker image

docker build -f src/main/docker/Dockerfile.jvm -t digiexpress-dev-app:1-snapshot .


# Run docker image

docker-compose -f src/main/docker/tasks-docker.yml up -d 