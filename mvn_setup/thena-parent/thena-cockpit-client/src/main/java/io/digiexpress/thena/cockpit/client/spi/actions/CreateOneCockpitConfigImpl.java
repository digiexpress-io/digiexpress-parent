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
import java.util.function.Consumer;

import io.resys.thena.api.envelope.BatchStatus;
import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.digiexpress.thena.cockpit.client.api.CockpitCommitActions.CreateOneCockpitConfig;
import io.digiexpress.thena.cockpit.client.api.CockpitCommitActions.OneCockpitConfigEnvelope;
import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import io.digiexpress.thena.cockpit.client.api.CockpitNewObject.NewCockpitConfig;
import io.digiexpress.thena.cockpit.client.api.ImmutableCockpitContainer;
import io.digiexpress.thena.cockpit.client.api.ImmutableOneCockpitConfigEnvelope;
import io.digiexpress.thena.cockpit.client.api.entities.ImmutableCockpitCommit;
import io.digiexpress.thena.cockpit.client.spi.commitlog.CockpitCommitBuilder;
import io.digiexpress.thena.cockpit.client.spi.create.NewCockpitConfigBuilder;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import io.digiexpress.thena.cockpit.client.tables.CockpitDbBuilder.PersistenceUnit;
import io.digiexpress.thena.cockpit.client.tables.ImmutablePersistenceUnit;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateOneCockpitConfigImpl implements CreateOneCockpitConfig {

  private final CockpitDb state;
  private final String tenantId;
  
  private String author;
  private String message;
  private Consumer<NewCockpitConfig> config;
  private Consumer<CockpitContainer> handleNewState;
  
  @Override
  public CreateOneCockpitConfig commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  
  @Override
  public CreateOneCockpitConfig commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  
  @Override
  public CreateOneCockpitConfig cockpitConfig(Consumer<NewCockpitConfig> addCockpitConfig) {
    RepoAssert.notNull(addCockpitConfig, () -> "addCockpitConfig can't be empty!");
    config = addCockpitConfig;
    return this;
  }

  @Override
  public Uni<OneCockpitConfigEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(config, () -> "config can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withTransaction(scope, this::doInTx);
  }
  
  @Override
  public CreateOneCockpitConfig onNewCockpitConfig(Consumer<CockpitContainer> handleNewState) {
    this.handleNewState = handleNewState;
    return this;
  }

  private Uni<OneCockpitConfigEnvelope> doInTx(CockpitDb tx) {
    return createRequest(tx)
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(CreateOneCockpitConfigException.class).recoverWithItem(ex -> {
          final CreateOneCockpitConfigException error = (CreateOneCockpitConfigException) ex;          
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
    return tx.builder().from(request).persist().onItem().transform(rsp -> {
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new CreateOneCockpitConfigException("Failed to create cockpit config!", rsp);
      }
      
      final OneCockpitConfigEnvelope result = ImmutableOneCockpitConfigEnvelope.builder()
          .repoId(tenantId)
          .cockpitConfig(ImmutableCockpitContainer.builder()
            .config(rsp.getCockpitConfigInserts().iterator().next())
            .commits(rsp.getCockpitCommitInserts())
            .commitTrees(rsp.getCockpitCommitTreeInserts())
            .props(rsp.getCockpitConfigPropsInserts())
            .tenants(rsp.getCockpitConfigTenantInserts())
            .build())
          .addAllMessages(rsp.getCommitLogs().stream().map(log -> ImmutableMessage.builder()
              .exception(log.getException())
              .text(log.getText())
              .build()).toList())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();
      return result;
    })
    .onItem().invoke(newState -> {
      if(handleNewState != null) {
        handleNewState.accept(newState.getCockpitConfig());
      }
    });
  }
  
  private Uni<ImmutablePersistenceUnit> createRequest(CockpitDb tx) {
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
          .configId(OidUtils.genUUID())
          .build()
    );
    
    
    
    final var newConfig = new NewCockpitConfigBuilder(logger);
    this.config.accept(newConfig);
    final var created = newConfig.close();
    
    final var configId = created.getCockpitConfigInserts().iterator().next().getId();
    
    next = ImmutablePersistenceUnit.builder()
        .from(start)
        .from(created)
        .from(logger.withConfigId(configId).close())
        .build();
  
    return Uni.createFrom().item(next);
  }
  
  public static class CreateOneCockpitConfigException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final PersistenceUnit batch;
    
    public CreateOneCockpitConfigException(String message, PersistenceUnit batch) {
      super(message + System.lineSeparator() + " " +
          String.join(System.lineSeparator() + " ", batch.getCommitLogs().stream().map(e -> e.getText()).toList()));
      
      batch.getCommitLogs().stream().filter(e -> e.getException() != null).forEach(e -> addSuppressed(e.getException()));
      this.batch = batch;
    }
    
    public PersistenceUnit getBatch() {
      return batch;
    }
  }
}