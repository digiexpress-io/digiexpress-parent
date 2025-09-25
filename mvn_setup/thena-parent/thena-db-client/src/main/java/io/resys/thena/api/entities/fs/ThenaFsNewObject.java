package io.resys.thena.api.entities.fs;

/*-
 * #%L
 * thena-db-client
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

import java.util.function.Consumer;

import jakarta.annotation.Nullable;

import io.resys.thena.api.entities.fs.FsDirent.DirentType;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.vertx.core.json.JsonObject;

// Generic interfaces for create/update/delete operations 
public interface ThenaFsNewObject {


  interface NewDirent {
    NewDirent externalId(String externalId);
    NewDirent direntParentId(@Nullable String direntParentId);    
    NewDirent direntType(DirentType direntType);
    NewDirent direntName(String direntName);
    NewDirent direntDescription(String direntDescription);
    NewDirent direntUserType(@Nullable String direntUserType);
    @Nullable String getDirentUserType(); // user defined optional type
    
    
    // nested builders    
    NewDirent addAssignees(Consumer<NewDirentAssignment> assignment);
    NewDirent addLabels(Consumer<NewDirentLabel> label);
    NewDirent addLink(Consumer<NewDirentLink> link);
    NewDirent addRemark(Consumer<NewDirentRemark> remark);

    NewDirent onNewState(Consumer<FsDirentContainer> handleNewState);
    void build();
  }
  
  // support interface inside of callback
  interface NewDirentAssignment {
    NewDirentAssignment assignee(String assignee);
    NewDirentAssignment assignmentType(String assignmentType);
    NewDirentAssignment assigneeContact(@Nullable String assignmeeContact);
    void build();
  }  
  // support interface inside of callback
  interface NewDirentLabel {
    NewDirentLabel labelType(String labelType);
    NewDirentLabel labelValue(String labelValue);
    NewDirentLabel labelBody(@Nullable JsonObject labelBody);
    void build();
  }
  // support interface inside of callback
  interface NewDirentLink {
    NewDirentLink linkType(String linkType);
    NewDirentLink linkValue(String linkValue);
    NewDirentLink linkBody(@Nullable JsonObject linkBody);
    void build();
  }
  // support interface inside of callback
  interface NewDirentRemark {
    NewDirentRemark parentId(@Nullable String parentId);
    NewDirentRemark remarkText(String remarkText);
    NewDirentRemark remarkStatus(@Nullable String remarkStatus);
    NewDirentRemark remarkSource(@Nullable String remarkSource);
    NewDirentRemark remarkType(@Nullable String remarkType);
    NewDirentRemark reporterId(String reporterId);
    String build();  // returns generated remark id
  }
  

}
