package io.digiexpress.client.spi.executors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import io.digiexpress.client.api.ImmutableExecutionBody;
import io.digiexpress.client.api.ImmutableProcessCreated;
import io.digiexpress.client.api.ImmutableProcessState;
import io.digiexpress.client.api.ImmutableStep;
import io.digiexpress.client.api.ProcessState.ProcessCreated;
import io.digiexpress.client.api.ServiceClient.ExecutionBody;
import io.digiexpress.client.api.ServiceClient.ProcessExecutor;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceEnvir;
import io.digiexpress.client.spi.support.ExecutorException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessExecutorImpl implements ProcessExecutor {
  private final ServiceClientConfig config;
  private final String nameOrId;
  private final ServiceEnvir envir;
//  private final ServiceProgram wrapper;
//  private final ProcessValue processValue;
//  private final Map<String, Serializable> values = new HashMap<>();
//  private LocalDateTime targetDate;
  
  @Override
  public ProcessExecutor actions(Map<String, Serializable> initVariables) {
    values.putAll(initVariables);
    return this;
  }
  @Override
  public ProcessExecutor action(String variableName, Serializable variableValue) {
    if(values.containsKey(variableName)) {
      throw ExecutorException.processInitVariableAlreadyDefined(wrapper, processValue, () -> "Variable name: " + variableName);
    }
    return this;
  }
  @Override
  public ProcessExecutor targetDate(LocalDateTime now) {
    this.targetDate = now;
    return this;
  }
  @Override
  public ExecutionBody<Map<String, Serializable>> build() {
    final var targetDate = this.targetDate == null ? LocalDateTime.now() : this.targetDate;
    final var step = ImmutableStep.<ProcessCreated>builder()
        .id(config.getStore().getGid().getNextId(ServiceDocument.DocumentType.SERVICE_DEF))
        .version(1)
        .body(ImmutableProcessCreated.builder().processValue(processValue).build())
        .start(targetDate)
        .end(targetDate)
        .build();
    
    final var actions = Collections.unmodifiableMap(values);
    final var state = ImmutableProcessState.builder()
        .id(config.getStore().getGid().getNextId(ServiceDocument.DocumentType.SERVICE_DEF))
        .version(1)
        .ref(wrapper.getRefId())
        .rel(wrapper.getRelId())
        .addSteps(step)
        .build();
    return ImmutableExecutionBody.<Map<String, Serializable>>builder()
        .actions(actions)
        .state(state)
        .build();
  }
  
//final ServiceProgram doc = envir.getValues().values().stream().findFirst()
//.orElseThrow(() ->
//    ServiceExecutorException.serviceNotFound(
//    () -> "Services available" + 
//      String.join(",", envir.getValues().values().stream()
//        .map(p -> p.getId())
//        .collect(Collectors.toList())) + 
//  "!"));
//
//
//final var process = doc.getDef().getProcesses().stream()
//.filter(p -> p.getId().equals(nameOrId) || p.getName().equals(nameOrId))
//.findFirst()
//.orElseThrow(() -> ServiceExecutorException.processNotFound(doc, nameOrId, 
//  () -> "Accepted in: '" + doc.getId() + "' process id/name: " + 
//      String.join(",", doc.getDef().getProcesses().stream()
//        .map(p -> p.getId() + "/" + p.getName())
//        .collect(Collectors.toList())) + 
//  "!"));
//
//return new CreateProcessExecutorImpl(config, doc, process);

}
