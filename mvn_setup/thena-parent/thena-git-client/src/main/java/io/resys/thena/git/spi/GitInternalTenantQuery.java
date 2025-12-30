package io.resys.thena.git.spi;

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



import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlSchemaFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.git.spi.sql.GitRegistrySqlImpl;
import io.resys.thena.spi.InternalTenantQueryImpl;
import io.resys.thena.spi.TenantRegistrySqlImpl;
import io.resys.thena.spi.TenantDataSource.InternalTenantQuery;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class GitInternalTenantQuery extends InternalTenantQueryImpl implements InternalTenantQuery {

  public GitInternalTenantQuery(ThenaSqlDataSource dataSource) {
    super(dataSource);
  }

  @Override
  public Uni<Tenant> insert(final Tenant newRepo) {
    RepoAssert.isTrue(newRepo.getType() == StructureType.git, () -> "Tenant type must be git");

    final var next = dataSource.withTenant(newRepo);
    final var git = new GitRegistrySqlImpl(next.getRegistry());
    final var sqlQuery = new TenantRegistrySqlImpl(next.getRegistry());
    final var pool = next.getPool();
    
    return pool.withTransaction(tx -> {
      final var tenantInsert = sqlQuery.insertOne(newRepo);
      final var tablesCreate = new StringBuilder();
      tablesCreate
        .append(git.blobs().createTable().getValue())
        .append(git.commits().createTable().getValue())
        .append(git.treeValues().createTable().getValue())
        .append(git.trees().createTable().getValue())
        .append(git.branches().createTable().getValue())
        .append(git.tags().createTable().getValue())
        
        .append(git.commits().createConstraints().getValue())
        .append(git.branches().createConstraints().getValue())
        .append(git.tags().createConstraints().getValue())
        .append(git.trees().createConstraints().getValue())
        .append(git.treeValues().createConstraints().getValue())
        .toString();
      
      if(log.isDebugEnabled()) {
        log.debug(new StringBuilder("Creating schema: ")
            .append(System.lineSeparator())
            .append(tablesCreate.toString())
            .toString());
      }
      
      final Uni<Void> create = getClient().query(sqlQuery.createTable().getValue()).execute()
          .onItem().transformToUni(data -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlFailed("Can't create table 'TENANT'!", sqlQuery.createTable(), e)));
      
      
      final Uni<Void> insert = tx.preparedQuery(tenantInsert.getValue()).execute(tenantInsert.getProps())
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlTupleFailed("Can't insert into 'TENANT'!", tenantInsert, e)));
      final Uni<Void> nested = tx.query(tablesCreate.toString()).execute()
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlSchemaFailed("Can't create tables!", tablesCreate.toString(), e)));
      
      return create
          .onItem().transformToUni((junk) -> insert)
          .onItem().transformToUni((junk) -> nested)
          .onItem().transform(junk -> newRepo)
          .onItem().invoke(newTenant -> this.dataSource.getTenantCache().setTenant(newTenant));
    });
  }


  @Override
  public Uni<Tenant> delete(final Tenant newRepo) {
    RepoAssert.isTrue(newRepo.getType() == StructureType.git, () -> "Tenant type must be git");
    final var next = dataSource.withTenant(newRepo);
    final var git = new GitRegistrySqlImpl(next.getRegistry());
    

    final var sqlQuery = new TenantRegistrySqlImpl(next.getRegistry());
    
    final var pool = next.getPool();
    return pool.withTransaction(tx -> {
      final var tenantDelete = sqlQuery.deleteOne(newRepo);
      final var tablesDrop = new StringBuilder();
      
      tablesDrop
        .append(git.branches().dropTable().getValue())
        .append(git.tags().dropTable().getValue())
        .append(git.commits().dropTable().getValue())
        .append(git.treeValues().dropTable().getValue())
        .append(git.trees().dropTable().getValue())
        .append(git.blobs().dropTable().getValue());
      
      
      if(log.isDebugEnabled()) {
        log.debug("Delete tenant by name query, with props: {} \r\n{}", 
            tenantDelete.getProps().deepToString(), 
            tenantDelete.getValue());
        
        
        log.debug(new StringBuilder("Drop schema: ")
            .append(System.lineSeparator())
            .append(tablesDrop.toString())
            .toString());
      }
      
      
      final Uni<Void> insert = tx.preparedQuery(tenantDelete.getValue()).execute(tenantDelete.getProps())
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlTupleFailed("Can't delete from 'REPO'!", tenantDelete, e)));
      final Uni<Void> nested = tx.query(tablesDrop.toString()).execute()
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlSchemaFailed("Can't drop tables!", tablesDrop.toString(), e)));
      
      return insert
          .onItem().transformToUni(junk -> nested)
          .onItem().transform(junk -> newRepo)
          .onItem().invoke(() -> this.dataSource.getTenantCache().invalidateAll());
    });
  }
}
