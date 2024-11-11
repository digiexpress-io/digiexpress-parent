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
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.client.api.ProcessClient.CreateProcessExecutor;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.api.ProcessClient.QueryProcessInstances;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@RequiredArgsConstructor
@Data @Accessors(fluent = true)
public class CreateProcessExecutorImpl implements CreateProcessExecutor {
  
  
  private final Supplier<QueryProcessInstances> processCommands;
  private final HdesClient hdesClient;
  private final Supplier<ProgramEnvir> programEnvir;
  private final TransactionWrapper ts;
  private final EveliAssetClient workflowCommands;
  
  private ProcessInstance instance;
  
  @Override
  public CreateProcessExecutor processInstance(ProcessInstance process) {
    this.instance = process;
    return this;
  }
  
  @Override
  public FlowResult execute() {
    TaskAssert.notNull(instance, () -> "process instance must be defined!");
    return ts.map(em -> executeWorkflow());
  }
  
  private FlowResult executeWorkflow() {

    final var flowInput = new HashMap<String, Serializable>();
    flowInput.put("questionnaireId", instance.getQuestionnaireId());
    flowInput.put("workflowName", instance.getWorkflowName());
    
    final FlowResult run = hdesClient.executor(programEnvir.get())
        .inputMap(flowInput)
        .flow(instance.getFlowName())
        .andGetBody();
    
    return run;
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
