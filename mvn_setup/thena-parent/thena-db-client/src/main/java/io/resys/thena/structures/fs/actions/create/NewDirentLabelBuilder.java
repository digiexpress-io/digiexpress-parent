package io.resys.thena.structures.fs.actions.create;

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

import java.util.Map;

import io.resys.thena.api.entities.fs.FsDirentLabel;
import io.resys.thena.api.entities.fs.ImmutableFsDirentLabel;
import io.resys.thena.api.entities.fs.ThenaFsNewObject;
import io.resys.thena.structures.fs.actions.commitlog.FsCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;

public class NewDirentLabelBuilder implements ThenaFsNewObject.NewDirentLabel {
  private final FsCommitBuilder logger;
  private final String direntId;
  private final Map<String, FsDirentLabel> all_direntLabels;

  private boolean built;
  private String labelValue;
  private String labelType;
  private JsonObject labelBody;
  
  public NewDirentLabelBuilder(
      FsCommitBuilder logger, 
      String direntId, 
      Map<String, FsDirentLabel> all_direntLabels) {
    
    super();
    this.logger = logger;
    this.direntId = direntId;
    this.all_direntLabels = all_direntLabels;
  }
  
  @Override
  public void build() {
    this.built = true;
  }
  @Override
  public ThenaFsNewObject.NewDirentLabel labelValue(String labelValue) {
    RepoAssert.notEmpty(labelValue, () -> "labelValue must be defined!");
    this.labelValue = labelValue;
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentLabel labelType(String labelType) {
    this.labelType = labelType;
    return this;
  }
  @Override
  public ThenaFsNewObject.NewDirentLabel labelBody(JsonObject labelBody) {
    this.labelBody = labelBody;
    return this;
  }
  public ImmutableFsDirentLabel close() {
    RepoAssert.isTrue(built, () -> "you must call LabelChanges.build() to finalize dirent CREATE or UPDATE!");
    RepoAssert.notEmpty(labelValue, () -> "labelValue must be defined!");
    RepoAssert.notEmpty(labelType, () -> "labelType must be defined!");
    
    
    
    final ImmutableFsDirentLabel built = ImmutableFsDirentLabel.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .direntId(direntId)
        .labelValue(labelValue)
        .labelType(labelType)
        .labelBody(labelBody)
        .build();
    
    RepoAssert.isTrue(
        this.all_direntLabels.values().stream()
        .filter(a -> a.getLabelType().equals(built.getLabelType()))
        .filter(a -> a.getLabelValue().equals(built.getLabelValue()))
        .count() == 0
        , () -> "can't have duplicate labels!");
    
    logger.add(built);
    return built;
  }
}
