# Migration Fragment for Version 6.0.0

## Wrench asset services
there is no more ProgramContext is replaced with:
io.resys.limaone.program.Runtime

which holds dedicated methods for dialob and access to DI.
Runtime.getProperties().getBean(Class<T> type);
Runtime.getProperties().getFormDb().withTenant().formInstanceQuery().getOneSync(String questionnaireId);



---
in most cases just replace dialob access with 

    final var dialob = ctx.getProperties()
      .getFormDb().withTenant().formInstanceQuery()
      .getOneSync(input.questionnaireId);
      
      
    final var om = ctx.getProperties().getBean(ObjectMapper.class);