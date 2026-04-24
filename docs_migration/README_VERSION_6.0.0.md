# Migration Fragment for Version 6.0.0

## Update application to underlying version 6
Update application and dependencies to V6.
Deploy application.

## Convert assets
Update in class `io.digiexpress.eveli.mig.V6Runner` database connections to database.
Connect to original database and run query:

``` 
select * from tenants;
```
and check that tenant names correspond to names in `V6Migration` parameters.
Run this as JUnit test.
On successful conversion update wrench assets manually (see below).

## Wrench asset services
there is no more ProgramContext, it is replaced with: `io.resys.limaone.program.Runtime`

which holds dedicated methods for dialob and access to DI:

``` 
Runtime.getProperties().getBean(Class<T> type);
Runtime.getProperties().getFormDb().withTenant().formInstanceQuery().getOneSync(String questionnaireId);
```


---
in most cases just replace dialob or bean access with 

``` 
    final var dialob = ctx.getProperties()
      .getFormDb().withTenant().formInstanceQuery()
      .getOneSync(input.questionnaireId);
      
      
    final var om = ctx.getProperties().getBean(ObjectMapper.class);
``` 

Following can be removed from theWrench service code:
* import statements in the beginning of service
* `@ServiceData` annotations for input and output

NB! New version is more strict in flow validation, so if there are non-existing input or output parameters used then this is flagged as error, in old version it was ignored. So these issues should be fixed after wrench services are updated.
 