package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlSchemaFailed;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlTupleFailed;
import io.resys.thena.spi.DbState.InternalTenantQuery;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class InternalTenantQueryImpl implements InternalTenantQuery {
  private final ThenaSqlDataSource dataSource;
  
  private ThenaSqlClient getClient() {
    return dataSource.getClient();
  }

  @Override
  public Uni<Tenant> getByName(String name) {
    final var sql = dataSource.getRegistry().tenant().getByName(name);
    if(log.isDebugEnabled()) {
      log.debug("Repo by name query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().tenant().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Tenant> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> {
          
          
          dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'REPOS' by 'name'!", sql, e));
        });
  }

  @Override
  public Uni<Tenant> getByNameOrId(String nameOrId) {
    final var sql = dataSource.getRegistry().tenant().getByNameOrId(nameOrId);
    
    if(log.isDebugEnabled()) {
      log.debug("Repo by nameOrId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().tenant().defaultMapper())
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<Tenant> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlTupleFailed("Can't find 'REPOS' by 'name' or 'id'!", sql, e)));
  }
  
  @Override
  public Uni<Tenant> insert(final Tenant newRepo) {
    final var next = dataSource.withTenant(newRepo);
    final var git = next.getRegistry().git();
    final var doc = next.getRegistry().doc();
    final var org = next.getRegistry().org();
    final var grim = next.getRegistry().grim();
    final var sqlQuery = next.getRegistry();
    final var pool = next.getPool();
    
    return pool.withTransaction(tx -> {
      final var tenantInsert = sqlQuery.tenant().insertOne(newRepo);
      final var tablesCreate = new StringBuilder();
      
      if(newRepo.getType() == StructureType.git) {
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
      } else if(newRepo.getType() == StructureType.grim) {
        tablesCreate
        .append(grim.commands().createTable().getValue())
        .append(grim.assignments().createTable().getValue())
        .append(grim.commits().createTable().getValue())
        .append(grim.commitTrees().createTable().getValue())
        .append(grim.commitViewers().createTable().getValue())
        .append(grim.missionData().createTable().getValue())
        .append(grim.missionLabels().createTable().getValue())
        .append(grim.missionLinks().createTable().getValue())
        .append(grim.missions().createTable().getValue())
        .append(grim.goals().createTable().getValue())
        .append(grim.objectives().createTable().getValue())
        .append(grim.remarks().createTable().getValue())

        .append(grim.commands().createConstraints().getValue())
        .append(grim.assignments().createConstraints().getValue())
        .append(grim.commits().createConstraints().getValue())
        .append(grim.commitTrees().createConstraints().getValue())
        .append(grim.commitViewers().createConstraints().getValue())
        .append(grim.missionData().createConstraints().getValue())
        .append(grim.missionLabels().createConstraints().getValue())
        .append(grim.missionLinks().createConstraints().getValue())
        .append(grim.missions().createConstraints().getValue())
        .append(grim.goals().createConstraints().getValue())
        .append(grim.objectives().createConstraints().getValue())
        .append(grim.remarks().createConstraints().getValue())
        ;
      } else if(newRepo.getType() == StructureType.org) {
        
        tablesCreate
          .append(org.orgRights().createTable().getValue())
          .append(org.orgParties().createTable().getValue())
          .append(org.orgPartyRights().createTable().getValue())
          
          .append(org.orgMembers().createTable().getValue())
          .append(org.orgMemberRights().createTable().getValue())
          .append(org.orgMemberships().createTable().getValue())

          .append(org.orgActorStatus().createTable().getValue())
          .append(org.orgCommits().createTable().getValue())
          .append(org.orgActorData().createTable().getValue())
          
          .append(org.orgRights().createConstraints().getValue())
          .append(org.orgMembers().createConstraints().getValue())
          .append(org.orgParties().createConstraints().getValue())
          .append(org.orgCommits().createConstraints().getValue())
          
          .toString();
        
      } else  {
        tablesCreate
          .append(doc.docs().createTable().getValue())
          .append(doc.docBranches().createTable().getValue())
          .append(doc.docCommits().createTable().getValue())
          .append(doc.docLogs().createTable().getValue())
  
          .append(doc.docBranches().createConstraints().getValue())
          .append(doc.docCommits().createConstraints().getValue())
          .append(doc.docLogs().createConstraints().getValue())
          .toString();
      }
      
      if(log.isDebugEnabled()) {
        log.debug(new StringBuilder("Creating schema: ")
            .append(System.lineSeparator())
            .append(tablesCreate.toString())
            .toString());
      }
      
      final Uni<Void> create = getClient().query(dataSource.getRegistry().tenant().createTable().getValue()).execute()
          .onItem().transformToUni(data -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlFailed("Can't create table 'TENANT'!", dataSource.getRegistry().tenant().createTable(), e)));
      
      
      final Uni<Void> insert = tx.preparedQuery(tenantInsert.getValue()).execute(tenantInsert.getProps())
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlTupleFailed("Can't insert into 'TENANT'!", tenantInsert, e)));
      final Uni<Void> nested = tx.query(tablesCreate.toString()).execute()
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlSchemaFailed("Can't create tables!", tablesCreate.toString(), e)));
      
      return create
          .onItem().transformToUni((junk) -> insert)
          .onItem().transformToUni((junk) -> nested)
          .onItem().transform(junk -> newRepo);
    });
  }

  @Override
  public Multi<Tenant> findAll() {
    final var sql = this.dataSource.getRegistry().tenant().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Fina all tenants query, with props: {} \r\n{}", 
          "", 
          sql.getValue());
    }
    
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(dataSource.getRegistry().tenant().defaultMapper())
    .execute()
    .onItem()
    .transformToMulti((RowSet<Tenant> rowset) -> Multi.createFrom().iterable(rowset))
    .onFailure(e -> dataSource.getErrorHandler().notFound(e)).recoverWithCompletion()
    .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlFailed("Can't find 'REPOS'!", sql, e)));
  }
  
  
  @Override
  public Uni<Tenant> delete(final Tenant newRepo) {
    final var next = dataSource.withTenant(newRepo);
    final var git = next.getRegistry().git();
    final var doc = next.getRegistry().doc();
    final var org = next.getRegistry().org();
    final var grim = next.getRegistry().grim();
    
    final var sqlQuery = next.getRegistry();
    final var pool = next.getPool();
    return pool.withTransaction(tx -> {
      final var tenantDelete = sqlQuery.tenant().deleteOne(newRepo);
      final var tablesDrop = new StringBuilder();
      
      if(newRepo.getType() == StructureType.git) {
        tablesDrop
        .append(git.branches().dropTable().getValue())
        .append(git.tags().dropTable().getValue())
        .append(git.commits().dropTable().getValue())
        .append(git.treeValues().dropTable().getValue())
        .append(git.trees().dropTable().getValue())
        .append(git.blobs().dropTable().getValue());

      } else if(newRepo.getType() == StructureType.grim) {
        tablesDrop
        .append(grim.assignments().dropTable().getValue())
        .append(grim.commits().dropTable().getValue())
        .append(grim.commitTrees().dropTable().getValue())
        .append(grim.commitViewers().dropTable().getValue())
        .append(grim.missionData().dropTable().getValue())
        .append(grim.missionLabels().dropTable().getValue())
        .append(grim.missionLinks().dropTable().getValue())
        .append(grim.missions().dropTable().getValue())
        .append(grim.goals().dropTable().getValue())
        .append(grim.objectives().dropTable().getValue())
        .append(grim.remarks().dropTable().getValue())
        .append(grim.commands().dropTable().getValue())
        ;
      } else if(newRepo.getType() == StructureType.org) {
        
        tablesDrop
        .append(org.orgActorData().dropTable().getValue())
        .append(org.orgActorStatus().dropTable().getValue())
        .append(org.orgPartyRights().dropTable().getValue())
        .append(org.orgMemberRights().dropTable().getValue())
        .append(org.orgMemberships().dropTable().getValue())

        .append(org.orgMembers().dropTable().getValue())
        .append(org.orgParties().dropTable().getValue())
        .append(org.orgRights().dropTable().getValue())
        
        .append(org.orgCommitTrees().dropTable().getValue())
        .append(org.orgCommits().dropTable().getValue());
        
        
      } else {
        tablesDrop
        .append(doc.docLogs().dropTable().getValue())
        .append(doc.docCommits().dropTable().getValue())
        .append(doc.docBranches().dropTable().getValue())
        .append(doc.docs().dropTable().getValue());        
        
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
          .onItem().transform(junk -> newRepo);
    });
  }

  @Override
  public Uni<Void> delete() {
    final var tenantDelete = dataSource.getRegistry().tenant().dropTable();
    final var pool = dataSource.getPool();
    return  pool.query(tenantDelete.getValue()).execute()
        .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
        .onFailure().invoke(e -> dataSource.getErrorHandler().deadEnd(new SqlSchemaFailed("Can't drop tenant table!", tenantDelete.getValue(), e)));
    
  }
}
