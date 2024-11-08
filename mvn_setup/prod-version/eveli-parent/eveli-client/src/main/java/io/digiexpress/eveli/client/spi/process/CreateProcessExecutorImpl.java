package io.digiexpress.eveli.client.spi.process;

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
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.client.api.ProcessClient.CreateProcessExecutor;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.api.ProcessClient.QueryProcessInstances;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.FlowProgram.FlowExecutionStatus;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RequiredArgsConstructor
public class CreateProcessExecutorImpl implements CreateProcessExecutor {
  
  
  private final Supplier<QueryProcessInstances> processCommands;
  private final HdesClient hdesClient;
  private final Supplier<ProgramEnvir> programEnvir;
  private final TransactionWrapper ts;
  private final EveliAssetClient workflowCommands;
  

  @Override
  public void execute(String dialobSessionId) {
    ts.execute(em -> executeWorkflow(dialobSessionId));
  }
  private void executeWorkflow(String dialobSessionId) {
    FlowResult run;
    final var flowInput = new HashMap<String, Serializable>();
    Optional<ProcessInstance> processData = Optional.empty();
    Optional<Entity<Workflow>> wfData = Optional.empty();
    log.info("Hdes commands: dialob session completion for id: {}", dialobSessionId);
    try {
      processData = processCommands.get().findOneByQuestionnaireId(dialobSessionId);
      if (processData.isEmpty()) {
        log.warn("Hdes commands: No process data for dialob session: {}", dialobSessionId);
      } else {
        wfData = workflowCommands.queryBuilder().findOneWorkflowByName(processData.get().getWorkflowName()).await().atMost(Duration.ofMinutes(1));
        if (wfData.isEmpty()) {
          throw new RuntimeException("Missing workflow with name " + processData.get().getWorkflowName());
        }
        flowInput.put("questionnaireId", dialobSessionId);
        flowInput.put("workflowName", processData.get().getWorkflowName());
        run = hdesClient.executor(programEnvir.get())
            .inputMap(flowInput)
            .flow(wfData.get().getBody().getFlowName())
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
          .append("  - flow: ").append(wfData.map(w -> w.getBody().getFlowName()).orElse("undefined"))
          .append("  - formName: ").append(wfData.map(w -> w.getBody().getFormName()).orElse("undefined"))
          .append("  - formTag: ").append(wfData.map(w -> w.getBody().getFormTag()).orElse("undefined"))
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

}