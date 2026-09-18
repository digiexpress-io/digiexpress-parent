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

import org.apache.commons.lang3.StringUtils;

import io.resys.thena.api.entities.grim.ImmutableGrimRps;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewRps;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.grim.spi.ImmutableGrimBatchMissions;
import io.resys.thena.grim.spi.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;

public class NewRpsBuilder implements ThenaGrimNewObject.NewRps {
  private final GrimCommitBuilder logger;
  private final OffsetDateTime createdAt = OffsetDateTime.now();
  private final ImmutableGrimBatchMissions.Builder batch;
  private final ImmutableGrimRps.Builder rps;
  
  private boolean built;
  
  public NewRpsBuilder(GrimCommitBuilder logger) {
    super();
    this.logger = logger;
    this.batch = ImmutableGrimBatchMissions.builder()
        .tenantId(logger.getTenantId())
        .status(BatchStatus.OK)
        .log("");
    this.rps = ImmutableGrimRps.builder()
        .id(String.valueOf(OidUtils.gen()))
        .createdAt(createdAt);
  }
  @Override
  public void build() {
    this.built = true;
  }
  @Override
  public NewRpsBuilder formName(String formName) {
    this.rps.formName(formName);
    return this;
  }
  
  @Override
  public NewRps workflowName(String workflowName) {
    this.rps.workflowName(workflowName);
    return this;
  }
  @Override
  public NewRps externalId(String externalId) {
    this.rps.externalId(externalId);
    return this;
  }
  @Override
  public NewRps locale(String locale) {
    this.rps.locale(locale);
    return this;
  }
  @Override
  public NewRps formVersion(String formVersion) {
    this.rps.formVersion(formVersion);
    return this;
  }
  @Override
  public NewRps rating(Integer rating) {
    this.rps.rating(rating);
    return this;
  }
  @Override
  public NewRps comment(String comment) {
    this.rps.comment(StringUtils.isBlank(comment) ? null : comment.trim());
    return this;
  }
  
  public ImmutableGrimBatchMissions close() {
    RepoAssert.isTrue(built, () -> "you must call NewRpsBuilder.build() to finalize mission CREATE!");
    
    final var rps = this.rps.build();    
    logger.add(rps);
    
    this.batch.addRps(rps);
    return this.batch.build();
  }
}
