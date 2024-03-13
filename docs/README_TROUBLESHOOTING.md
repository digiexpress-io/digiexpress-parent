# Troubleshooting

1. Tests are failing when running `mvn clean install`.
* Run `mvn clean install -DskipTests=true`

2. The frontend comes up but doesn't show any data after running `mvn -f mvn_setup/digiexpress-app/pom.xml compile quarkus:dev` in the 
root directory.  

* Check for any docker containers already running on port 8080
  *  `docker ps`  
* Stop and remove these containers.  
  * `docker stop <CONTAINER_ID>`
  * `docker rm <CONTAINER_ID>`  
* Restart backend