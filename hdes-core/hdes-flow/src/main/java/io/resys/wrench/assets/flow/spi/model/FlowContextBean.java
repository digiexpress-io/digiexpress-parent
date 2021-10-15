package io.resys.wrench.assets.flow.spi.model;

/*-
 * #%L
 * wrench-component-flow
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.hdes.client.api.programs.FlowResult.FlowContext;
import io.resys.hdes.client.api.programs.FlowResult.FlowExecutionLog;
import io.resys.hdes.client.api.programs.FlowResult.FlowExecutionStatus;
import io.resys.hdes.client.api.programs.FlowResult.FlowTask;
import io.resys.hdes.client.spi.util.HdesAssert;

public class FlowContextBean implements FlowContext {

  private static final long serialVersionUID = -8670956405426919220L;

  private FlowExecutionStatus status;
  private String pointer;
  private final Collection<FlowResultLog> history;
  private final Collection<FlowTask> tasks;
  private final Map<String, Serializable> variables;

  public FlowContextBean(FlowExecutionStatus status, String pointer,
      Collection<FlowResultLog> history, Collection<FlowTask> tasks, Map<String, Serializable> variables) {
    super();
    this.status = status;
    this.pointer = pointer;
    this.history = history;
    this.tasks = tasks;
    this.variables = variables;
  }
  @Override
  public FlowExecutionStatus getStatus() {
    return status;
  }
  @Override
  public FlowContext setStatus(FlowExecutionStatus status) {
    this.status = status;
    return this;
  }
  @Override
  public String getPointer() {
    return this.pointer;
  }
  @Override
  public FlowContext setPointer(String taskModelId) {
    this.pointer = taskModelId;
    return this;
  }
  @Override
  public Collection<FlowResultLog> getHistory() {
    return Collections.unmodifiableCollection(history);
  }
  @Override
  public FlowResultLog getHistory(String taskId) {
    return this.history.stream().filter(t -> t.getId().equals(taskId)).findFirst().get();
  }
  @Override
  public FlowContext addHistory(FlowResultLog history) {
    this.history.add(history);
    return this;
  }
  @Override
  public Collection<FlowTask> getTasks() {
    return Collections.unmodifiableCollection(tasks);
  }
  @Override
  public Collection<FlowTask> getTasks(String modelId) {
    return this.tasks.stream().filter(t -> t.getModelId().equals(modelId)).collect(Collectors.toList());
  }
  @Override
  public FlowContext addTask(FlowTask task) {
    this.tasks.add(task);
    return this;
  }
  @Override
  public Map<String, Serializable> getVariables() {
    return Collections.unmodifiableMap(variables);
  }
  @Override
  public String getShortHistory() {
    List<String> result = new ArrayList<>();
    for(FlowResultLog history : history) {
      result.add(history.getModelId());
    }
    return result.toString();
  }
  @Override
  public FlowTask getTask(String taskId) {
    Optional<FlowTask> task = this.tasks.stream().filter(t -> t.getId().equals(taskId)).findFirst();
    HdesAssert.isTrue(task.isPresent(), () -> "No task with id:" + taskId + " known task id-s: " + this.tasks.stream().map(t -> t.getId()).collect(Collectors.toList())+ "!");
    return task.get();
  }
  @Override
  public FlowContext putVariable(String name, Serializable value) {
    this.variables.put(name, value);
    return this;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public <T> T getTaskOutput(String taskName) {
    Collection<FlowTask> tasks = this.getTasks(taskName);
    if(tasks.isEmpty()) {
      return null;
    }
    FlowTask task = tasks.iterator().next();
    return (T) task.getVariables().get(taskName);
  }
}
