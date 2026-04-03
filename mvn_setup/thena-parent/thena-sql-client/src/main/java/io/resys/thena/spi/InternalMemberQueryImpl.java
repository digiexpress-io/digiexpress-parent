package io.resys.thena.spi;

import java.util.Optional;
import java.util.UUID;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.Member;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.spi.TenantDataSource.InternalMemberQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;



@Slf4j(topic = LogConstants.SHOW_SQL)
public class InternalMemberQueryImpl implements InternalMemberQuery {
  protected final ThenaSqlDataSource dataSource;
  protected final MemberRegistry registry;
  
  public InternalMemberQueryImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = new MemberRegistrySqlImpl(dataSource.getRegistry());
  }

  public ThenaSqlClient getClient() {
    return dataSource.getClient();
  }
  @Override
  public Uni<Optional<Member>> findById(UUID id) {
    final var sql = registry.getById(id.toString());
    if(log.isDebugEnabled()) {
      log.debug("Find member by id query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Member> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return Optional.of(it.next());
          }
          return Optional.<Member>empty();
        })
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> {
          dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'member' by 'id'!", sql, e));
        });
  }

  @Override
  public Uni<Member> getById(UUID id) {
    final var sql = registry.getById(id.toString());
    if(log.isDebugEnabled()) {
      log.debug("Get member by id query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Member> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> {
          dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'member' by 'id'!", sql, e));
        });
  }

  
  @Override
  public Multi<Member> findAllByExtId(String externalId) {
    final var sql = registry.findByExternalId(externalId);
    
    if(log.isDebugEnabled()) {
      log.debug("Find member by externalId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<Member> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithCompletion()
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'member' by 'externalId'!", sql, e)));
  }
  
  
  @Override
  public Multi<Member> findAll() {
    final var sql = this.registry.findAll();
    if(log.isDebugEnabled()) {
      log.debug("Find all members query, with props: {} \r\n{}", 
          "", 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
      .mapping(registry.defaultMapper())
      .execute()
      .onItem()
      .transformToMulti((RowSet<Member> rowset) -> Multi.createFrom().iterable(rowset))
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithCompletion()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlFailed("Can't find 'member'!", sql, e)));
  }

  @Override
  public Uni<Member> delete(Member existingalias) {
    final var sql = this.registry.deleteOne(existingalias);
    if(log.isDebugEnabled()) {
      log.debug("Delete member query, with props: {} \r\n{}", 
          sql.getPropsDeepString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
      .mapping(registry.defaultMapper())
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Member> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't delete 'member'!", sql, e)));
  }

  @Override
  public Uni<Member> insert(Member newalias) {
    final var sql = this.registry.insertOne(newalias);
    if(log.isDebugEnabled()) {
      log.debug("Insert member query, with props: {} \r\n{}", 
          sql.getPropsDeepString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
      .mapping(registry.defaultMapper())
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Member> rowset) -> {
        
        return newalias;
      })
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't insert 'member'!", sql, e)));
  }

  @Override
  public Uni<Member> merge(Member existingalias) {
    final var sql = this.registry.updateOne(existingalias);
    if(log.isDebugEnabled()) {
      log.debug("Update member query, with props: {} \r\n{}", 
          sql.getPropsDeepString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
      .mapping(registry.defaultMapper())
      .execute(sql.getProps())
      .onItem()
      .transform((RowSet<Member> rowset) -> {
        final var it = rowset.iterator();
        if(it.hasNext()) {
          return it.next();
        }
        return null;
      })
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't update 'member'!", sql, e)));
  }

  @Override
  public Uni<Optional<Member>> findByExtIdAndAliasId(String externalId, UUID uuid) {
    final var sql = registry.getByExtIdAndAliasId(externalId, uuid);
    if(log.isDebugEnabled()) {
      log.debug("Get member by extIdAndAliasId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(registry.defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Member> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return Optional.of(it.next());
          }
          return Optional.<Member>empty();
        })
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithItem(Optional.empty())
        .onFailure().invoke(e -> {
          dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'member' by 'extIdAndGroupName'!", sql, e));
        });
  }


  @Override
  public Multi<Member> findByExtIdAndAliasIdAndRef(String extId, UUID uuid, String ref) {
    final var sql = this.registry.findByExtIdAndAliasIdAndRef(extId, uuid, ref);
    if(log.isDebugEnabled()) {
      log.debug("Find member by extIdAndAliasIdAndRef, with props: {} \r\n{}", 
          sql.getPropsDeepString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
      .mapping(registry.defaultMapper())
      .execute(sql.getProps())
      .onItem()
      .transformToMulti((RowSet<Member> rowset) -> Multi.createFrom().iterable(rowset))
      .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithCompletion()
      .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'member' by: 'extIdAndAliasIdAndRef'!", sql, e)));
  }
  
}
