package io.digiexpress.eveli.client.spi.task.visitors;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.List;

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.digiexpress.eveli.client.api.TaskClient.TransferTaskCommand;
import io.digiexpress.eveli.client.api.TaskFileClient;
import io.digiexpress.eveli.client.api.TaskFileClient.TaskFile;
import io.digiexpress.eveli.client.spi.dms.DocContainerClient;
import io.digiexpress.eveli.client.spi.dms.DocContainerEnvelope;
import io.digiexpress.eveli.client.spi.dms.ImmutableDoc;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStore;
import io.resys.thena.api.entities.CommitResultStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TransferTaskVisitor {
  
  private final TaskStore ctx;
  private final TaskFileClient taskFileClient;
  private final DocContainerClient docContainerClient;
  
  private final String userId;
  private final String taskId;
  private final TransferTaskCommand command;
  
  public Uni<TaskClient.Task> accept() {
    return getTaskFiles()
      .onItem().transformToUni(files -> createDocContainer(files))
      .onItem().transformToUni(env -> updateTask(env));
  }
  
  private Uni<DocContainerEnvelope> createDocContainer(List<TaskFile> files) {
    final var container = docContainerClient.containerBuilder();
    
    for(final var file : files) {
      container.addDocument(ImmutableDoc.builder()
          .body(file.getBody())
          .bodyType(file.getBodyType())
          .mimeType(file.getMimeType())
          .name(file.getName())
          .externalId(file.getExternalId())
          .build()); 
    }
    return container
        .title(command.getTransferTitle())
        .draftedBy(userId)
        .decidedBy(userId)
        .createdBy(userId)
        .externalId(taskId)
        .build();
  }
  
  private Uni<List<TaskFile>> getTaskFiles() {
    return taskFileClient.queryTaskFiles().findAll(taskId).onItem().transform(files -> {
      if(files.isEmpty()) {
        throw TaskException.builder("TRANSFER_TASK_FAIL_NO_FILES_TO_TRANSFER")
          .add(
              "transfer-files-fail", 
              "Task transfer must contain at least 1 file!", JsonObject.mapFrom(command)).build(); 
      }
      return files;
    });
  }
  
  
  private Uni<TaskClient.Task> updateTask(DocContainerEnvelope env) {
    final var docContainerId = env.getObjects().getId();
    final var config = ctx.getConfig();
    final var tenant = config.getClient().grim(ctx.getConfig().getTenantName());
    
    return tenant.commit().modifyOneMission()
      .missionId(taskId).modifyMission(merge -> {
        merge
        .addLink(newLink -> newLink.linkType(TaskMapper.LINK_TYPE_TRANSFERRED_ID).linkValue(docContainerId).build())
        
        .status(TaskStatus.TRANSFERRED.name())
        // change is viewed by worker who created it
        .addViewer(viewer -> viewer.userId(userId).usedFor(TaskMapper.VIEWER_WORKER).currentTxCommit().build())
        .build();
        
      })
      .commitAuthor(userId)
      .commitMessage("Adding task viewer by: " + TransferTaskVisitor.class.getSimpleName())
      .build()
      .onItem().transform(envelope -> {
        if(envelope.getStatus() == CommitResultStatus.OK) {
          return envelope;
        }
        throw TaskException.builder("TRANSFER_TASK_STATUS_UPDATE_FAIL").add(tenant, envelope).build(); 
      })
      .onItem().transform(TaskMapper::map);
  }

}
