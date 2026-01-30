package io.resys.thena.grim.spi.builders;

/*-
 * #%L
 * thena-grim-client
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
import java.util.Optional;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.grim.GrimProcess;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.grim.spi.GrimDataSource.InternalProcQuery;
import io.resys.thena.grim.spi.datasource.GrimRegistry;
import io.resys.thena.grim.spi.datasource.GrimRegistrySqlImpl;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class InternalProcQueryImpl implements InternalProcQuery {
  private final ThenaSqlDataSource dataSource;
  private final GrimRegistry registry;
  private final ThenaSqlDataSourceErrorHandler errorHandler;
  private boolean includeFormBody = false;
  
  public InternalProcQueryImpl(ThenaSqlDataSource dataSource) {
    super();
    this.dataSource = dataSource;
    this.registry = new GrimRegistrySqlImpl(dataSource.getRegistry());
    this.errorHandler = dataSource.getErrorHandler();
  }
  @Override
  public InternalProcQuery includeFormBody(boolean includeFormBody) {
    this.includeFormBody = includeFormBody;
    return this;
  }
  
  @Override
  public Multi<GrimProcess> findOnOrAfter(OffsetDateTime onOrAfter) {
    final var sql = registry.processes().findOnOrAfter(onOrAfter, includeFormBody);
    if(log.isDebugEnabled()) {
      log.debug("InternalProcQueryImpl.findOnOrAfter query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.processes().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti)
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_PROCESS)));
  }

  @Override
  public Multi<GrimProcess> findOnOrBeforeWithoutMission(OffsetDateTime onOrBefore) {
    final var sql = registry.processes().findOnOrBeforeWithoutMission(onOrBefore, includeFormBody);
    if(log.isDebugEnabled()) {
      log.debug("InternalProcQueryImpl.findOnOrBeforeWithoutMission query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.processes().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti)
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_PROCESS)));
  }

  @Override
  public Uni<GrimProcess> getOneById(String id) {
    final var sql = registry.processes().getById(id, includeFormBody);
    if(log.isDebugEnabled()) {
      log.debug("InternalProcQueryImpl.getById query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.processes().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform(rowset -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_PROCESS)));

  }

  @Override
  public Uni<Optional<GrimProcess>> findOneByMissionId(String missionId) {
    final var sql = registry.processes().findOneByMissionId(missionId, includeFormBody);
    if(log.isDebugEnabled()) {
      log.debug("InternalProcQueryImpl.findOneByMissionId query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.processes().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform(rowset -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return Optional.of(it.next());
          }
          return Optional.<GrimProcess>empty();
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_PROCESS)));
  }

  @Override
  public Uni<Optional<GrimProcess>> findOneById(String id) {
    final var sql = registry.processes().getById(id, includeFormBody);
    if(log.isDebugEnabled()) {
      log.debug("InternalProcQueryImpl.findOneById query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.processes().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform(rowset -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return Optional.of(it.next());
          }
          return Optional.<GrimProcess>empty();
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_PROCESS)));
  }

  @Override
  public Multi<GrimProcess> findAllNotArchivedyUserId(String userId) {
    final var sql = registry.processes().findNotArchivedByUserId(userId, includeFormBody);
    if(log.isDebugEnabled()) {
      log.debug("InternalProcQueryImpl.findActiveByUserId query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.processes().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transformToMulti(RowSet::toMulti)
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_PROCESS)));
  }

  @Override
  public Uni<GrimProcess> getOneByIdWithLock(String id) {
    final var sql = registry.processes().getOneByIdWithLock(id, includeFormBody);
    if(log.isDebugEnabled()) {
      log.debug("InternalProcQueryImpl.getOneByIdWithLock query, with props: {} \r\n{}", 
          sql.getPropsDeepString(),
          sql.getValue());
    }
    return dataSource.getClient().preparedQuery(sql.getValue())
        .mapping(registry.processes().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform(rowset -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(sql.failed(e, "Can't find '%s'!", GrimDocType.GRIM_PROCESS)));
  }
}
