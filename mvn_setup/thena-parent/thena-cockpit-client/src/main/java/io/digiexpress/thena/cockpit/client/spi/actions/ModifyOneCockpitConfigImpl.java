package io.digiexpress.thena.cockpit.client.spi.actions;

/*-
 * #%L
 * thena-cockpit-client
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
import java.util.Optional;
import java.util.function.Consumer;

import io.digiexpress.thena.cockpit.client.api.CockpitCommitActions.ModifyOneCockpitConfig;
import io.digiexpress.thena.cockpit.client.api.CockpitCommitActions.OneCockpitConfigEnvelope;
import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import io.digiexpress.thena.cockpit.client.api.CockpitMergeObject.MergeCockpitConfig;
import io.digiexpress.thena.cockpit.client.api.ImmutableOneCockpitConfigEnvelope;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitDocType;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitCommit;
import io.digiexpress.thena.cockpit.client.spi.commitlog.CockpitCommitBuilder;
import io.digiexpress.thena.cockpit.client.spi.modify.MergeCockpitConfigBuilder;
import io.digiexpress.thena.cockpit.client.spi.queries.CockpitQueryImpl;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import io.digiexpress.thena.cockpit.client.tables.CockpitDbBuilder.PersistenceUnit;
import io.digiexpress.thena.cockpit.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ModifyOneCockpitConfigImpl implements ModifyOneCockpitConfig {

  private final CockpitDb state;
  private final String tenantId;
  
  private String author;
  private String message;
  private String configId;
  private Consumer<MergeCockpitConfig> config;
  
  @Override
  public ModifyOneCockpitConfig commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  
  @Override
  public ModifyOneCockpitConfig commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  
  @Override
  public ModifyOneCockpitConfig cockpitConfigId(String CockpitConfigId) {
    this.configId = RepoAssert.notEmpty(CockpitConfigId, () -> "CockpitConfigId can't be empty!");
    return this;
  }
  
  @Override
  public ModifyOneCockpitConfig modifyCockpitConfig(Consumer<MergeCockpitConfig> modifyCockpitConfig) {
    RepoAssert.notNull(modifyCockpitConfig, () -> "modifyCockpitConfig can't be empty!");
    config = modifyCockpitConfig;
    return this;
  }
  
  @Override
  public Uni<OneCockpitConfigEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(config, () -> "modifyCockpitConfig can't be empty!");
    RepoAssert.notEmpty(configId, () -> "configId can't be empty!");
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withTransaction(scope, this::doInTx);
  }

  private Uni<OneCockpitConfigEnvelope> doInTx(CockpitDb tx) {
    return createRequest(tx)
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(ModifyOneCockpitConfigException.class).recoverWithItem(ex -> {
          final ModifyOneCockpitConfigException error = (ModifyOneCockpitConfigException) ex;          
          return ImmutableOneCockpitConfigEnvelope.builder()
            .repoId(tenantId)
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'").append(" is rejected.")
                  .append(System.lineSeparator())
                  .append("Message: ").append(error.getMessage())
                  .toString())
                .exception(error)
                .build())
            .status(CommitResultStatus.ERROR)
          .build();
        });
  }

  private Uni<OneCockpitConfigEnvelope> createResponse(CockpitDb tx, PersistenceUnit request) {
    // Merge requests
    final var start = ImmutablePersistenceUnit.builder()
        .tenantId(tenantId)
        .log("")
        .status(BatchStatus.OK)
        .from(request);
    
    // Patch all in current TX
    return tx.builder().from(start.build()).persist().onItem().transformToUni(rsp -> {
      
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new ModifyOneCockpitConfigException("Failed to modify cockpit config!", rsp);
      }

      return CockpitQueryImpl.of(tx)
          .addCockpitId(this.configId)
          .excludeDocs(CockpitDocType.CONFIG_COMMIT)
          .findAll().collect().asList()
          .onItem().transform(container -> {
            final var item = container.iterator().next();
            final OneCockpitConfigEnvelope env = ImmutableOneCockpitConfigEnvelope.builder()
              .repoId(tenantId)
              .cockpitConfig(item)
              .status(BatchStatus.mapStatus(rsp.getStatus()))
              .build();
            return env;
          })
          .onItem().call(env -> CreateOneCockpitConfigImpl.createIfNotExistsTenants(env.getCockpitConfig(), state));
            
    });
  }
  
  private Uni<PersistenceUnit> createRequest(CockpitDb tx) {
    return CockpitQueryImpl.of(tx)
      .addCockpitId(this.configId)
      .lockForUpdate()
      .excludeDocs(CockpitDocType.CONFIG_COMMIT)
      .findAll().collect().asList().onItem()
      .transform(container -> createRequest(tx, container));
  }
  
  private ImmutablePersistenceUnit createRequest(CockpitDb tx, List<CockpitContainer> env) {
    RepoAssert.isTrue(env.size() == 1, () -> "Cockpit config container must be grouped by configs, one config per container!");
    
    final var config = env.get(0).getConfig();
    final var configId = config.getId();
    
    final var start = ImmutablePersistenceUnit.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    
    ImmutablePersistenceUnit next = start;    
    final var logger = new CockpitCommitBuilder(tenantId, 
        ImmutableCockpitCommit.builder()
          .id(OidUtils.genUUID())
          .commitAuthor(author)
          .commitMessage(message)
          .createdAt(createdAt)
          .parentId(Optional.ofNullable(config.getUpdatedTreeCommitId()).orElse(config.getCommitId()))
          .build()
    );
    
    final var mergeConfig = new MergeCockpitConfigBuilder(env.get(0), logger);
    this.config.accept(mergeConfig);
    final var created = mergeConfig.close();
    
    next = ImmutablePersistenceUnit.builder()
        .from(start)
        .from(created)
        .from(logger.withConfigId(configId).close())
        .build();
    return next;
  }
  
  public static class ModifyOneCockpitConfigException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final PersistenceUnit batch;
    
    public ModifyOneCockpitConfigException(String message, PersistenceUnit batch) {
      super(message);
      this.batch = batch;
    }
    
    public PersistenceUnit getBatch() {
      return batch;
    }
  }
}