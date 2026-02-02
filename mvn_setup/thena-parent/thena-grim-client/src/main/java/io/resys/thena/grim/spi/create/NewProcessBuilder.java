package io.resys.thena.grim.spi.create;

/*-
 * #%L
 * thena-grim-client
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

import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessStatus;
import io.resys.thena.api.entities.grim.GrimProcess.GrimProcessType;
import io.resys.thena.api.entities.grim.ImmutableGrimProcess;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewProcess;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.grim.spi.ImmutableGrimBatchMissions;
import io.resys.thena.grim.spi.commitlog.GrimCommitBuilder;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;

public class NewProcessBuilder implements ThenaGrimNewObject.NewProcess {
  private final GrimCommitBuilder logger;
  private final OffsetDateTime createdAt = OffsetDateTime.now();
  private final ImmutableGrimBatchMissions.Builder batch;
  private final ImmutableGrimProcess.Builder process;
  
  private boolean built;
  
  public NewProcessBuilder(GrimCommitBuilder logger, String missionId, long id) {
    super();
    this.logger = logger;
    this.batch = ImmutableGrimBatchMissions.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
    this.process = ImmutableGrimProcess.builder()
        .id(String.valueOf(id))
        .created(createdAt)
        .updated(createdAt)
        .missionId(missionId)
        .missionRef(null)
        ;
  }
  @Override
  public void build() {
    this.built = true;
  }
  @Override
  public NewProcess formBody(JsonObject formBody) {
    this.process.formBody(formBody);
    return this;
  }
  @Override
  public NewProcess workflowName(String workflowName) {
    this.process.workflowName(workflowName);
    return this;
  }
  @Override
  public NewProcess questionnaireId(String questionnaire) {
    this.process.questionnaireId(questionnaire);
    return this;
  }
  @Override
  public NewProcess userId(String userId) {
    this.process.userId(userId);
    return this;
  }
  @Override
  public NewProcess expiresInSeconds(Long expires_in_seconds) {
    this.process.expiresInSeconds(expires_in_seconds);
    return this;
  }
  @Override
  public NewProcess expiresAt(OffsetDateTime expiresAt) {
    this.process.expiresAt(expiresAt);
    return this;
  }
  @Override
  public NewProcess anon(Boolean anon) {
    this.process.anon(anon);
    return this;
  }
  @Override
  public NewProcess articleName(String articleName) {
    this.process.articleName(articleName);
    return this;
  }
  @Override
  public NewProcess parentArticleName(String parentArticleName) {
    this.process.parentArticleName(parentArticleName);
    return this;
  }
  @Override
  public NewProcess formName(String formName) {
    this.process.formName(formName);
    return this;
  }
  @Override
  public NewProcess flowName(String flowName) {
    this.process.flowName(flowName);
    return this;
  }
  @Override
  public NewProcess formTagName(String formTagName) {
    this.process.formTagName(formTagName);
    return this;
  }
  @Override
  public NewProcess stencilTagName(String stencilTagName) {
    this.process.stencilTagName(stencilTagName);
    return this;
  }
  @Override
  public NewProcess wrenchTagName(String wrenchTagName) {
    this.process.wrenchTagName(wrenchTagName);
    return this;
  }
  @Override
  public NewProcess missionId(String missionId) {
    this.process.missionId(missionId);
    return this;
  }
  @Override
  public NewProcess cockpitId(String cockpitId) {
    this.process.cockpitId(cockpitId);
    return this;
  }
  @Override
  public NewProcess status(GrimProcessStatus status) {
    this.process.status(status);
    return this;
  }
  @Override
  public NewProcess type(GrimProcessType type) {
    this.process.type(type);
    return this;
  }
  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call ObjectiveChanges.build() to finalize mission CREATE or UPDATE!");
    
    final var objective = this.process.build();    
    logger.add(objective);
    
    this.batch.addProcs(objective);
    return this.batch.build();
  }


}
