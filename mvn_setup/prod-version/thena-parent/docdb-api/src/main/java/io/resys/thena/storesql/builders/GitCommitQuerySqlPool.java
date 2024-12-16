package io.resys.thena.storesql.builders;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import java.util.Optional;
import java.util.function.Function;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.CommitLockStatus;
import io.resys.thena.api.entities.git.Commit;
import io.resys.thena.api.entities.git.CommitLock;
import io.resys.thena.api.entities.git.CommitTree;
import io.resys.thena.api.entities.git.ImmutableBranch;
import io.resys.thena.api.entities.git.ImmutableCommit;
import io.resys.thena.api.entities.git.ImmutableCommitLock;
import io.resys.thena.api.entities.git.ImmutableTree;
import io.resys.thena.api.registry.git.CommitRegistry;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.structures.git.GitQueries.GitCommitQuery;
import io.resys.thena.structures.git.GitQueries.LockCriteria;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class GitCommitQuerySqlPool implements GitCommitQuery {
  private final ThenaSqlDataSource wrapper;
  private final CommitRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;

  public GitCommitQuerySqlPool(ThenaSqlDataSource dataSource) {
    this.wrapper = dataSource;
    this.registry = dataSource.getRegistry().git().commits();
    this.errorHandler = dataSource.getErrorHandler();
  }
  
  @Override
  public Uni<Commit> getById(String commit) {
    final var sql = registry.getById(commit);
    if(log.isDebugEnabled()) {
      log.debug("Commit byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Commit> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'COMMIT' by 'id': '" + commit + "'!", sql, e)));
  }
  @Override
  public Multi<Commit> findAll() {
    final var sql = registry.findAll();
    if(log.isDebugEnabled()) {
      log.debug("Commit findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute()
        .onItem()
        .transformToMulti((RowSet<Commit> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'COMMIT'!", sql, e)));
  }
  @Override
  public Uni<CommitLock> getLock(LockCriteria crit) {
    final var sql = registry.getLock(crit);
    if(log.isDebugEnabled()) {
      log.debug("Commit: {} getLock query, with props: {} \r\n {}",
          GitCommitQuerySqlPool.class,
          sql.getPropsDeepString(),
          sql.getValue());
    }
    final Function<io.vertx.mutiny.sqlclient.Row, CommitTree> mapper;
    if(crit.getTreeValueIds().isEmpty()) {
      mapper = registry.commitTreeMapper();
    } else {
      mapper = registry.commitTreeWithBlobsMapper();
    }
    
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(mapper)
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<CommitTree> rowset) -> {
          final var builder = ImmutableCommitLock.builder();
          final var it = rowset.iterator();
          
          ImmutableTree.Builder tree = null;
          while(it.hasNext()) {
            final var commitTree = it.next();
            if(tree == null) {
              tree = ImmutableTree.builder().id(commitTree.getTreeId());
              
              builder
                .branch(ImmutableBranch.builder()
                    .commit(commitTree.getCommitId())
                    .name(commitTree.getBranchName())
                    .build())
                .commit(ImmutableCommit.builder()
                  .author(commitTree.getCommitAuthor())
                  .dateTime(commitTree.getCommitDateTime())
                  .id(commitTree.getCommitId())
                  .merge(Optional.ofNullable(commitTree.getCommitMerge()))
                  .message(commitTree.getCommitMessage())
                  .parent(Optional.ofNullable(commitTree.getCommitParent()))
                  .tree(commitTree.getTreeId())
                  .build());
            }
            if(commitTree.getTreeValue().isPresent()) {
              tree.putValues(commitTree.getTreeValue().get().getName(), commitTree.getTreeValue().get());  
            }
            if(commitTree.getBlob().isPresent()) {
              builder.putBlobs(commitTree.getBlob().get().getId(), commitTree.getBlob().get());
            }
          }
          if(tree != null) {
            builder.status(CommitLockStatus.LOCK_TAKEN).tree(tree.build());
          } else {
            builder.status(CommitLockStatus.NOT_FOUND);
          }
          final CommitLock lock = builder.build();
          return lock;
        })
        //.onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'COMMIT LOCK ON REF' by head/commit: '" + crit.getHeadName() + "/" + crit.getCommitId() + "'!", sql, e)))
        ;
  }
}
