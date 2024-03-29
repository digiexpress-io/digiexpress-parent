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
    final var sql = dataSource.getQueryBuilder().repo().getByName(name);
    if(log.isDebugEnabled()) {
      log.debug("Repo by name query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(row -> dataSource.getDataMapper().repo(row))
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
    final var sql = dataSource.getQueryBuilder().repo().getByNameOrId(nameOrId);
    
    if(log.isDebugEnabled()) {
      log.debug("Repo by nameOrId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(), 
          sql.getValue());
    }
    
    
    return getClient().preparedQuery(sql.getValue())
        .mapping(row -> dataSource.getDataMapper().repo(row))
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
    final var sqlSchema = next.getSchema();
    final var sqlQuery = next.getQueryBuilder();
    final var pool = next.getPool();
    
    return pool.withTransaction(tx -> {
      final var repoInsert = sqlQuery.repo().insertOne(newRepo);
      final var tablesCreate = new StringBuilder();
      
      if(newRepo.getType() == StructureType.git) {
        tablesCreate.append(git.blobs().createTable().getValue())
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
      
      final Uni<Void> create = getClient().query(sqlSchema.createTenant().getValue()).execute()
          .onItem().transformToUni(data -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlFailed("Can't create table 'TENANT'!", sqlSchema.createTenant(), e)));
      
      
      final Uni<Void> insert = tx.preparedQuery(repoInsert.getValue()).execute(repoInsert.getProps())
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlTupleFailed("Can't insert into 'TENANT'!", repoInsert, e)));
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
    final var sql = this.dataSource.getQueryBuilder().repo().findAll();
    if(log.isDebugEnabled()) {
      log.debug("Fina all repos query, with props: {} \r\n{}", 
          "", 
          sql.getValue());
    }
    
    
    return getClient().preparedQuery(sql.getValue())
    .mapping(row -> dataSource.getDataMapper().repo(row))
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
    
    final var sqlQuery = next.getQueryBuilder();
    final var pool = next.getPool();
    return pool.withTransaction(tx -> {
      final var repoDelete = sqlQuery.repo().deleteOne(newRepo);
      final var tablesDrop = new StringBuilder();
      
      if(newRepo.getType() == StructureType.git) {
        tablesDrop
        .append(git.branches().dropTable().getValue())
        .append(git.tags().dropTable().getValue())
        .append(git.commits().dropTable().getValue())
        .append(git.treeValues().dropTable().getValue())
        .append(git.trees().dropTable().getValue())
        .append(git.blobs().dropTable().getValue());
        
      } else if(newRepo.getType() == StructureType.org) {
        
        tablesDrop
        .append(org.orgActorData().dropTable().getValue())
        .append(org.orgActorStatus().dropTable().getValue())
        
        .append(org.orgRights().dropTable().getValue())
        .append(org.orgParties().dropTable().getValue())
        .append(org.orgPartyRights().dropTable().getValue())

        .append(org.orgMembers().dropTable().getValue())
        .append(org.orgMemberRights().dropTable().getValue())
        .append(org.orgMemberships().dropTable().getValue())
        
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
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlTupleFailed("Can't delete from 'REPO'!", repoDelete, e)));
      final Uni<Void> nested = tx.query(tablesDrop.toString()).execute()
          .onItem().transformToUni(rowSet -> Uni.createFrom().voidItem())
          .onFailure().invoke(e -> next.getErrorHandler().deadEnd(new SqlSchemaFailed("Can't drop tables!", tablesDrop.toString(), e)));
      
      return insert
          .onItem().transformToUni(junk -> nested)
          .onItem().transform(junk -> newRepo);
    });
  }
}
