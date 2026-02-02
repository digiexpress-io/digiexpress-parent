package io.digiexpress.eveli.client.spi.task.visitors;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.digiexpress.eveli.client.api.TaskClient.MergeProcess;
import io.digiexpress.eveli.client.api.TaskClient.ModifyProcess;
import io.digiexpress.eveli.client.api.TaskClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient.Task;
import io.digiexpress.eveli.client.spi.asserts.TaskAssert;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStore;
import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyProcessVisitor implements ModifyProcess {
  private final TaskStore ctx;
  
  private String commitMessage;
  private String commitAuthor;
  private String id;
  
  private Function<MergeProcess, Uni<?>> onAnyUni;
  private BiConsumer<Optional<Task>, MergeProcess> onTask;
  private BiConsumer<ProcessInstance, MergeProcess> onMerger;

  private MergeProcessImpl internalMerger = new MergeProcessImpl();
  
  @Override
  public ModifyProcess id(String id) {
    this.id = id;
    return this;
  }
  @Override
  public ModifyProcess commitMessage(String commitMessage) {
    this.commitMessage = commitMessage;
    return this;
  }
  @Override
  public ModifyProcess commitAuthor(String commitAuthor) {
    this.commitAuthor = commitAuthor;
    return this;
  }
  @Override
  public ModifyProcess onAnyUni(Function<MergeProcess, Uni<?>> onAnyUni) {
    this.onAnyUni = onAnyUni;
    return this;
  }
  @Override
  public ModifyProcess onTask(BiConsumer<Optional<Task>, MergeProcess> onTask) {
    this.onTask = onTask;
    return this;
  }
  @Override
  public ModifyProcess merge(BiConsumer<ProcessInstance, MergeProcess> onMerger) {
    this.onMerger = onMerger;
    return this;
  }

  private Uni<?> visitStep1_txCrossLock(ThenaGrimMergeObject.MergeProcess mergeProc) {
    if(onAnyUni != null) {
      return onAnyUni.apply(internalMerger.start(mergeProc));      
    }
    return Uni.createFrom().voidItem();
  }
  
  private void visitStep2_onTaskStateLoad(Optional<GrimMissionContainer> grimMissionbyQuestionnaireId, ThenaGrimMergeObject.MergeProcess mergeProc) {
    if(onTask != null) {
      final var task = grimMissionbyQuestionnaireId.map(TaskMapper::map);
      onTask.accept(task, internalMerger.start(mergeProc));
    }
  }
  
  private void visitStep3_addNewProps(GrimProcess grimProcess, ThenaGrimMergeObject.MergeProcess mergeProc) {
    onMerger.accept(internalMerger.getCurrentState(), internalMerger.start(mergeProc));
  }
  
  @Override
  public Uni<ProcessInstance> build() {
    TaskAssert.notEmpty(commitMessage, () -> "commitMessage can't be empty!");
    TaskAssert.notEmpty(commitAuthor, () -> "commitAuthor can't be empty!");
    TaskAssert.notEmpty(id, () -> "id can't be empty!");
    TaskAssert.notNull(onMerger, () -> "merger can't be empty!");
    
    final var config = ctx.getConfig();
    final var grim = config.getClient().grim(config.getTenantName());
    
    return grim.commit().modifyOneProc()
      .commitAuthor(commitAuthor)
      .commitMessage(commitMessage)
      .procId(id)
      .onAnyUni(mergeProc -> visitStep1_txCrossLock(mergeProc))
      .onMission((currentState, mergeProc) -> visitStep2_onTaskStateLoad(currentState, mergeProc))
      .modifyProc((grimProcess, mergeProc) -> visitStep3_addNewProps(grimProcess, mergeProc))
      .build().map(e -> {
        if(e.getStatus() != CommitResultStatus.OK || e.getProc() == null) {
          throw TaskException.builder("MODIFY_ONE_TASK_PROC_FAIL").add(grim, e).build();
        }
        return TaskMapper.map(e.getProc());
      });
  }
  

  private static class MergeProcessImpl implements MergeProcess {
    private ThenaGrimMergeObject.MergeProcess delegate;
    private ProcessInstance currentState;
    
    private Optional<GrimProcessStatus> status;
    private Optional<String> taskId;
    private Optional<String> formBody;
    private Optional<String> flowBody;

    
    public MergeProcessImpl start(ThenaGrimMergeObject.MergeProcess delegate) {
      this.delegate = delegate;
      this.currentState = TaskMapper.map(delegate.getCurrentState());     
      return this;
    } 
    
    @Override
    public MergeProcess status(GrimProcessStatus status) {
      TaskAssert.notNull(status, () -> "status can't be null!");
      this.status = Optional.ofNullable(status);
      return this;
    }
    @Override
    public MergeProcess taskId(String taskId) {
      this.taskId = Optional.ofNullable(taskId);
      return this;
    }
    @Override
    public MergeProcess formBody(String formBody) {
      this.formBody = Optional.ofNullable(formBody);
      return this;
    }
    @Override
    public MergeProcess flowBody(String flowBody) {
      this.flowBody = Optional.ofNullable(flowBody);
      return this;
    }
    @Override
    public ProcessInstance getCurrentState() {
      return currentState;
    }

    @Override
    public ProcessInstance skip() {
      delegate.skip();
      return currentState;
    }

    @Override
    public ProcessInstance build() {
      if(status != null) {
        delegate.status(status.get());
      }
      if(taskId != null) {
        delegate.missionId(taskId.orElse(null));
      }
      if(formBody != null) {
        delegate.formBody(formBody.map(text -> new JsonObject(text)).orElse(null));
      }
      if(flowBody != null) {
        delegate.flowBody(flowBody.map(text -> new JsonObject(text)).orElse(null));
      }      
      return TaskMapper.map(delegate.build());
    }
    
  }
}
