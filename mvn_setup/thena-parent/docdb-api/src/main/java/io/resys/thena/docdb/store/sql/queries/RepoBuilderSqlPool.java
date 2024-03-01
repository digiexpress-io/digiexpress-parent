package io.resys.thena.docdb.store.sql.queries;

import io.resys.thena.docdb.api.LogConstants;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.models.Repo;
import io.resys.thena.docdb.api.models.Repo.RepoType;
import io.resys.thena.docdb.spi.DbCollections;
import io.resys.thena.docdb.spi.DbState.RepoBuilder;
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.resys.thena.docdb.store.sql.SqlSchema;
import io.resys.thena.docdb.support.ErrorHandler;
import io.resys.thena.docdb.support.ErrorHandler.SqlSchemaFailed;
import io.resys.thena.docdb.support.ErrorHandler.SqlFailed;
import io.resys.thena.docdb.support.ErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class RepoBuilderSqlPool implements RepoBuilder {
  private final io.vertx.mutiny.sqlclient.Pool pool;
  private final io.vertx.mutiny.sqlclient.SqlClient client;
  private final DbCollections names;
  private final SqlSchema sqlSchema;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  
  private io.vertx.mutiny.sqlclient.SqlClient getClient() {
    if(client == null) {
      return pool;
    }
    return client;
  }

  @Override
  public Uni<Repo> getByName(String name) {
    final var sql = sqlBuilder.repo().getByName(name);
    if(log.isDebugEnabled()) {
      log.debug("Repo by name query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.repo(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Repo> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> {
          
          
          errorHandler.deadEnd(new SqlTupleFailed("Can't find 'REPOS' by 'name'!", sql, e));
        });
  }

  @Override
  public Uni<Repo> getByNameOrId(String nameOrId) {
    final var sql = sqlBuilder.repo().getByNameOrId(nameOrId);
    
    if(log.isDebugEnabled()) {
      log.debug("Repo by nameOrId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.repo(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Repo> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'REPOS' by 'name' or 'id'!", sql, e)));
  }
  
  @Override
  public Uni<Repo> insert(final Repo newRepo) {
    final var next = names.toRepo(newRepo);
    final var sqlSchema = this.sqlSchema.withOptions(next);
    
    return pool.withTransaction(tx -> {
      final var repoInsert = this.sqlBuilder.withOptions(next).repo().insertOne(newRepo);
      final var tablesCreate = new StringBuilder();
      
      if(newRepo.getType() == RepoType.git) {
        tablesCreate.append(sqlSchema.createGitBlobs().getValue())
          .append(sqlSchema.createGitCommits().getValue())
          .append(sqlSchema.createGitTreeItems().getValue())
          .append(sqlSchema.createGitTrees().getValue())
          .append(sqlSchema.createGitRefs().getValue())
          .append(sqlSchema.createGitTags().getValue())
          
          .append(sqlSchema.createGitCommitsConstraints().getValue())
          .append(sqlSchema.createGitRefsConstraints().getValue())
          .append(sqlSchema.createGitTagsConstraints().getValue())
          .append(sqlSchema.createGitTreeItemsConstraints().getValue())
          .toString();
        
      } else if(newRepo.getType() == RepoType.org) {
        
        tablesCreate
          .append(sqlSchema.createOrgRoles().getValue())
          .append(sqlSchema.createOrgGroups().getValue())
          .append(sqlSchema.createOrgGroupRoles().getValue())
          
          .append(sqlSchema.createOrgUsers().getValue())
          .append(sqlSchema.createOrgUserRoles().getValue())
          .append(sqlSchema.createOrgUserMemberships().getValue())
          

          .append(sqlSchema.createOrgActorStatus().getValue())
          .append(sqlSchema.createOrgCommits().getValue())
          .append(sqlSchema.createOrgActorData().getValue())
          
          .append(sqlSchema.createOrgRolesConstraints().getValue())
          .append(sqlSchema.createOrgUserConstraints().getValue())
          .append(sqlSchema.createOrgGroupConstraints().getValue())
          .append(sqlSchema.createOrgCommitConstraints().getValue())
          
          .toString();
        
      } else  {
        tablesCreate
          .append(sqlSchema.createDoc().getValue())
          .append(sqlSchema.createDocBranch().getValue())
          .append(sqlSchema.createDocCommits().getValue())
          .append(sqlSchema.createDocLog().getValue())
  
          .append(sqlSchema.createDocBranchConstraints().getValue())
          .append(sqlSchema.createDocCommitsConstraints().getValue())
          .append(sqlSchema.createDocLogConstraints().getValue())
          .toString();
      }
      
      if(log.isDebugEnabled()) {
        log.debug(new StringBuilder("Creating schema: ")
            .append(System.lineSeparator())
            .append(tablesCreate.toString())
            .toString());
      }
      
      final Uni<Void> create = getClient().preparedQuery(sqlSchema.createRepo().getValue()).execute()
          .onItem().transformToUni(data -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't create table 'REPO'!", sqlSchema.createRepo(), e)));
      
      
      final Uni<Void> insert = tx.preparedQuery(repoInsert.getValue()).execute(repoInsert.getProps())
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't insert into 'REPO'!", repoInsert, e)));
      final Uni<Void> nested = tx.query(tablesCreate.toString()).execute()
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> errorHandler.deadEnd(new SqlSchemaFailed("Can't create tables!", tablesCreate.toString(), e)));
      
      
      return create
          .onItem().transformToUni((junk) -> insert)
          .onItem().transformToUni((junk) -> nested)
          .onItem().transform(junk -> newRepo);
    });
  }

  @Override
  public Multi<Repo> findAll() {
    final var sql = this.sqlBuilder.repo().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Fina all repos query, with props: {} \r\n{}", 
          "", 
          sql.getValue());
    }
    
    
    return getClient().preparedQuery(sql.getValue())
    .mapping(row -> sqlMapper.repo(row))
    .execute()
    .onItem()
    .transformToMulti((RowSet<Repo> rowset) -> Multi.createFrom().iterable(rowset))
    .onFailure(e -> errorHandler.notFound(e)).recoverWithCompletion()
    .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'REPOS'!", sql, e)));
  }
  
  
  @Override
  public Uni<Repo> delete(final Repo newRepo) {
    final var next = names.toRepo(newRepo);
    final var sqlSchema = this.sqlSchema.withOptions(next);
    
    return pool.withTransaction(tx -> {
      final var repoDelete = this.sqlBuilder.withOptions(next).repo().deleteOne(newRepo);
      final var tablesDrop = new StringBuilder();
      
      if(newRepo.getType() == RepoType.git) {
        tablesDrop
        .append(sqlSchema.dropGitRefs().getValue())
        .append(sqlSchema.dropGitTags().getValue())
        .append(sqlSchema.dropGitCommits().getValue())
        .append(sqlSchema.dropGitTreeItems().getValue())
        .append(sqlSchema.dropGitTrees().getValue())
        .append(sqlSchema.dropGitBlobs().getValue());
        
      } else if(newRepo.getType() == RepoType.org) {
        
        tablesDrop
        .append(sqlSchema.dropOrgRoles().getValue())
        .append(sqlSchema.dropOrgGroups().getValue())
        .append(sqlSchema.dropOrgGroupRoles().getValue())

        .append(sqlSchema.dropOrgUsers().getValue())
        .append(sqlSchema.dropOrgUserRoles().getValue())
        .append(sqlSchema.dropOrgUserMemberships().getValue())
        
        .append(sqlSchema.dropOrgActorStatus().getValue())
        .append(sqlSchema.dropOrgActorLogs().getValue())
        .append(sqlSchema.dropOrgActorData().getValue());
        
      } else {
        tablesDrop
        .append(sqlSchema.dropDocLog().getValue())
        .append(sqlSchema.dropDocCommit().getValue())
        .append(sqlSchema.dropDocBranch().getValue())
        .append(sqlSchema.dropDoc().getValue());        
        
      }
      
      
      if(log.isDebugEnabled()) {
        log.debug("Delete repo by name query, with props: {} \r\n{}", 
            repoDelete.getProps().deepToString(), 
            repoDelete.getValue());
        
        
        log.debug(new StringBuilder("Drop schema: ")
            .append(System.lineSeparator())
            .append(tablesDrop.toString())
            .toString());
      }
      
      
      final Uni<Void> insert = tx.preparedQuery(repoDelete.getValue()).execute(repoDelete.getProps())
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't delete from 'REPO'!", repoDelete, e)));
      final Uni<Void> nested = tx.query(tablesDrop.toString()).execute()
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> errorHandler.deadEnd(new SqlSchemaFailed("Can't drop tables!", tablesDrop.toString(), e)));
      
      return insert
          .onItem().transformToUni(junk -> nested)
          .onItem().transform(junk -> newRepo);
    });
  }
}
