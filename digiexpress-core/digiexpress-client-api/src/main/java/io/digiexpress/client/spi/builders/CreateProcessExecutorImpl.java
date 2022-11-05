package io.digiexpress.client.spi.builders;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.digiexpress.client.api.ImmutableExecutionBody;
import io.digiexpress.client.api.ImmutableProcessCreated;
import io.digiexpress.client.api.ImmutableProcessState;
import io.digiexpress.client.api.ImmutableStep;
import io.digiexpress.client.api.ProcessState.ProcessCreated;
import io.digiexpress.client.api.ServiceClient.CreateProcessExecutor;
import io.digiexpress.client.api.ServiceClient.ExecutionBody;
import io.digiexpress.client.api.ServiceClient.ServiceClientConfig;
import io.digiexpress.client.api.ServiceDocument;
import io.digiexpress.client.api.ServiceDocument.ProcessValue;
import io.digiexpress.client.api.ServiceEnvir.ServiceWrapper;
import io.digiexpress.client.spi.support.ServiceExecutorException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateProcessExecutorImpl implements CreateProcessExecutor {
  private final ServiceClientConfig config;
  private final ServiceWrapper wrapper;
  private final ProcessValue processValue;
  private final Map<String, Serializable> values = new HashMap<>();
  private LocalDateTime targetDate;
  
  @Override
  public CreateProcessExecutor actions(Map<String, Serializable> initVariables) {
    values.putAll(initVariables);
    return this;
  }
  @Override
  public CreateProcessExecutor action(String variableName, Serializable variableValue) {
    if(values.containsKey(variableName)) {
      throw ServiceExecutorException.processInitVariableAlreadyDefined(wrapper, processValue, () -> "Variable name: " + variableName);
    }
    return this;
  }
  @Override
  public CreateProcessExecutor targetDate(LocalDateTime now) {
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
}
