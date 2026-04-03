package io.resys.thena.docdb.test;

/*-
 * #%L
 * thena-git-client
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ImmutableTenantContext;
import io.resys.thena.datasource.TenantCacheImpl;
import io.resys.thena.datasource.TenantContext;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlClient.ThenaSqlPool;
import io.resys.thena.datasource.ThenaSqlDataSource.TenantCache;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlSchemaFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceImpl;
import io.resys.thena.datasource.vertx.ThenaSqlPoolVertx;
import io.resys.thena.spi.InternalAliasQueryImpl;
import io.resys.thena.spi.InternalMemberQueryImpl;
import io.resys.thena.spi.TenantActionsImpl;
import io.resys.thena.spi.TenantDataSource;
import io.resys.thena.spi.TenantRegistrySqlImpl;
import io.resys.thena.storesql.PgErrors;
import io.resys.thena.test.ThenaTest;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ThenaTest
public class DbTestTemplate {
  private TenantActionsImpl client;
  @SuppressWarnings("unused")
  private io.vertx.mutiny.sqlclient.Pool pgPool;
  protected static Duration atMost = Duration.ofMinutes(1);
  
  @BeforeEach
  public void setUp(io.vertx.mutiny.sqlclient.Pool pgPool) throws InterruptedException {
    waitUntilPostgresqlAcceptsConnections(pgPool);
    
    final String tenantName = "";
    final ThenaSqlPool pool = new ThenaSqlPoolVertx(pgPool);
    final TenantContext tenantTableNames = ImmutableTenantContext.defaults("");
    final ThenaSqlDataSourceErrorHandler errorHandler = new PgErrors(); 
    final Optional<ThenaSqlClient> tx = Optional.of(pool);
    final TenantCache tenantCache = new TenantCacheImpl();
    
    final var source = new ThenaSqlDataSourceImpl(tenantName, tenantTableNames, pool, errorHandler, tx, tenantCache);
    final TenantDataSource db = new TenantDataSource() {
      @Override public ThenaDataSource getDataSource() { return source; }
      @Override public InternalTenantQuery tenant() {
        return new io.resys.thena.spi.InternalTenantQueryImpl(source) {
          @Override
          public Uni<Tenant> delete(Tenant newRepo) {
            final var tenantDelete = registry.dropTable();
            final var pool = dataSource.getPool();
            return  pool.query(tenantDelete.getValue()).execute()
              .onItem().transformToUni(rowSet -> {
                this.dataSource.getTenantCache().invalidateAll();
                return Uni.createFrom().item(newRepo);
              })
              .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlSchemaFailed("Can't drop tenant table!", tenantDelete.getValue(), e)));
          }
          @Override
          public Uni<Tenant> insert(Tenant newRepo) {
            final var next = dataSource.withTenant(newRepo);
            final var sqlQuery = new TenantRegistrySqlImpl(next.getRegistry());
            final var pool = next.getPool();
            
            return pool.withTransaction(tx -> {
              final var tenantInsert = sqlQuery.insertOne(newRepo);
              
              final Uni<Void> create = getClient().query(sqlQuery.createTable().getValue()).execute()
                  .onItem().transformToUni(data -> Uni.createFrom().voidItem())
                  .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlFailed("Can't create table 'TENANT'!", sqlQuery.createTable(), e)));
              
              final Uni<Void> insert = tx.preparedQuery(tenantInsert.getValue()).execute(tenantInsert.getProps())
                  .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
                  .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlTupleFailed("Can't insert into 'TENANT'!", tenantInsert, e)));
              
              return create
                  .onItem().transformToUni((junk) -> insert)
                  .onItem().transform(junk -> newRepo)
                  .onItem().invoke(newTenant -> this.dataSource.getTenantCache().setTenant(newTenant));
            });
          }};
      }
      @Override
      public InternalAliasQuery alias() {
        return new InternalAliasQueryImpl(source);
      }
      @Override
      public InternalMemberQuery member() {
        return new InternalMemberQueryImpl(source);
      }
    };
    this.pgPool = pgPool;
    this.client = new TenantActionsImpl(db);
  }

  private void waitUntilPostgresqlAcceptsConnections(Pool pool) {
    // On some platforms there may be some delay before postgresql starts to respond.
    // Try until postgresql connection is successfully opened.
    var connection = pool.getConnection()
      .onFailure()
      .retry().withBackOff(Duration.ofMillis(10), Duration.ofSeconds(3)).atMost(20)
      .await().atMost(Duration.ofSeconds(60));
    connection.closeAndForget();
  }

  @AfterEach
  public void tearDown() { }

  public static String toString(Class<?> type, String resource) {
    try {
      return new String(type.getClassLoader().getResourceAsStream(resource).readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public TenantActionsImpl getClient() {
    return client;
  }
}
