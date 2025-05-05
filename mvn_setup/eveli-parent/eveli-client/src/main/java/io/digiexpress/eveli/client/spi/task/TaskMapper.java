package io.digiexpress.eveli.client.spi.task;

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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;

import org.apache.commons.codec.binary.StringUtils;

import io.digiexpress.eveli.client.api.ImmutableTask;
import io.digiexpress.eveli.client.api.ImmutableTaskComment;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;

public class TaskMapper {
  public static final String ASSIGNMENT_TYPE_TASK_USER = GrimAssignment.ASSIGNMENT_TYPE_USER;
  public static final String ASSIGNMENT_TYPE_TASK_ROLE = "task_role";
  
  public static final String LABEL_TYPE_KEYWORD = "keyword";
  public static final String LABEL_TYPE_FEATURES = "features";
  
  public static final String LINK_TYPE_CLIENT_LOCALE = "client_locale";
  public static final String LINK_TYPE_ADDITIONAL_INFO = "additional_info";
  
  public static final String VIEWER_WORKER = "WORKER";
  public static final String VIEWER_CUSTOMER = "CUSTOMER";
  
  public static final String COMMENT_EXTERNAL = "EXTERNAL";
  public static final String COMMENT_INTERNAL = "INTERNAL";
  public static final Duration atMost = Duration.ofMinutes(5);
  
  public static ZonedDateTime toZoned(OffsetDateTime input) {
    if(input == null) {
      return null;
    }
    return input.toZonedDateTime();
  }
  

  public static TaskClient.TaskComment map(GrimRemark remark) {
    return ImmutableTaskComment.builder()
        .id(remark.getId())
        .version(remark.getCommitId())
        .created(TaskMapper.toZoned(remark.getTransitives().getCreatedAt()))
        .commentText(remark.getRemarkText())
        .userName(remark.getTransitives().getCreatedBy())
        .replyToId(remark.getParentId()) // probably bad idea, lazy relations
        .taskId(remark.getMissionId()) // probably bad idea, lazy relations
        .external(COMMENT_EXTERNAL.equals(remark.getRemarkType()))
        .source(StringUtils.equals(TaskClient.TaskCommentSource.FRONTDESK.name(), remark.getRemarkSource()) ? TaskCommentSource.FRONTDESK : TaskCommentSource.PORTAL)
        .build();
  }
  

  public static TaskClient.Task map(GrimMissionContainer cont) {
    return map(
        cont.getMission(), 
        cont.getAssignments().values(), 
        cont.getRemarks().values(),
        cont.getLinks().values(),
        cont.getMissionLabels().values()
        );
  }
  
  public static TaskClient.Task map(
      GrimMission commited, 
      Collection<GrimAssignment> assignments, 
      Collection<GrimRemark> remarks,
      Collection<GrimMissionLink> links,
      Collection<GrimMissionLabel> labels) {

    final var assignee = assignments.stream()
      .filter(e -> TaskMapper.ASSIGNMENT_TYPE_TASK_USER.equals(e.getAssignmentType()))
      .findFirst();
    
    final var assignedRoles = assignments.stream()
        .filter(e -> TaskMapper.ASSIGNMENT_TYPE_TASK_ROLE.equals(e.getAssignmentType()))
        .map(e -> e.getAssignee())
        .toList();


    final var keywords = labels.stream()
        .filter(e -> TaskMapper.LABEL_TYPE_KEYWORD.equals(e.getLabelType()))
        .map(e -> e.getLabelValue())
        .toList();

    final var features = labels.stream()
        .filter(e -> TaskMapper.LABEL_TYPE_FEATURES.equals(e.getLabelType()))
        .map(e -> e.getLabelValue())
        .toList();    
    
    final var clientLocale = links.stream()
        .filter(e -> TaskMapper.LINK_TYPE_CLIENT_LOCALE.equals(e.getLinkType()))
        .map(e -> e.getLinkValue())
        .findFirst();

    final var additionalInfo = links.stream()
        .filter(e -> TaskMapper.LINK_TYPE_ADDITIONAL_INFO.equals(e.getLinkType()))
        .map(e -> e.getLinkValue())
        .findFirst();
    
    /* JPA version
    
    return ImmutableTask.builder()
        .version(task.getVersion())
        .assignedUser(task.getAssignedUser())
        .assignedUserEmail(task.getAssignedUserEmail())
        .clientIdentificator(task.getClientIdentificator())
        .completed(task.getCompleted())
        .created(task.getCreated())
        .description(task.getDescription())
        .dueDate(task.getDueDate())
        .id(task.getId())
        .questionnaireId(task.getQuestionnanireId())
        .priority(task.getPriority())
        .status(task.getStatus())
        .subject(task.getSubject())
        .taskRef(task.getTaskRef())
        .updated(task.getUpdated())
        .updaterId(task.getUpdaterId())
        .build();*/
    
    final var task = ImmutableTask.builder()
      .version(commited.getCommitId())
      .clientIdentificator(commited.getReporterId())
      .clientLanguage(clientLocale.orElse(null))
      .description(commited.getDescription())
      .dueDate(commited.getDueDate())
      .id(commited.getId())

      .completed(TaskMapper.toZoned(commited.getCompletedAt()))
      .created(TaskMapper.toZoned(commited.getTransitives().getCreatedAt()))
      .questionnaireId(commited.getQuestionnaireId())
      .priority(TaskPriority.valueOf(commited.getMissionPriority()))
      .status(TaskStatus.valueOf(commited.getMissionStatus()))
      .subject(commited.getTitle())
      .taskRef(commited.getRefId())
      
      .updated(TaskMapper.toZoned(commited.getTransitives().getTreeUpdatedAt()))
      .updaterId(commited.getTransitives().getTreeUpdatedBy())
      
      .assignedUser(assignee.map(e -> e.getAssignee()).orElse(null))
      .assignedUserEmail(assignee.map(e -> e.getAssigneeContact()).orElse(null))
      .assignedRoles(assignedRoles)

      .comments(remarks.stream().map(TaskMapper::map).toList())
      
      .keyWords(keywords)
      .features(features)
      .additionalInfo(additionalInfo.orElse(null))
      
      .build();
    
    return task;
  }
}
