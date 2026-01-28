package io.resys.thena.api.entities.grim;

/*-
 * #%L
 * thena-docdb-api
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;

import jakarta.annotation.Nullable;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.vertx.core.json.JsonObject;

// Generic interfaces for create/update/delete operations 
public interface ThenaGrimNewObject {


  interface NewMission {
    NewMission title(String title);
    NewMission description(String description);
    NewMission questionnaireId(String questionnaireId);
    
    NewMission parentId(@Nullable String parentId);
    NewMission reporterId(@Nullable String reporterId);    
    
    NewMission status(@Nullable String status);
    NewMission startDate(@Nullable LocalDate startDate);
    NewMission dueDate(@Nullable LocalDate dueDate);
    NewMission priority(@Nullable String priority);
    
    // nested builders
    NewMission addViewer(Consumer<NewMissionCommitViewer> viewer);
    
    NewMission addAssignees(Consumer<NewAssignment> assignment);
    NewMission addLabels(Consumer<NewLabel> label);
    NewMission addLink(Consumer<NewLink> link);
    NewMission addRemark(Consumer<NewRemark> remark);
    NewMission addCommands(List<JsonObject> commandToAppend);    
    NewMission addObjective(Consumer<NewObjective> goal);
    NewMission onNewState(Consumer<GrimMissionContainer> handleNewState);
    void build();
  }
  
  // support interface inside of callback
  interface NewAssignment {
    NewAssignment assignee(String assignee);
    NewAssignment assignmentType(String assignmentType);
    NewAssignment assigneeContact(@Nullable String assignmeeContact);
    void build();
  }  
  // support interface inside of callback
  interface NewLabel {
    NewLabel labelType(String labelType);
    NewLabel labelValue(String labelValue);
    NewLabel labelBody(@Nullable JsonObject labelBody);
    void build();
  }
  // support interface inside of callback
  interface NewLink {
    NewLink linkType(String linkType);
    NewLink linkValue(String linkValue);
    NewLink linkBody(@Nullable JsonObject linkBody);
    void build();
  }
  
  interface NewProcess {
    NewProcess workflowName(String name);
    
    NewProcess questionnaireId(@Nullable String questionnaire);
    NewProcess userId(@Nullable String userId);
    NewProcess expiresInSeconds(@Nullable Long expires_in_seconds);
    NewProcess expiresAt(@Nullable OffsetDateTime expiresAt);

    NewProcess anon(@Nullable Boolean anon);
    NewProcess articleName(@Nullable String articleName);
    NewProcess parentArticleName(@Nullable String parentArticleName);
    NewProcess formName(@Nullable String formName);
    NewProcess flowName(@Nullable String flowName);
    NewProcess missionId(@Nullable String missionId);
    NewProcess cockpitId(@Nullable String cockpitId);
    

    NewProcess formTagName(@Nullable String formTagName);
    NewProcess stencilTagName(@Nullable String stencilTagName);
    NewProcess wrenchTagName(@Nullable String wrenchTagName);
    
    NewProcess status(String status);
    NewProcess type(GrimProcessType type);
    
    void build();
  }
  
  // support interface inside of callback
  interface NewObjective {
    NewObjective title(String title);
    NewObjective description(String description);
    NewObjective type(@Nullable String type);
    NewObjective locale(@Nullable String locale);
    NewObjective externalId(@Nullable String externalId);
    NewObjective processId(@Nullable String processId);
    NewObjective questionnaireId(@Nullable String questionnaireId);
    NewObjective status(@Nullable String status);
    NewObjective startDate(@Nullable LocalDate startDate);
    NewObjective dueDate(@Nullable LocalDate dueDate);
    
    NewObjective addGoal(Consumer<NewGoal> newGoal);
    NewObjective addAssignees(Consumer<NewAssignment> assignment);
    void build();    
  }    
  // support interface inside of callback
  interface NewGoal {
    NewGoal title(String title);
    NewGoal description(String description);
    NewGoal status(@Nullable String status);
    NewGoal startDate(@Nullable LocalDate startDate);
    NewGoal dueDate(@Nullable LocalDate dueDate);
    
    NewGoal addAssignees(Consumer<NewAssignment> assignment);
    void build(); 
  }
  // support interface inside of callback
  interface NewRemark {
    NewRemark parentId(@Nullable String parentId);
    NewRemark remarkText(String remarkText);
    NewRemark remarkStatus(@Nullable String remarkStatus);
    NewRemark remarkSource(@Nullable String remarkSource);
    NewRemark remarkType(@Nullable String remarkType);
    NewRemark reporterId(String reporterId);
    NewRemark addAssignees(Consumer<NewAssignment> assignment);
    String build();  // returns generated remark id
  }
  
  // support interface inside of callback
  interface NewMissionCommitViewer {
    NewMissionCommitViewer userId(String userId);
    NewMissionCommitViewer usedFor(String usedFor);
    NewMissionCommitViewer commitId(String commitId);
    NewMissionCommitViewer currentTxCommit(); // ongoing tx commit
    NewMissionCommitViewer currentTreeCommit(); // whatever is last tree updated commit 
    NewMissionCommitViewer skipViewer(); // cancel out of viewer, skips the object 
    String getCurrentTreeCommit();
    void build(); 
  }
}
