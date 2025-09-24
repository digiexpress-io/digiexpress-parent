package io.resys.thena.storesql.builders;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.actions.FsQueryActions.FsArchiveQueryType;
import io.resys.thena.api.entities.fs.ImmutableFsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsContainers.FsDirentContainer;
import io.resys.thena.api.entities.fs.ThenaFsObject.FsDocType;
import io.resys.thena.api.registry.FsRegistry;
import io.resys.thena.api.registry.fs.ImmutableFsDirentFilter;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.registry.fs.FsRegistrySqlImpl;
import io.resys.thena.structures.fs.FsQueries;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class InternalFsDirentContainerQuerySql implements FsQueries.InternalDirentQuery {

  private final ThenaSqlDataSource dataSource;
  private final FsRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  private final Collection<FsDocType> docsToExclude = new LinkedHashSet<>();
  private final ImmutableFsDirentFilter.Builder builder = ImmutableFsDirentFilter.builder();
  private ImmutableFsDirentFilter filter;
  private List<String> direntIds;
  
  public InternalFsDirentContainerQuerySql(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = new FsRegistrySqlImpl(dataSource.getRegistry());
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public FsQueries.InternalDirentQuery onlyDocs(FsDocType... docs) {
    docsToExclude.clear();
    docsToExclude.addAll(Arrays.asList(FsDocType.values()));
    docsToExclude.removeAll(Arrays.asList(docs));
    return this;
  }
  @Override
  public FsQueries.InternalDirentQuery excludeDocs(FsDocType... docs) {
    docsToExclude.addAll(Arrays.asList(docs));
    return this;
  }
  @Override
  public FsQueries.InternalDirentQuery direntId(String... missionId) {
    if(this.direntIds == null) {
      this.direntIds = new ArrayList<>();
    }
    this.direntIds.addAll(Arrays.asList(missionId));
    return this;
  }
  @Override
  public FsQueries.InternalDirentQuery archived(FsArchiveQueryType includeArchived) {
    this.builder.archived(includeArchived);
    return this;
  }
  @Override
  public FsQueries.InternalDirentQuery lockForUpdate() {
    this.builder.lockForUpdate(true);
    return this;
  }
  @Override
  public Uni<FsDirentContainer> getById(String direntId) {
    builder.direntIds(Arrays.asList(direntId));
    this.filter = builder.build();
    return Uni.combine().all().unis(
        findAllLinks(),
        findAllRemarks(),
        findAllData(),
        findAllAssignments(),
        findAllCommits(),
        findAllDirents(),
        findAllLabels()
      ).with(FsDirentContainer.class, (containers) -> {
        final var combined = ImmutableFsDirentContainer.builder();
        containers.forEach(container -> combined.from(container));
        final FsDirentContainer built = combined.build();
        final var result = built.groupByDirent();
        if(result.isEmpty()) {
          return null;
        }
        return result.iterator().next();
      });
  }
  @Override
  public Multi<FsDirentContainer> findAll() {
    this.filter = builder.direntIds(Optional.ofNullable(direntIds)).build();
    return Uni.combine().all().unis(
      findAllLinks(),
      findAllRemarks(),
      findAllData(),
      findAllAssignments(),
      findAllCommits(),
      findAllDirents(),
      findAllLabels()
    ).with(FsDirentContainer.class, (containers) -> {
      final var combined = ImmutableFsDirentContainer.builder();
      containers.forEach(container -> combined.from(container));
      final FsDirentContainer built = combined.build();
      return built.groupByDirent();
    }).onItem().transformToMulti(e -> Multi.createFrom().items(e.stream()));
  }
  private Uni<FsDirentContainer> findAllCommits() {
    if(docsToExclude.contains(FsDocType.FS_COMMIT)) {
      return Uni.createFrom().item(ImmutableFsDirentContainer.builder().build());
    }

    final var sql = registry.commits().findAll(filter);
    if(log.isDebugEnabled()) {
      log.debug("Dirent findAllCommits query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.commits().defaultMapper())
        .execute(sql.getProps()).onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", FsDocType.FS_COMMIT)))
        .onItem().transform(items -> ImmutableFsDirentContainer
            .builder().commits(items.stream().collect(Collectors.toMap(e -> e.getCommitId(), e -> e)))
            .build()
        );
  }
  private Uni<FsDirentContainer> findAllAssignments() {
    if(docsToExclude.contains(FsDocType.FS_DIRENT_ASSIGNMENT)) {
      return Uni.createFrom().item(ImmutableFsDirentContainer.builder().build());
    }

    // query ASSIGNMENTS by mission id
    final var sql = registry.direntAssignments().findAll(filter);
    if(log.isDebugEnabled()) {
      log.debug("Dirent findAllAssignments query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.direntAssignments().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", FsDocType.FS_DIRENT_ASSIGNMENT)))
        .onItem().transform(items -> ImmutableFsDirentContainer
            .builder().assignments(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<FsDirentContainer> findAllData() {
    if(docsToExclude.contains(FsDocType.FS_DIRENT_DATA)) {
      return Uni.createFrom().item(ImmutableFsDirentContainer.builder().build());
    }

    final var sql = registry.direntData().findAll(filter);
    if(log.isDebugEnabled()) {
      log.debug("Dirent findAllData query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.direntData().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", FsDocType.FS_DIRENT_DATA)))
        .onItem().transform(items -> ImmutableFsDirentContainer
            .builder().data(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }

  private Uni<FsDirentContainer> findAllRemarks() {
    if(docsToExclude.contains(FsDocType.FS_DIRENT_REMARK)) {
      return Uni.createFrom().item(ImmutableFsDirentContainer.builder().build());
    }

    final var sql = registry.direntRemarks().findAll(filter);
    if(log.isDebugEnabled()) {
      log.debug("Dirent findAllRemarks query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.direntRemarks().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", FsDocType.FS_DIRENT_REMARK)))
        .onItem().transform(items -> ImmutableFsDirentContainer
            .builder().remarks(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }

  private Uni<FsDirentContainer> findAllLinks() {
    if(docsToExclude.contains(FsDocType.FS_DIRENT_LINKS)) {
      return Uni.createFrom().item(ImmutableFsDirentContainer.builder().build());
    }

    final var sql = registry.direntLinks().findAll(filter);
    if(log.isDebugEnabled()) {
      log.debug("Dirent findAllLinks query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.direntLinks().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", FsDocType.FS_DIRENT_LINKS)))
        .onItem().transform(items -> ImmutableFsDirentContainer
            .builder().links(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }

  private Uni<FsDirentContainer> findAllDirents() {
    if(docsToExclude.contains(FsDocType.FS_DIRENT)) {
      return Uni.createFrom().item(ImmutableFsDirentContainer.builder().build());
    }

    final var sql = registry.dirents().findAll(filter);
    if(log.isDebugEnabled()) {
      log.debug("Dirent findAllDirents query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.dirents().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", FsDocType.FS_DIRENT)))
        .onItem().transform(items -> ImmutableFsDirentContainer
            .builder().dirents(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
  private Uni<FsDirentContainer> findAllLabels() {
    if(docsToExclude.contains(FsDocType.FS_DIRENT_LABEL)) {
      return Uni.createFrom().item(ImmutableFsDirentContainer.builder().build());
    }

    
    final var sql = registry.direntLabels().findAll(filter);
    if(log.isDebugEnabled()) {
      log.debug("Dirent findAllLabels query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.direntLabels().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti).collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", FsDocType.FS_DIRENT_LABEL)))
        .onItem().transform(items -> ImmutableFsDirentContainer
            .builder().direntLabels(items.stream().collect(Collectors.toMap(e -> e.getId(), e -> e)))
            .build()
        );
  }
}
