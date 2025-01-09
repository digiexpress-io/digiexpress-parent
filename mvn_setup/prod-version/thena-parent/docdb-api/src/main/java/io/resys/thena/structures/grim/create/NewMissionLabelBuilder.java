package io.resys.thena.structures.grim.create;

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

import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLabel;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewLabel;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimOneOfRelations;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;

public class NewMissionLabelBuilder implements ThenaGrimNewObject.NewLabel {
  private final GrimCommitBuilder logger;
  private final String missionId;
  private final Map<String, GrimMissionLabel> all_missionLabels;

  private boolean built;
  private String labelValue;
  private String labelType;
  private JsonObject labelBody;
  private GrimOneOfRelations rels;
  
  public NewMissionLabelBuilder(
      GrimCommitBuilder logger, 
      String missionId, 
      GrimOneOfRelations relation, 
      Map<String, GrimMissionLabel> all_missionLabels) {
    
    super();
    this.logger = logger;
    this.missionId = missionId;
    this.rels = relation;
    this.all_missionLabels = all_missionLabels;
  }
  
  @Override
  public void build() {
    this.built = true;
  }
  @Override
  public NewLabel labelValue(String labelValue) {
    RepoAssert.notEmpty(labelValue, () -> "labelValue must be defined!");
    this.labelValue = labelValue;
    return this;
  }
  @Override
  public NewLabel labelType(String labelType) {
    this.labelType = labelType;
    return this;
  }
  @Override
  public NewLabel labelBody(JsonObject labelBody) {
    this.labelBody = labelBody;
    return this;
  }
  public ImmutableGrimMissionLabel close() {
    RepoAssert.isTrue(built, () -> "you must call LabelChanges.build() to finalize mission CREATE or UPDATE!");
    RepoAssert.notEmpty(labelValue, () -> "labelValue must be defined!");
    RepoAssert.notEmpty(labelType, () -> "labelType must be defined!");
    
    
    
    final ImmutableGrimMissionLabel built = ImmutableGrimMissionLabel.builder()
        .id(OidUtils.gen())
        .commitId(logger.getCommitId())
        .missionId(missionId)
        .labelValue(labelValue)
        .labelType(labelType)
        .labelBody(labelBody)
        .relation(rels)
        .build();
    
    RepoAssert.isTrue(
        this.all_missionLabels.values().stream()
        .filter(a -> 
          (a.getRelation() == null && rels == null) ||
          (a.getRelation() != null && a.getRelation().equals(rels))
        )
        .filter(a -> a.getLabelType().equals(built.getLabelType()))
        .filter(a -> a.getLabelValue().equals(built.getLabelValue()))
        .count() == 0
        , () -> "can't have duplicate labels!");
    
    logger.add(built);
    return built;
  }
}
