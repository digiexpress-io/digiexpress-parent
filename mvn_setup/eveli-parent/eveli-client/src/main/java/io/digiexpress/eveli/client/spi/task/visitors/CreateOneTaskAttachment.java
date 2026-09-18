package io.digiexpress.eveli.client.spi.task.visitors;

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

import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskAttachment.AttachmentSource;
import io.digiexpress.eveli.client.spi.task.TaskException;
import io.digiexpress.eveli.client.spi.task.TaskMapper;
import io.digiexpress.eveli.client.spi.task.TaskStoreConfig;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.grim.api.GrimClient.GrimStructuredTenant;
import io.resys.thena.grim.api.GrimCommitActions.ModifyOneMission;
import io.resys.thena.grim.api.GrimCommitActions.OneMissionEnvelope;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneTaskAttachment implements TaskStoreConfig.MergeTaskVisitor<TaskClient.Task> {
  private final String userId;
  private final String taskId;
  private final TaskClient.TaskAttachment attachment;
  
  private void createTaskAttachment(TaskClient.TaskAttachment attachment, MergeMission merge) {
    final var usedFor = attachment.getSource() == AttachmentSource.PORTAL_FORM || attachment.getSource() == AttachmentSource.PORTAL_UPLOAD ? TaskMapper.VIEWER_CUSTOMER : TaskMapper.VIEWER_WORKER;
    
    var currentLink = merge.getCurrentState().getLinks().values().stream()
        .filter(l -> TaskMapper.LINK_TYPE_ATTACHMENT.equals(l.getLinkType()))
        .filter(l -> attachment.getName().equals(l.getLinkValue()))
        .findFirst();
    
    if (currentLink.isPresent()) {
      merge.modifyLink(currentLink.get().getId(), link -> {
        link.linkBody(JsonObject.mapFrom(attachment))
        .build();
      });
    }
    else {
      // create new link
      merge.addLink(link -> {
        link
            .linkType(TaskMapper.LINK_TYPE_ATTACHMENT)
            .linkValue(attachment.getName())
            .linkBody(JsonObject.mapFrom(attachment))
            .build();
        
      });
    }
    merge.addViewer(newViewer -> newViewer.userId(userId).usedFor(usedFor).currentTxCommit().build())
    .build();
  }

  @Override
  public ModifyOneMission start(GrimStructuredTenant config, ModifyOneMission builder) {
    builder.missionId(taskId).modifyMission(merge -> createTaskAttachment(attachment, merge));
    return builder
        .commitAuthor(userId)
        .commitMessage("Creating tasks by: " + CreateOneTaskAttachment.class.getSimpleName());
  }

  @Override
  public OneMissionEnvelope visitEnvelope(GrimStructuredTenant config, OneMissionEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope;
    }
    throw TaskException.builder("CREATE_TASK_ATTACHMENT_SAVE_FAIL").add(config, envelope).build(); 
  }

  @Override
  public Uni<TaskClient.Task> end(GrimStructuredTenant config, OneMissionEnvelope commited) {
    final var task = TaskMapper.map(
        commited.getMission(), 
        commited.getAssignments(), 
        commited.getRemarks(),
        commited.getLinks(),
        commited.getLabels(),
        commited.getObjectives());
    return Uni.createFrom().item(task);
  }
}
