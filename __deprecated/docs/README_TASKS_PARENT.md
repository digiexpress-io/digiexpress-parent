# Reinit endpoint

When the backend data models change, you need to drop your tables, recreate new tables, and then populate them with demo task data.  

You can accomplish this quickly following these steps:

1. Run `mvn clean install` on `tasks-parent`.  If tests fail, run `mvn clean install -DskipTests=true`.
2. Start up the `tasks-parent/quarkus-dev-app` as usual with `mvn compile quarkus:dev`
3. In the browser, enter these endpoints

`http://localhost:8080/q/tasks/api/reinit`    
`http://localhost:8080/q/tasks/api/demo/populate/{numberOfTasks}`

You should then be able to proceed as normal.


### local dev

**install dependencies**

cd thena-parent  
`mvn clean install`

If install is failing due to test failures, run  
`mvn clean install -DskipTests=true`

Backend and frontend are found in tasks-parent

**run backend**

thena-parent/extensions/tasks-parent/quarkus-dev-app    
`mvn compile quarkus:dev`

**run frontend**

thena-parent/extensions/tasks-parent/tasks-ui  
install deps: `yarn install`  
`yarn start`

---

run docker services  
`docker-compose -f thena-parent/doc/tasks-docker-db.yml up -d` 

adminer ui
http://localhost:8091/

populate db with tasks
http://localhost:8080/q/tasks/api/demo/populate/1000

Drop order:

nested_10_refs  
nested_10_tags  
nested_10_commits  
nested_10_treeitems  
nested_10_trees  
nested_10_blobs  
repos