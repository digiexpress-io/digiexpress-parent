package io.digiexpress.client.spi.executors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.digiexpress.client.api.ImmutableExecutionBody;
import io.digiexpress.client.api.ImmutableProcessCreated;
import io.digiexpress.client.api.ImmutableProcessState;
import io.digiexpress.client.api.ImmutableServiceRef;
import io.digiexpress.client.api.ImmutableServiceRel;
import io.digiexpress.client.api.ImmutableStep;
import io.digiexpress.client.api.ProcessState;
import io.digiexpress.client.api.ProcessState.ProcessCreated;
import io.digiexpress.client.api.ServiceClient.ExecutionBody;
import io.digiexpress.client.api.ServiceClient.ProcessExecutor;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceDocument.ProcessValue;
import io.digiexpress.client.api.ServiceEnvir;
import io.digiexpress.client.spi.support.ExecutorException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProcessExecutorImpl implements ProcessExecutor {
  private final ServiceClientConfig config;
  private final String nameOrId;
  private final ServiceEnvir envir;
  private final Map<String, Serializable> values = new HashMap<>();
  private LocalDateTime targetDate;

  @Override
  public ProcessExecutor actions(Map<String, Serializable> initVariables) {
    values.putAll(initVariables);
    return this;
  }
  @Override
  public ProcessExecutor action(String variableName, Serializable variableValue) {
    if(values.containsKey(variableName)) {
      throw ExecutorException.processInitVariableAlreadyDefined(nameOrId, () -> "Variable name: " + variableName);
    }
    return this;
  }
  @Override
  public ProcessExecutor targetDate(LocalDateTime now) {
    this.targetDate = now;
    return this;
  }
  @Override
  public ExecutionBody<ProcessState> build() {
    final var targetDate = this.targetDate == null ? LocalDateTime.now() : this.targetDate;
    final var rel = this.envir.getRel(targetDate).getDelegate(config);
    final var def = this.envir.getDef(targetDate).getDelegate(config);
    
    
    ProcessValue processValue = def.getProcesses().stream()
        .filter(p -> p.getId().equalsIgnoreCase(nameOrId) || p.getName().equalsIgnoreCase(nameOrId))
        .findFirst().orElse(null);
    
    if(processValue == null) {
      final var wk = this.envir.getStecil(targetDate).getDelegate(config).getSites().values().stream()
          .flatMap(e -> e.getLinks().values().stream().filter(t -> t.getWorkflow()))
          .filter(w -> w.getId().equals(nameOrId) || w.getName().equals(nameOrId))
          .findFirst()
          .orElseThrow(() -> ExecutorException.processNotFound(nameOrId, () -> ""));
      
      processValue = def.getProcesses().stream()
          .filter(p -> p.getName().equalsIgnoreCase(wk.getValue()))
          .findFirst().orElseThrow(() -> ExecutorException.processNotFound(nameOrId, () -> ""));
    }

  
    final var step = ImmutableStep.<ProcessCreated>builder()
        .id(config.getStore().getGid().getNextId(ServiceDocument.DocumentType.SERVICE_DEF))
        .version(1)
        .body(ImmutableProcessCreated.builder()
            .flowId(processValue.getFlowId())
            .formId(processValue.getFormId())
            .desc(processValue.getDesc())
            .name(processValue.getName())
            .build())
        .start(targetDate)
        .end(targetDate)
        .build();
    
    final var iniParams = Collections.unmodifiableMap(values);
    final var state = ImmutableProcessState.builder()
        .id(config.getStore().getGid().getNextId(ServiceDocument.DocumentType.SERVICE_RELEASE))
        .version(1)
        .def(ImmutableServiceRef.builder().id(def.getId()).version(def.getVersion()).build())
        .rel(ImmutableServiceRel.builder().id(rel.getId()).version(rel.getVersion()).name(rel.getName()).build())
        .addSteps(step)
        .build();
    return ImmutableExecutionBody.<ProcessState>builder().body(state).build();
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
