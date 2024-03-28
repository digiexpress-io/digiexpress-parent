package io.resys.thena.storesql.builders;

import io.resys.thena.api.LogConstants;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
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
public class RepoBuilderSqlPool implements InternalTenantQuery {
  private final ThenaSqlDataSource dataSource;
  
  private io.vertx.mutiny.sqlclient.SqlClient getClient() {
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
    final var sqlSchema = next.getSchema();
    final var sqlQuery = next.getQueryBuilder();
    final var pool = next.getPool();
    return pool.withTransaction(tx -> {
      final var repoInsert = sqlQuery.repo().insertOne(newRepo);
      final var tablesCreate = new StringBuilder();
      
      if(newRepo.getType() == StructureType.git) {
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
        
      } else if(newRepo.getType() == StructureType.org) {
        
        tablesCreate
          .append(sqlSchema.createOrgRights().getValue())
          .append(sqlSchema.createOrgParties().getValue())
          .append(sqlSchema.createOrgPartyRights().getValue())
          
          .append(sqlSchema.createOrgMembers().getValue())
          .append(sqlSchema.createOrgMemberRights().getValue())
          .append(sqlSchema.createOrgMemberships().getValue())
          

          .append(sqlSchema.createOrgActorStatus().getValue())
          .append(sqlSchema.createOrgCommits().getValue())
          .append(sqlSchema.createOrgActorData().getValue())
          
          .append(sqlSchema.createOrgRightsConstraints().getValue())
          .append(sqlSchema.createOrgMemberConstraints().getValue())
          .append(sqlSchema.createOrgPartyConstraints().getValue())
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
    final var sqlSchema = next.getSchema();
    final var sqlQuery = next.getQueryBuilder();
    final var pool = next.getPool();
    return pool.withTransaction(tx -> {
      final var repoDelete = sqlQuery.repo().deleteOne(newRepo);
      final var tablesDrop = new StringBuilder();
      
      if(newRepo.getType() == StructureType.git) {
        tablesDrop
        .append(sqlSchema.dropGitRefs().getValue())
        .append(sqlSchema.dropGitTags().getValue())
        .append(sqlSchema.dropGitCommits().getValue())
        .append(sqlSchema.dropGitTreeItems().getValue())
        .append(sqlSchema.dropGitTrees().getValue())
        .append(sqlSchema.dropGitBlobs().getValue());
        
      } else if(newRepo.getType() == StructureType.org) {
        
        tablesDrop
        .append(sqlSchema.dropOrgRights().getValue())
        .append(sqlSchema.dropOrgParties().getValue())
        .append(sqlSchema.dropOrgPartyRights().getValue())

        .append(sqlSchema.dropOrgMembers().getValue())
        .append(sqlSchema.dropOrgMemberRights().getValue())
        .append(sqlSchema.dropOrgMemberships().getValue())
        
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
