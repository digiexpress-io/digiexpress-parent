package io.resys.wrench.assets.bundle.spi.flow.executors;

/*-
 * #%L
 * wrench-component-assets
 * %%
 * Copyright (C) 2016 - 2017 Copyright 2016 ReSys OÜ
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.util.Assert;

import io.resys.hdes.client.api.programs.FlowResult;
import io.resys.hdes.client.api.programs.DecisionResult.DecisionTableOutput;
import io.resys.hdes.client.api.programs.FlowProgram.Step;
import io.resys.hdes.client.api.programs.FlowProgram.StepBody;
import io.resys.hdes.client.api.programs.FlowResult.FlowTask;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.AssetService;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceQuery;
import io.resys.wrench.assets.bundle.api.repositories.AssetServiceRepository.ServiceResponse;
import io.resys.wrench.assets.bundle.spi.dt.resolvers.FlowDtInputResolver;
import io.resys.wrench.assets.bundle.spi.dt.resolvers.LoggingFlowDtInputResolver;
import io.resys.wrench.assets.flow.api.FlowExecutorRepository.FlowTaskExecutor;


public class GenericFlowDtExecutor implements FlowTaskExecutor  {

  private final VariableResolver variableResolver;
  private final Supplier<ServiceQuery> query;

  public GenericFlowDtExecutor(Supplier<ServiceQuery> query, VariableResolver variableResolver) {
    super();
    this.query = query;
    this.variableResolver = variableResolver;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Step execute(FlowResult flow, FlowTask task) {
    Step node = flow.getModel().getStep().get(task.getModelId());

    StepBody taskValue = node.getBody();
    AssetService service = query.get().dt(taskValue.getRef());

    Map<String, Serializable> inputs = new HashMap<>();
    ServiceResponse response = service.newExecution().insert(new LoggingFlowDtInputResolver(inputs, new FlowDtInputResolver(flow, node, variableResolver))).run();
    List<DecisionTableOutput> outputs = (List<DecisionTableOutput>) response.list();
    task
    .putInputs(inputs)
    .putVariables(createVariables(flow, task, taskValue, outputs));

    flow.complete(task);
    return node.getNext().iterator().next();
  }

  protected Map<String, Serializable> createVariables(FlowResult flow, FlowTask task, StepBody taskValue, List<DecisionTableOutput> outputs) {
    final Serializable value;
    if(taskValue.isCollection()) {
      List<Serializable> entities = new ArrayList<>();
      for(DecisionTableOutput out : outputs) {
        entities.add((Serializable) out.getValues());
      }
      value = (Serializable) entities;
    } else {
      Assert.isTrue(outputs.size() <= 1, "Flow: " + flow.getId() + " task: " + task.getId() + " must produce: [0..1] outputs but was: " + outputs.size() + "!");
      if(outputs.isEmpty()) {
        value = new HashMap<>();
      } else {
        value = (Serializable) outputs.iterator().next().getValues();
      }
    }

    Map<String, Serializable> variables = new HashMap<>();
    variables.put(task.getModelId(), value);
    return variables;
  }
}
