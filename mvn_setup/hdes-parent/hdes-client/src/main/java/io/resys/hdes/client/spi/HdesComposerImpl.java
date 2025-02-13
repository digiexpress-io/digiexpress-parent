package io.resys.hdes.client.spi;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer;
import io.resys.hdes.client.api.HdesStore.HistoryEntity;
import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ImmutableComposerState;
import io.resys.hdes.client.api.ImmutableUpdateStoreEntity;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTagSummary;
import io.resys.hdes.client.api.diff.TagDiff;
import io.resys.hdes.client.spi.changeset.AstCommandOptimiser;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.composer.CopyAsEntityVisitor;
import io.resys.hdes.client.spi.composer.CreateEntityVisitor;
import io.resys.hdes.client.spi.composer.DataDumpVisitor;
import io.resys.hdes.client.spi.composer.DebugVisitor;
import io.resys.hdes.client.spi.composer.DeleteEntityVisitor;
import io.resys.hdes.client.spi.composer.DryRunVisitor;
import io.resys.hdes.client.spi.composer.ImportEntityVisitor;
import io.smallrye.mutiny.Uni;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HdesComposerImpl implements HdesComposer {

  private final HdesClient client;
  private final AstCommandOptimiser opt;
  public HdesComposerImpl(HdesClient client) {
    super();
    this.client = client;
    this.opt = new AstCommandOptimiser(client);
  }

  @Override
  public Uni<ComposerState> get() {
    return client.store().query().get().onItem().transform(this::state);
  }
  @Override
  public Uni<ComposerState> update(UpdateEntity asset) {
    return get(asset.getId()).onItem().transformToUni(old -> 
        client.store().update(
            ImmutableUpdateStoreEntity.builder().id(asset.getId())
            .body(opt.optimise(asset.getBody(), old.getSource().getBodyType()))
            .build()
        ))
        .onItem().transformToUni((updated) -> {
          // flush cache
          client.config().getCache().flush(asset.getId());
        
          return client.store().query().get().onItem().transform(this::state);
        });
  }
  @Override
  public Uni<ComposerState> create(CreateEntity asset) {
    return client.store().query().get().onItem().transform(this::state)
        .onItem().transformToUni(state -> client.store().batch(new CreateEntityVisitor(state, asset, client).visit()))
        .onItem().transformToUni(savedEntity -> client.store().query().get().onItem().transform(this::state));
  }
  @Override
  public Uni<ComposerState> delete(String id) {
    return client.store().query().get().onItem().transform(this::state)
        .onItem().transformToUni(state -> client.store().delete(new DeleteEntityVisitor(state, id).visit()))
        .onItem().transformToUni(savedEntities -> {
          // flush cache
          for (var entity : savedEntities) {
            client.config().getCache().flush(entity.getId());
          }
          return client.store().query().get().onItem().transform(this::state);
        });
  }
  @Override
  public Uni<ComposerEntity<?>> get(String idOrName) {
    return client.store().query().get().onItem().transform(this::state)
      .onItem().transform(state -> {
        List<ComposerEntity<?>> entities = new ArrayList<>();
        entities.addAll(state.getDecisions().values());
        entities.addAll(state.getFlows().values());
        entities.addAll(state.getServices().values());
        entities.addAll(state.getTags().values());
        return entities.stream()
            .filter(e -> e.getId().equals(idOrName) || (e.getAst() != null && e.getAst().getName().equals(idOrName)))
            .findFirst().orElse(null);
      });
  }
  @Override
  public Uni<HistoryEntity> getHistory(String id) {
    return client.store().history().get(id);
  }
  @Override
  public Uni<ComposerState> copyAs(CopyAs copyAs) {
    return client.store().query().get().onItem().transform(this::state)
        .onItem().transform(state -> new CopyAsEntityVisitor(state, copyAs, client).visit())
        .onItem().transformToUni(newEntity -> client.store().create(newEntity))
        .onItem().transformToUni(savedEntity -> client.store().query().get().onItem().transform(this::state));
  }
  @Override
  public Uni<DebugResponse> debug(DebugRequest entity) {
    return client.store().query().get().onItem()
        .transform(state -> new DebugVisitor(client).visit(entity, state));
  }
  @Override
  public Uni<ComposerEntity<?>> dryRun(UpdateEntity entity) {
    return client.store().query().get().onItem().transform(state -> new DryRunVisitor(client).visit(state, entity));
  }
  @Override
  public Uni<StoreDump> getStoreDump() {
    return client.store().query().get().onItem().transform(state -> new DataDumpVisitor(client).visit(state));
  }

  @Override
  public Uni<TagDiff> diff(DiffRequest request) {
    return client.store().query().get().onItem().transform(state -> client.diff()
        .tags(state.getTags().values())
        .baseId(request.getBaseId())
        .targetId(request.getTargetId())
        .targetDate(LocalDateTime.now())
        .build());
  }

  @Override
  public Uni<AstTagSummary> summary(String tagId) {
    return client.store().query().get().onItem().transform(state -> client.summary()
        .tags(state.getTags().values())
        .tagId(tagId)
        .build());
  }

  @Override
  public HdesComposer withBranch(String branchName) {
    if (branchName == null || branchName.isBlank()) {
      return this;
    }
    return new HdesComposerImpl(client.withBranch(branchName));
  }

  private ComposerState state(StoreState source) {
    // create envir
    final var envir = ComposerEntityMapper.toEnvir(client.envir().tagName(source.getTagName()), source).build();
    
    // map envir
    final var builder = ImmutableComposerState.builder();
    envir.getValues().values().forEach(v -> ComposerEntityMapper.toComposer(builder, v));
    return (ComposerState) builder.build(); 
  }

  @Override
  public Uni<ComposerState> importTag(AstTag asset) {
    return client.store().query().get().onItem().transform(this::state)
        .onItem().transform(state -> new ImportEntityVisitor(state, asset, client).visit())
        .onItem().transformToUni(newEntity -> client.store().batch(newEntity))
        .onItem().transformToUni(savedEntity -> client.store().query().get().onItem().transform(this::state));
  }
}
