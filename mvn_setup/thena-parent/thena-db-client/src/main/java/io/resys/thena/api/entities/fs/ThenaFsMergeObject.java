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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.resys.thena.api.entities.fs.FsDirent.DirentType;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentAssignment;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentLabel;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentLink;
import io.resys.thena.api.entities.fs.ThenaFsNewObject.NewDirentRemark;
import io.vertx.core.json.JsonObject;

// Generic interfaces for create/update/delete operations 
public interface ThenaFsMergeObject {  
  
  interface MergeDirent {
    MergeDirent onCurrentState(Consumer<FsDirentContainer> handleCurrentState);
    FsDirentContainer getCurrentState();
    
    MergeDirent archivedAt(@Nullable OffsetDateTime archivedAt);
    MergeDirent externalId(String externalId);
    MergeDirent direntParentId(@Nullable String direntParentId);    
    MergeDirent direntType(DirentType direntType);
    MergeDirent direntName(String direntName);
    MergeDirent direntDescription(String direntDescription);
    MergeDirent direntUserType(@Nullable String direntUserType);
    
    
    // nested builders
    <T> MergeDirent setAllAssignees(String assigneeType, List<T> replacments, Function<T, Consumer<NewDirentAssignment>> assignment);
    <T> MergeDirent setAllLabels(String labelType, List<T> replacments, Function<T, Consumer<NewDirentLabel>> label);
    <T> MergeDirent setAllLinks(String linkType, List<T> replacments, Function<T, Consumer<NewDirentLink>> link);
    
    MergeDirent addAssignees(Consumer<NewDirentAssignment> assignment);
    MergeDirent addLabels(Consumer<NewDirentLabel> label);
    MergeDirent addLink(Consumer<NewDirentLink> link);
    MergeDirent addRemark(Consumer<NewDirentRemark> remark);
    
    MergeDirent modifyLink(String linkId, Consumer<MergeDirentLink> goal);
    MergeDirent modifyRemark(String remarkId, Consumer<MergeDirentRemark> objective);
    
    MergeDirent removeRemark(String remarkId);

    void build();
  }
  
  // support interface inside of callback
  interface MergeDirentRemark {
    MergeDirentRemark parentId(@Nullable String parentId);
    MergeDirentRemark remarkText(String remarkText);
    MergeDirentRemark remarkStatus(@Nullable String remarkStatus);
    MergeDirentRemark reporterId(String reporterId);
    void build(); 
  }
  interface MergeDirentLink {
    MergeDirentLink linkType(String linkType);
    MergeDirentLink linkValue(String linkValue);
    MergeDirentLink linkBody(@Nullable JsonObject linkBody);
    void build();
  }  
}
