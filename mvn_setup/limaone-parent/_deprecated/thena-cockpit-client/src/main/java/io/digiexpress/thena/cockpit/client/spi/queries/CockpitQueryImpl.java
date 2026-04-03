package io.digiexpress.thena.cockpit.client.spi.queries;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.digiexpress.thena.cockpit.client.api.CockpitContainer;
import io.digiexpress.thena.cockpit.client.api.CockpitQueryActions.CockpitQuery;
import io.digiexpress.thena.cockpit.client.api.ImmutableCockpitContainer;
import io.digiexpress.thena.cockpit.client.api.entities.CockpitDocType;
import io.digiexpress.thena.cockpit.client.tables.CockpitDb;
import io.digiexpress.thena.cockpit.client.tables.CockpitDbQuery;
import io.digiexpress.thena.cockpit.client.tables.CockpitTableFilter;
import io.digiexpress.thena.cockpit.client.tables.ImmutableCockpitTableFilter;
import io.digiexpress.thena.cockpit.client.tables.ImmutableWorld;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CockpitQueryImpl implements CockpitQuery {

  private final Uni<CockpitDb> state;
  
  private List<String> cockpitIds;
  private final List<CockpitDocType> excludedDocs = new ArrayList<>();
  private boolean lockForUpdate;

  @Override
  public CockpitQuery lockForUpdate() {
    this.lockForUpdate = true;
    return this;
  }

  @Override
  public CockpitQuery excludeDocs(CockpitDocType... docs) {
    this.excludedDocs.addAll(Arrays.asList(docs));
    return this;
  }

  @Override
  public CockpitQuery addCockpitId(String id) {
    if(this.cockpitIds == null) {
      this.cockpitIds = new ArrayList<>();
    }
    this.cockpitIds.add(id);
    return this;
  }

  @Override
  public CockpitQuery addAllCockpitId(List<String> ids) {
    if(this.cockpitIds == null) {
      this.cockpitIds = new ArrayList<>();
    }
    this.cockpitIds.addAll(ids);
    return this;
  }

  @Override
  public Uni<CockpitContainer> getOne(String id) {
    this.addCockpitId(id);
    
    return findAll().collect().asList().onItem().transform(env -> {
      if(env.size() != 1) {
        throw new CockpitQueryException("Expecting exactly 1 result but found: " + env.size());
      }
      
      return env.getFirst();
    });
  }

  @Override
  public Multi<CockpitContainer> findAll() {
    return this.state
        .onItem().transformToUni(state -> startQuery(state))
        .onItem().transformToMulti(items -> Multi.createFrom().items(items.stream()));
  }
  
  @Override
  public Uni<Optional<CockpitContainer>> findOne() {
    return findAll().collect().asList().onItem().transform(env -> {
      if(env.size() > 1) {
        throw new CockpitQueryException("Expecting exactly 1 or 0 result but found: " + env.size());
      }
      
      return env.stream().findFirst();
    });
  }
  
  private Uni<List<CockpitContainer>> startQuery(CockpitDb state) {
    final var query = ImmutableCockpitTableFilter.builder()
        .lockForUpdate(Boolean.TRUE.equals(this.lockForUpdate))
        .cockpitConfigIds(Optional.ofNullable(this.cockpitIds == null || this.cockpitIds.isEmpty() ? null: this.cockpitIds))
        .build();

    return Uni.combine().all().unis(
        findAllConfigs(state, query),
        findAllTenants(state, query),
        findAllProps(state, query),
        findAllCommits(state, query),
        findAllCommitTrees(state, query)
      ).with(CockpitDbQuery.World.class, (containers) -> {
        final var combined = ImmutableWorld.builder();
        containers.forEach(container -> combined.from(container));
        final CockpitDbQuery.World built = combined.build();
        final var result = groupByCockpitConfig(built);
        return result;
      });
  }
  
  private Uni<CockpitDbQuery.World> findAllConfigs(CockpitDb state, CockpitTableFilter filter) {
    if(this.excludedDocs.contains(CockpitDocType.CONFIG)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryCockpitConfig().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().aliasConfig(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<CockpitDbQuery.World> findAllTenants(CockpitDb state, CockpitTableFilter filter) {
    if(this.excludedDocs.contains(CockpitDocType.CONFIG_TENANT)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryCockpitConfigTenant().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().cockpitConfigTenant(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<CockpitDbQuery.World> findAllProps(CockpitDb state, CockpitTableFilter filter) {
    if(this.excludedDocs.contains(CockpitDocType.CONFIG_PROPS)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryCockpitConfigProps().findAllByFilter(filter)
      .onItem().transform(items -> ImmutableWorld
          .builder().cockpitConfigProps(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<CockpitDbQuery.World> findAllCommits(CockpitDb state, CockpitTableFilter filter) {
    if(this.excludedDocs.contains(CockpitDocType.CONFIG_COMMIT)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryCockpitCommit().findAll()
      .onItem().transform(items -> ImmutableWorld
          .builder().cockpitCommit(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  private Uni<CockpitDbQuery.World> findAllCommitTrees(CockpitDb state, CockpitTableFilter filter) {
    if(this.excludedDocs.contains(CockpitDocType.CONFIG_COMMIT_TREE)) {
      return Uni.createFrom().item(ImmutableWorld.builder().build());
    }
    return state.query().queryCockpitCommitTree().findAll()
      .onItem().transform(items -> ImmutableWorld
          .builder().cockpitCommitTree(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
          .build()
      );
  }
  
  public static CockpitQueryImpl of(CockpitDb db) {
    return new CockpitQueryImpl(Uni.createFrom().item(db));
  }
  
  public static List<CockpitContainer> groupByCockpitConfig(CockpitDbQuery.World world) {
    final var builders = new java.util.HashMap<String, ImmutableCockpitContainer.Builder>();
    
    // Initialize builders for each config
    for(final var config : world.getCockpitConfig().values()) {
      builders.put(config.getId(), ImmutableCockpitContainer.builder()
          .config(config));
    }
    
    // Group all entities by config ID
    world.getCockpitConfigTenant().values().forEach(tenant -> {
      if(builders.containsKey(tenant.getCockpitConfigId())) {
        builders.get(tenant.getCockpitConfigId()).addTenants(tenant);
      }
    });
    
    world.getCockpitConfigProps().values().forEach(props -> {
      if(builders.containsKey(props.getCockpitConfigId())) {
        builders.get(props.getCockpitConfigId()).addProps(props);
      }
    });
    
    // Add commits and commit trees to all containers
    final var commits = world.getCockpitCommit().values();
    final var commitTrees = world.getCockpitCommitTree().values();
    
    builders.values().forEach(builder -> {
      builder.addAllCommits(commits);
      builder.addAllCommitTrees(commitTrees);
    });
    
    return builders.values().stream()
        .map(builder -> builder.build())
        .collect(Collectors.toList());
  }
  
  
  private static class CockpitQueryException extends RuntimeException {
    private static final long serialVersionUID = 7907387187541951150L;

    public CockpitQueryException(String message) {
      super(message);
    }
    
  }
}