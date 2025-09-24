package io.resys.thena.storesql;

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
import io.resys.thena.registry.TenantRegistrySqlImpl;
import io.resys.thena.registry.fs.FsRegistrySqlImpl;
import io.resys.thena.registry.org.OrgRegistrySqlImpl;
import io.resys.thena.spi.InternalTenantQueryImpl;
import io.resys.thena.spi.TenantDataSource.InternalTenantQuery;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
public class DbStateTenantQuery extends InternalTenantQueryImpl implements InternalTenantQuery {

  public DbStateTenantQuery(ThenaSqlDataSource dataSource) {
    super(dataSource);
  }

  @Override
  public Uni<Tenant> insert(final Tenant newRepo) {
    final var next = dataSource.withTenant(newRepo);
    final var org = new OrgRegistrySqlImpl(next.getRegistry());
    final var fs = new FsRegistrySqlImpl(next.getRegistry());
    final var sqlQuery = new TenantRegistrySqlImpl(next.getRegistry());
    final var pool = next.getPool();
    
    return pool.withTransaction(tx -> {
      final var tenantInsert = sqlQuery.insertOne(newRepo);
      final var tablesCreate = new StringBuilder();
      
      if(newRepo.getType() == StructureType.git) {

      } else if(newRepo.getType() == StructureType.grim) {

      } else if(newRepo.getType() == StructureType.org) {
        
        tablesCreate
          .append(org.orgRights().createTable().getValue())
          .append(org.orgParties().createTable().getValue())
          .append(org.orgPartyRights().createTable().getValue())
          
          .append(org.orgMembers().createTable().getValue())
          .append(org.orgMemberRights().createTable().getValue())
          .append(org.orgMemberships().createTable().getValue())
          .append(org.orgCommits().createTable().getValue())
          .append(org.orgCommitTrees().createTable().getValue())
          
          .append(org.orgRights().createConstraints().getValue())
          .append(org.orgMembers().createConstraints().getValue())
          .append(org.orgParties().createConstraints().getValue())
          .append(org.orgCommits().createConstraints().getValue())
          .append(org.orgCommitTrees().createConstraints().getValue());
        
        
      } else if(newRepo.getType() == StructureType.fs) {
        
        tablesCreate

        .append(fs.direntAssignments().createTable().getValue())
        .append(fs.commits().createTable().getValue())
        .append(fs.commitTrees().createTable().getValue())
        .append(fs.direntData().createTable().getValue())
        .append(fs.direntLabels().createTable().getValue())
        .append(fs.direntLinks().createTable().getValue())
        .append(fs.dirents().createTable().getValue())
        .append(fs.direntRemarks().createTable().getValue())

        .append(fs.direntAssignments().createConstraints().getValue())
        .append(fs.commits().createConstraints().getValue())
        .append(fs.commitTrees().createConstraints().getValue())
        
        .append(fs.direntData().createConstraints().getValue())
        .append(fs.direntLabels().createConstraints().getValue())
        .append(fs.direntLinks().createConstraints().getValue())
        .append(fs.dirents().createConstraints().getValue())
        .append(fs.direntRemarks().createConstraints().getValue());
        
        
      } 
      
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
    final var next = dataSource.withTenant(newRepo);
    
    final var org = new OrgRegistrySqlImpl(next.getRegistry());
    final var fs = new FsRegistrySqlImpl(next.getRegistry());
    final var sqlQuery = new TenantRegistrySqlImpl(next.getRegistry());
    
    final var pool = next.getPool();
    return pool.withTransaction(tx -> {
      final var tenantDelete = sqlQuery.deleteOne(newRepo);
      final var tablesDrop = new StringBuilder();
      
      if(newRepo.getType() == StructureType.git) {

      } else if(newRepo.getType() == StructureType.grim) {

      } else if(newRepo.getType() == StructureType.fs) {
        tablesDrop
        .append(fs.direntAssignments().dropTable().getValue())
        .append(fs.direntData().dropTable().getValue())
        .append(fs.direntLabels().dropTable().getValue())
        .append(fs.direntLinks().dropTable().getValue())
        .append(fs.direntRemarks().dropTable().getValue())

        
        .append(fs.commitTrees().dropTable().getValue())
        .append(fs.dirents().dropTable().getValue())
        .append(fs.commits().dropTable().getValue())
        
        ;
      } else if(newRepo.getType() == StructureType.org) {
        
        tablesDrop
        .append(org.orgPartyRights().dropTable().getValue())
        .append(org.orgMemberRights().dropTable().getValue())
        .append(org.orgMemberships().dropTable().getValue())

        .append(org.orgMembers().dropTable().getValue())
        .append(org.orgParties().dropTable().getValue())
        .append(org.orgRights().dropTable().getValue())
        
        .append(org.orgCommitTrees().dropTable().getValue())
        .append(org.orgCommits().dropTable().getValue());
        
        
      } 
      
      
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
