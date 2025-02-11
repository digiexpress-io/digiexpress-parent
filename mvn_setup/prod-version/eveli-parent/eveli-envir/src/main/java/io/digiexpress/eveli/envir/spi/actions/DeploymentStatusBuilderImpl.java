package io.digiexpress.eveli.envir.spi.actions;

/*-
 * #%L
 * eveli-envir
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

import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.DeploymentStatusBuilder;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;



@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class DeploymentStatusBuilderImpl implements DeploymentStatusBuilder {
  private final EveliEnvirStore ctx;
  private final EveliRuntimeCache cache;
  private final DeploymentStatusBuilderLogger logger = new DeploymentStatusBuilderLogger();
  private String userId;
  private String deploymentId;
  
  private Boolean setToDeployed;
  
  @Override
  public DeploymentStatusBuilder deployed() {
    this.setToDeployed = true;
    return this;
  }
  @Override
  public DeploymentStatusBuilder undeployed() {
    this.setToDeployed = false;
    return this;
  }
  @Override
  public Uni<EveliDeployment> build() {
    RepoAssert.notNull(setToDeployed, () -> "deployed/undeployed must be defined!");
    RepoAssert.notEmpty(userId, () -> "userId must be defined!");
    RepoAssert.notEmpty(deploymentId, () -> "deploymentId must be defined!");
    
    return Uni.combine().all().unis(
      new DeploymentQueryImpl(ctx).emptyBranchBody(true).excludeExternal(true).getOneById(deploymentId),
      new DeploymentQueryImpl(ctx).emptyBranchBody(true).excludeExternal(true)
        .status(EveliDeploymentStatus.DEPLOYED)
        .findAll()
    )
    .asTuple().onItem().transformToUni(tuple -> applyUpdate(tuple.getItem1(), tuple.getItem2()))
    .onItem().transform(this::validateUpdateResponse)
    .onItem().transform(this::createResult);
  }
  
  private Uni<ManyDocsEnvelope> applyUpdate(EveliDeployment target, List<EveliDeployment> deployed) {
    final var config = ctx.getConfig();
    
    final var builder = config.getClient().doc(config.getRepoId()).commit().modifyManyDocs()
      .commitMessage("Activating singular deployment")
      .commitAuthor(userId);
    
    // skip on error
    if(target.getStatus() == EveliDeploymentStatus.ERROR) {
      logger.setSkipping(target).error();
      return builder.item()
          .docId(target.getId())
          .docSubStatus(EveliDeploymentStatus.ERROR.name())
          .next()
          .build();
    }
    
    for(final var dep : deployed) {
      logger.setReady(dep);
      builder.item()
        .docId(dep.getId())
        .docSubStatus(EveliDeploymentStatus.READY.name())
        .next();
    }
    
    final EveliDeploymentStatus targetStatus;
    if(this.setToDeployed) {
      targetStatus = EveliDeploymentStatus.DEPLOYED;
      logger.setDeployed(target);
    } else {
      targetStatus = EveliDeploymentStatus.READY;
      logger.setReady(target);
    }
    return builder.item()
      .docId(target.getId())
      .docSubStatus(targetStatus.name())
      .next()
      .build();
  }
  
  public ManyDocsEnvelope validateUpdateResponse(ManyDocsEnvelope envelope) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      logger.error();
      final var config = ctx.getConfig();
      throw DocStoreException.builder("DEPLOYMENT_UPDATE_FAILED")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(JsonObject.of("id", deploymentId).encode()))
        .build();
    }
    
    logger.invalidateCache(cache.getDeployment()).info();
    
    cache.invalidateId();
    return envelope;
  }

  private EveliDeployment createResult(ManyDocsEnvelope envelope) {
    final var dep = envelope.getDoc().stream()
        .filter(doc -> doc.getId().equals(deploymentId)).findFirst()
        .orElseThrow(() -> {
          final var config = ctx.getConfig();
          throw DocStoreException.builder("DEPLOYMENT_UPDATED_BUT_NOT_IN_RESULT")
          .add(config, envelope)
          .add((callback) -> callback.addArgs(JsonObject.of("id", deploymentId).encode()))
          .build();
        });
    return EveliEnvirStore.map(dep, Optional.empty());
  }
}
