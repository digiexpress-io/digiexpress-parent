# Reinit endpoint

When the backend data models change, you need to drop your tables, recreate new tables, and then populate them with demo task data.  

You can accomplish this quickly following these steps:

1. Start up the `tasks-parent/quarkus-dev-app` as usual with `mvn compile quarkus:dev`
2. In the browser, enter these endpoints

`http://localhost:8080/q/tasks/api/reinit`    
`http://localhost:8080/q/tasks/api/demo/populate/{numberOfTasks}`

You should then be able to proceed as normal.