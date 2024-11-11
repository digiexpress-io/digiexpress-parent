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

import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstanceStatusBuilder;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessStatus;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.persistence.repositories.ProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ProcessInstanceStatusBuilderImpl implements ProcessInstanceStatusBuilder {
  
  private final ProcessRepository processJPA;
  
  @Override
  public void answeredByQuestionnaire(String questionnaireId, String taskId) {
    final var process = processJPA.findByQuestionnaireId(questionnaireId);
    if (process.isPresent()) {
      processJPA.save(process.get().setStatus(ProcessStatus.ANSWERED).setTaskId(Long.parseLong(taskId)));
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
  public void taskStatusChange(Long taskId, TaskStatus taskStatus) {
    final var process = processJPA.findByTaskId(taskId);
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
  
  private void setStatus(String id, ProcessStatus status) {
    processJPA.findById(Long.parseLong(id)).ifPresent(entity -> processJPA.save(entity.setStatus(status)));
  }
}
