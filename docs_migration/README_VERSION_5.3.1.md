# Migration Fragment for Version 5.3.1

## ProcessClient Removal

`ProcessClient` is removed.

The same functionality can be accessed via `TaskClient` methods. See:
- `QueryTaskProcesses queryTaskProcesses();`
- `ModifyProcess modifyProcess();`
- `CreateProcess createProcess();`

## Migration Path

"Wrench flow services" that previously used `ProcessClient` should remove all `ProcessClient` usage. Process status handling is now automatic within the transaction lock after wrench flow execution.

The process is automatically updated with:
- `form_body`
- `flow_body`
- `task_id` (queried by questionnaire ID after execution)

Wrench flows have the following properties available in their input:

```java
flowInput.put("processId", instance.getId());
flowInput.put("questionnaireId", instance.getQuestionnaireId());
flowInput.put("workflowName", instance.getWorkflowName());
```

```groovy
package io.resys.wrench.assets.bundle.groovy;
import java.io.Serializable;
import io.resys.hdes.client.api.programs.ServiceData;
import io.resys.hdes.client.api.programs.Program.ProgramContext;

  
public class ProcessUpdater {
  public Output execute(Input input, ProgramContext ctx) {
    return new Output();
  }

  @ServiceData
  public static class Input implements Serializable {
    String ssn
    String questionnaireId
    String taskId
  }
  
  @ServiceData
  public static class Output implements Serializable {}
}
```