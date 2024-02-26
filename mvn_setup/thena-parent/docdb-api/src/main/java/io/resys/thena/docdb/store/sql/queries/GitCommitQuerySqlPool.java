package io.resys.thena.docdb.store.sql.queries;

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

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ImmutableBranch;
import io.resys.thena.docdb.api.models.ImmutableCommit;
import io.resys.thena.docdb.api.models.ImmutableCommitLock;
import io.resys.thena.docdb.api.models.ImmutableTree;
import io.resys.thena.docdb.api.models.ThenaGitObject.Commit;
import io.resys.thena.docdb.api.models.ThenaGitObject.CommitLock;
import io.resys.thena.docdb.api.models.ThenaGitObject.CommitLockStatus;
import io.resys.thena.docdb.api.models.ThenaGitObject.CommitTree;
import io.resys.thena.docdb.models.git.GitQueries.GitCommitQuery;
import io.resys.thena.docdb.models.git.GitQueries.LockCriteria;
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.resys.thena.docdb.store.sql.support.SqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class GitCommitQuerySqlPool implements GitCommitQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Uni<Commit> getById(String commit) {
    final var sql = sqlBuilder.commits().getById(commit);
    if(log.isDebugEnabled()) {
      log.debug("Commit byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.commit(row))
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't get 'COMMIT' by 'id': '" + commit + "'!", e));
  }
  @Override
  public Multi<Commit> findAll() {
    final var sql = sqlBuilder.commits().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Commit findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.commit(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<Commit> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'COMMIT'!", e));
  }
  @Override
  public Uni<CommitLock> getLock(LockCriteria crit) {
    final var sql = sqlBuilder.commits().getLock(crit);
    if(log.isDebugEnabled()) {
      log.debug("Commit: {} getLock query, with props: {} \r\n{}",
          GitCommitQuerySqlPool.class,
          sql.getProps().deepToString(),
          sql.getValue());
    }
    final Function<io.vertx.mutiny.sqlclient.Row, CommitTree> mapper;
    if(crit.getTreeValueIds().isEmpty()) {
      mapper = (row) -> sqlMapper.commitTree(row);
    } else {
      mapper = (row) -> sqlMapper.commitTreeWithBlobs(row);
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
        .onFailure().invoke(e -> errorHandler.deadEnd("Can't find 'COMMIT LOCK ON REF' by head/commit: '" + crit.getHeadName() + "/" + crit.getCommitId() + "'!", e));
  }
}
