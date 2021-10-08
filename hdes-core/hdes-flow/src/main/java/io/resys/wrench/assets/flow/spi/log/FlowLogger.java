package io.resys.wrench.assets.flow.spi.log;

/*-
 * #%L
 * wrench-assets-flow
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import io.resys.hdes.client.api.execution.FlowResult;
import io.resys.hdes.client.api.execution.FlowResult.FlowHistory;
import io.resys.hdes.client.api.execution.FlowResult.FlowTask;
import io.resys.hdes.client.api.execution.FlowProgram.Step;
import io.resys.hdes.client.api.execution.FlowProgram.FlowTaskType;

public class FlowLogger {

  private FlowResult flow;

  public FlowLogger flow(FlowResult flow) {
    this.flow = flow;
    return this;
  }

  public Map<String, Object> build() {
    Map<String, Object> result = new HashMap<>();
    BiConsumer<String, Object> parent = (key, value) -> result.put(key, value);
    for(FlowHistory history : flow.getContext().getHistory()) {
      FlowTask task = flow.getContext().getTask(history.getId());
      Step taskModel = flow.getModel().getSteps().stream().filter(t -> t.getId().equals(task.getModelId())).findFirst().get();
      if(taskModel.getType() == FlowTaskType.DT || taskModel.getType() == FlowTaskType.SERVICE) {
        parent = createTaskLog(parent, task);
      }
    }

    return result;
  }

  protected BiConsumer<String, Object> createTaskLog(BiConsumer<String, Object> parent, FlowTask task) {
    Map<String, Object> taskLog = new HashMap<>();
    if(!task.getInputs().isEmpty()) {
      taskLog.put("inputs", task.getInputs());
    }
    taskLog.put("output", task.getVariables().get(task.getModelId()));
    parent.accept(task.getModelId(), taskLog);

    BiConsumer<String, Object> current = (key, value) -> {
      Map<String, Object> next = new HashMap<>();
      next.put(key, value);
      taskLog.put("next", next);
    };

    return current;
  }
}
