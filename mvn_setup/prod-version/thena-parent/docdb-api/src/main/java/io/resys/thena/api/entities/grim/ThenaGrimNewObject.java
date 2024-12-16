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
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.vertx.core.json.JsonObject;

// Generic interfaces for create/update/delete operations 
public interface ThenaGrimNewObject {


  interface NewMission {
    NewMission title(String title);
    NewMission description(String description);
    
    NewMission parentId(@Nullable String parentId);
    NewMission reporterId(@Nullable String reporterId);    
    
    NewMission status(@Nullable String status);
    NewMission startDate(@Nullable LocalDate startDate);
    NewMission dueDate(@Nullable LocalDate dueDate);
    NewMission priority(@Nullable String priority);
    
    // nested builders
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
  interface MergeLink {
    MergeLink linkType(String linkType);
    MergeLink linkValue(String linkValue);
    MergeLink linkBody(@Nullable JsonObject linkBody);
    void build();
  }  
  // support interface inside of callback
  interface NewObjective {
    NewObjective title(String title);
    NewObjective description(String description);
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
    NewRemark reporterId(String reporterId);
    NewRemark addAssignees(Consumer<NewAssignment> assignment);
    void build(); 
  }
}
