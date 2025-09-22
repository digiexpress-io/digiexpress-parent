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
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.StringUtils;

import io.digiexpress.eveli.client.api.ImmutableProcessInstance;
import io.digiexpress.eveli.client.api.ImmutableTask;
import io.digiexpress.eveli.client.api.ImmutableTaskComment;
import io.digiexpress.eveli.client.api.ProcessClient;
import io.digiexpress.eveli.client.api.ProcessClient.ProcessInstance;
import io.digiexpress.eveli.client.api.TaskClient;
import io.digiexpress.eveli.client.api.TaskClient.TaskCommentSource;
import io.digiexpress.eveli.client.api.TaskClient.TaskPriority;
import io.digiexpress.eveli.client.api.TaskClient.TaskStatus;
import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.grim.api.GrimCommitActions.OneMissionEnvelope;

public class TaskMapper {
  public static final String ASSIGNMENT_TYPE_TASK_USER = GrimAssignment.ASSIGNMENT_TYPE_USER;
  public static final String ASSIGNMENT_TYPE_TASK_ROLE = GrimAssignment.ASSIGNMENT_TYPE_TASK_ROLE;
  
  public static final String LABEL_TYPE_KEYWORD = "keyword";
  public static final String LABEL_TYPE_FEATURES = "features";
  
  public static final String LINK_TYPE_DOC_PROPS = "doc_props";
  public static final String LINK_TYPE_CLIENT_LOCALE = "client_locale";
  public static final String LINK_TYPE_ADDITIONAL_INFO = "additional_info";
  public static final String LINK_TYPE_TRANSFERRED_ID = "transferred_id";
  
  
  public static final String VIEWER_WORKER = "WORKER";
  public static final String VIEWER_CUSTOMER = "CUSTOMER";
  
  public static final String COMMENT_EXTERNAL = "EXTERNAL";
  public static final String COMMENT_INTERNAL = "INTERNAL";
  
  public static final String TASK_FEATURE_FEEDBACK = "feedback";
  public static final String TASK_FEATURE_TRANSFER = "transfer";
  public static final String TASK_FEATURE_ANON = "anon";
  
  
  
  
  public final static String DEFAULT_CLIENT_LANG = "fi";
  
  
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
        cont.getMissionLabels().values(),
        cont.getObjectives().values()
        );
  }
  
  
  public static TaskClient.Task map(OneMissionEnvelope commited) {
    final var task = TaskMapper.map(
        commited.getMission(), 
        commited.getAssignments(), 
        commited.getRemarks(), 
        commited.getLinks(),
        commited.getLabels(),
        commited.getObjectives());
    return task;
  }
  
  public static TaskClient.Task map(
      GrimMission commited, 
      Collection<GrimAssignment> assignments, 
      Collection<GrimRemark> remarks,
      Collection<GrimMissionLink> links,
      Collection<GrimMissionLabel> labels,
      Collection<GrimObjective> objectives) {

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
    
    
    final var transferredId = links.stream()
        .filter(e -> TaskMapper.LINK_TYPE_TRANSFERRED_ID.equals(e.getLinkType()))
        .map(e -> e.getLinkValue())
        .findFirst();
    
    
    final var transferredProps = links.stream()
        .filter(e -> TaskMapper.LINK_TYPE_TRANSFERRED_ID.equals(e.getLinkType()))
        .map(e -> e.getLinkBody())
        .findFirst();

    final var additionalInfo = links.stream()
        .filter(e -> TaskMapper.LINK_TYPE_ADDITIONAL_INFO.equals(e.getLinkType()))
        .map(e -> e.getLinkValue())
        .findFirst();

    final var docProps = links.stream()
        .filter(e -> TaskMapper.LINK_TYPE_DOC_PROPS.equals(e.getLinkType()))
        .map(e -> e.getLinkBody().getMap().entrySet().stream().collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue().toString())))
        .findFirst();
    
    
    
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
      
      .transferredId(transferredId.orElse(null))
      .transferredProps(transferredProps.orElse(null))
      
      .documentProperties(docProps.orElse(Collections.emptyMap()))
      
      .keyWords(keywords)
      .features(features)
      .additionalInfo(additionalInfo.orElse(null))
      
      .build();
    
    return task;
  }
  
  public static ProcessInstance map(GrimProcess entity) {
    return ImmutableProcessInstance.builder()
        .id(Long.parseLong(entity.getId()))
        .status(entity.getStatus() == null ? null : ProcessClient.ProcessStatus.valueOf(entity.getStatus()))
        .questionnaireId(entity.getQuestionnaireId())
        .taskId(entity.getMissionId())
        .taskRef(entity.getMissionRef())
        .userId(entity.getUserId())
        .created(entity.getCreated())
        .updated(entity.getUpdated())
        
        .workflowName(entity.getWorkflowName())
        .articleName(entity.getArticleName())
        .parentArticleName(entity.getParentArticleName())
        .anon(Boolean.TRUE.equals(entity.getAnon()))
        .formName(entity.getFormName())
        .flowName(entity.getFlowName())
        
        .formTagName(entity.getFormTagName())
        .stencilTagName(entity.getStencilTagName())
        .wrenchTagName(entity.getWrenchTagName())
        
        .expiresInSeconds(entity.getExpiresInSeconds())
        .expiresAt(entity.getExpiresAt())
        .build();
  }
}
