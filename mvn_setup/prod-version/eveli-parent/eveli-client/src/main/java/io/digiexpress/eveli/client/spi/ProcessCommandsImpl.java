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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.client.api.DialobCommands;
import io.digiexpress.eveli.client.api.ImmutableProcess;
import io.digiexpress.eveli.client.api.ProcessCommands;
import io.digiexpress.eveli.client.api.TaskCommands.TaskStatus;
import io.digiexpress.eveli.client.persistence.entities.ProcessEntity;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import io.digiexpress.eveli.client.spi.asserts.WorkflowAssert;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class ProcessCommandsImpl implements ProcessCommands {
  
  private final DialobCommands forms;
  private final ProcessRepository processJPA;
  private final EveliAssetClient workflowCommands;

  @Override
  public ProcessQuery query() {
    return new ProcessQuery() {
      @Override
      public Optional<ProcessCommands.Process> get(String id) {
        return processJPA.findById(Long.parseLong(id)).map(ProcessCommandsImpl::map);
      }
      @Override
      public Optional<ProcessCommands.Process> getByQuestionnaireId(String questionnaireId) {
        return processJPA.findByQuestionnaire(questionnaireId).map(ProcessCommandsImpl::map);
      }
      @Override
      public List<ProcessCommands.Process> findAll() {
        return StreamSupport.stream(processJPA.findAll(Sort.unsorted()).spliterator(), false)
            .map(ProcessCommandsImpl::map)
            .collect(Collectors.toList());
      }
      @Override
      public Optional<ProcessCommands.Process> getByTaskId(String id) {
        return processJPA.findByTask(id).map(ProcessCommandsImpl::map);
      }
      @Override
      public List<ProcessCommands.Process> findAllByUserId(String userId) {
        return processJPA.findAllByUserId(userId).stream().map(ProcessCommandsImpl::map).toList();
      }
      @Override
      public Page<ProcessEntity> find(String name, List<String> status, String userId, Pageable page) {
        List<ProcessStatus> statusList = new ArrayList<>();
        if (status == null || status.isEmpty()) {
          status = new ArrayList<>();
          for (final var  val : ProcessStatus.values()) {
            statusList.add(val);
          }
        }
        else {
          for (String s : status) {
            try {
              var statusEnum = ProcessStatus.valueOf(s);
              statusList.add(statusEnum);
            }
            catch (IllegalArgumentException | NullPointerException e) {
              // ignore
              log.warn("Incorrect process status to find method: {}", s);
            }
          }
        }
        return processJPA.searchProcesses('%' + name + '%', statusList, userId, page);
      }
    };
  }
  @Override
  public ProcessStatusBuilder status() {
    return new ProcessStatusBuilder() {
      @Override
      public void answeredByQuestionnaire(String questionnaireId, String taskId) {
        final var process = processJPA.findByQuestionnaire(questionnaireId);
        if (process.isPresent()) {
          processJPA.save(process.get().setStatus(ProcessStatus.ANSWERED).setTask(taskId));
        } else {
          log.warn("No process for questionnaire id {}, ignoring.", questionnaireId);
        }
      }
      @Override
      public void inProgress(String id) {
        setStatus(id, ProcessStatus.IN_PROGRESS);
      }
      @Override
      public void completed(String id) {
        setStatus(id, ProcessStatus.COMPLETED);
      }
      @Override
      public void rejected(String id) {
        setStatus(id, ProcessStatus.REJECTED);
      }
      @Override
      public void answered(String id) {
        setStatus(id, ProcessStatus.ANSWERED);
      }
      @Override
      public void taskStatusChange(String taskId, TaskStatus taskStatus) {
        final var process = processJPA.findByTask(taskId);
        if (process.isPresent()) {
          final var entity = process.get();
          final var status = entity.getStatus();
          log.debug("Handling task {} status change to {} for process {} with status {}", taskId, taskStatus, entity.getId(), status);
          
          if (status != ProcessStatus.COMPLETED && status != ProcessStatus.REJECTED) {
            if (taskStatus == TaskStatus.COMPLETED) {
              entity.setStatus(ProcessStatus.COMPLETED);
            }
            else if (taskStatus == TaskStatus.REJECTED) {
              entity.setStatus(ProcessStatus.REJECTED);
            }
            processJPA.save(entity);
          }
        } else {
          log.info("Task status change handler: No process for task id {}, ignoring.", taskId);
        }
      }
    };
  }  
  private void setStatus(String id, ProcessStatus status) {
    processJPA.findById(Long.parseLong(id)).ifPresent(entity -> processJPA.save(entity.setStatus(status)));
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static Process map(ProcessEntity entity) {
    return ImmutableProcess.builder()
    .id(entity.getId())
    .status(entity.getStatus())
    .workflowName(entity.getWorkflowName())
    .questionnaire(entity.getQuestionnaire())
    .task(Long.parseLong(entity.getTask()))
    .userId(entity.getUserId())
    .created(entity.getCreated())
    .updated(entity.getUpdated())
    .inputContextId(entity.getInputContextId())
    .inputParentContextId(entity.getInputParentContextId())
    .build();
  }
  
  @Setter
  @Accessors(fluent = true)
  public static class Builder {
    private DialobCommands forms;
    private ProcessRepository processJPA;
    private EveliAssetClient workflowCommands;
  
    public ProcessCommandsImpl build() {
      WorkflowAssert.notNull(forms, () -> "forms must be defined!");
      WorkflowAssert.notNull(processJPA, () -> "processJPA must be defined!");
      WorkflowAssert.notNull(workflowCommands, () -> "workflowCommands must be defined!");
      return new ProcessCommandsImpl(forms, processJPA, workflowCommands);
    }
  }
}
