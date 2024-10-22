package io.digiexpress.eveli.client.spi;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.digiexpress.eveli.client.api.HdesCommands;
import io.digiexpress.eveli.client.api.ProcessCommands;
import io.digiexpress.eveli.client.api.ProcessCommands.Process;
import io.digiexpress.eveli.client.api.WorkflowCommands;
import io.digiexpress.eveli.client.api.WorkflowCommands.Workflow;
import io.digiexpress.eveli.client.spi.asserts.WorkflowAssert;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.FlowProgram.FlowExecutionStatus;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RequiredArgsConstructor
public class HdesCommandsImpl implements HdesCommands {
  private final ProcessCommands processCommands;
  private final HdesClient hdesClient;
  private final Supplier<ProgramEnvir> programEnvir;
  private final TransactionWrapper ts;
  private final WorkflowCommands workflowCommands;
  
  @Override
  public HdesQuery query() {
    return new HdesQuery() {
      @Override
      public List<String> findFlowNames() {
        return new ArrayList<>(programEnvir.get().getFlowsByName().keySet());
      }
    };
  }
  @Override
  public void execute(String dialobSessionId) {
    ts.execute(em -> executeWorkflow(dialobSessionId));
  }
  private void executeWorkflow(String dialobSessionId) {
    FlowResult run;
    final var flowInput = new HashMap<String, Serializable>();
    Optional<Process> processData = Optional.empty();
    Optional<Workflow> wfData = Optional.empty();
    log.info("Hdes commands: dialob session completion for id: {}", dialobSessionId);
    try {
      processData = processCommands.query().getByQuestionnaireId(dialobSessionId);
      if (processData.isEmpty()) {
        log.warn("Hdes commands: No process data for dialob session: {}", dialobSessionId);
      } else {
        wfData = workflowCommands.query().getByName(processData.get().getWorkflowName());
        if (wfData.isEmpty()) {
          throw new RuntimeException("Missing workflow with name " + processData.get().getWorkflowName());
        }
        flowInput.put("questionnaireId", dialobSessionId);
        flowInput.put("workflowName", processData.get().getWorkflowName());
        run = hdesClient.executor(programEnvir.get())
            .inputMap(flowInput)
            .flow(wfData.get().getFlowName())
            .andGetBody();
        if (log.isDebugEnabled()){
          // this version prints out all input/output, could contain sensitive data
          log.debug("Hdes commands: flow for questionnaire: {}, status: {}, output {}", dialobSessionId, run.getStatus(), run.getLogs());
        }
        else if (run.getStatus() == FlowExecutionStatus.ERROR) {
          log.error("Hdes commands: completed flow for questionnaire: {}, status: {}, output {}", dialobSessionId, run.getStatus(), getRunOutput(run));
        }
        else {
          // info level without sensitive data, just status
          log.info("Hdes commands: completed flow for questionnaire: {}, status: {}", dialobSessionId, run.getStatus());
        }

      }
    } catch (RuntimeException e) {
      log.warn(new StringBuilder()
          .append("Hdes commands: error in execution:")
          .append("  - flow: ").append(wfData.map(w -> w.getFlowName()).orElse("undefined"))
          .append("  - formName: ").append(wfData.map(w -> w.getFormName()).orElse("undefined"))
          .append("  - formTag: ").append(wfData.map(w -> w.getFormTag()).orElse("undefined"))
          .append("  - input: ").append(hdesClient.mapper().toJson(flowInput))
          .append("  - error: ").append(e.getMessage())
          .toString(), e);
      throw e;
    }

  }
  
  private Object getRunOutput(FlowResult run) {
    StringBuilder builder = new StringBuilder("[");
    run.getLogs().forEach(logEntry->{
      builder.append("FlowResultLog {id=").append(logEntry.getId())
      .append(", status=").append(logEntry.getStatus())
      .append(", start=").append(logEntry.getStart())
      .append(", end=").append(logEntry.getEnd())
      .append(", errors=").append(logEntry.getErrors())
      .append("},");
    });
    // remove last comma
    if (builder.length() > 1) {
      builder.setLength(builder.length()-1);
    }
    builder.append("]");
    return builder.toString();
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  
  public interface TransactionWrapper {  
    void execute(Consumer<EntityManager> supplier);
    <T> T map(Function<EntityManager, T> supplier);
  }

  public static class SpringTransactionWrapper implements TransactionWrapper {
    
    private final EntityManager entityManager;

    public SpringTransactionWrapper(EntityManager entityManager) {
      super();
      this.entityManager = entityManager;
    }
    
    @Override
    @Transactional
    public void execute(Consumer<EntityManager> supplier) {
      supplier.accept(entityManager);
    }

    @Override
    @Transactional
    public <T> T map(Function<EntityManager, T> supplier) {
      return supplier.apply(entityManager);
    }
  }
  
  @Setter
  @Accessors(fluent = true)
  public static class Builder {
    private ProcessCommands process;
    private WorkflowCommands workflow;
    private Supplier<ProgramEnvir> programEnvir;
    private HdesClient hdesClient;
    private TransactionWrapper transactionWrapper;
    
    public HdesCommandsImpl build() {
      WorkflowAssert.notNull(process, () -> "process commands must be defiend!");
      WorkflowAssert.notNull(workflow, () -> "workflow commands must be defiend!");
      WorkflowAssert.notNull(hdesClient, () -> "hdesClient must be defiend!");
      WorkflowAssert.notNull(transactionWrapper, () -> "transactionWrapper commands must be defiend!");
      
      if(hdesClient.store() instanceof HdesInMemoryStore) {
        log.warn("HDES static compile enabled");
        final var source = hdesClient.store().query().get().await().atMost(Duration.ofMinutes(2));
        final var result = ComposerEntityMapper.toEnvir(hdesClient.envir(), source).build();
        this.programEnvir = () -> result;
      }
      
      if(programEnvir == null) {
        programEnvir = () -> {
          log.warn("HDES dynamic compile enabled");
          final var source = hdesClient.store().query().get().await().atMost(Duration.ofMinutes(1));
          return ComposerEntityMapper.toEnvir(hdesClient.envir(), source).build();
        };
      }
      return new HdesCommandsImpl(process, hdesClient, programEnvir, transactionWrapper, workflow);
    }
  }

}
