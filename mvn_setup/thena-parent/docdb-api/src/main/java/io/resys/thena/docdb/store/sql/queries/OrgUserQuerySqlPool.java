package io.resys.thena.docdb.store.sql.queries;

import java.util.List;

import io.resys.thena.docdb.api.LogConstants;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberHierarchyEntry;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgRightFlattened;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMember;
import io.resys.thena.docdb.api.models.ThenaOrgObject.OrgMemberFlattened;
import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.store.sql.SqlBuilder;
import io.resys.thena.docdb.store.sql.SqlMapper;
import io.resys.thena.docdb.store.sql.support.SqlClientWrapper;
import io.resys.thena.docdb.support.ErrorHandler;
import io.resys.thena.docdb.support.ErrorHandler.SqlFailed;
import io.resys.thena.docdb.support.ErrorHandler.SqlTupleFailed;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j(topic = LogConstants.SHOW_SQL)
@RequiredArgsConstructor
public class OrgUserQuerySqlPool implements OrgQueries.UserQuery {
  private final SqlClientWrapper wrapper;
  private final SqlMapper sqlMapper;
  private final SqlBuilder sqlBuilder;
  private final ErrorHandler errorHandler;

  @Override
  public Multi<OrgMember> findAll() {
    final var sql = sqlBuilder.orgUsers().findAll();
    if(log.isDebugEnabled()) {
      log.debug("User findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMember(row))
        .execute()
        .onItem()
        .transformToMulti((RowSet<OrgMember> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlFailed("Can't find 'USER'!", sql, e)));
  }
  
  @Override
  public Multi<OrgMember> findAll(List<String> id) {
    final var sql = sqlBuilder.orgUsers().findAll(id);
    if(log.isDebugEnabled()) {
      log.debug("User findAll query, with props: {} \r\n{}", 
          "",
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMember(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMember> rowset) -> Multi.createFrom().iterable(rowset))
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'USER'!", sql, e)));
  }


  @Override
  public Uni<OrgMember> getById(String id) {
    final var sql = sqlBuilder.orgUsers().getById(id);
    if(log.isDebugEnabled()) {
      log.debug("User byId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMember(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgMember> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure(e -> errorHandler.notFound(e)).recoverWithNull()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't get 'USER' by 'id': '" + id + "'!", sql, e)));
  }
  
	@Override
	public Uni<List<OrgMemberHierarchyEntry>> findAllUserHierarchyEntries(String userId) {
    final var sql = sqlBuilder.orgUsers().findAllUserGroupsAndRolesByUserId(userId);
    if(log.isDebugEnabled()) {
      log.debug("User findAllUserHierarchyEntries query, with props: {} \r\n{}", 
      		sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMemberHierarchyEntry(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgMemberHierarchyEntry> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'USER_GROUPS_ROLES'!", sql, e)));
	}

  @Override
  public Uni<List<OrgRightFlattened>> findAllRolesByUserId(String userId) {
    final var sql = sqlBuilder.orgUsers().findAllRolesByUserId(userId);
    if(log.isDebugEnabled()) {
      log.debug("User findAllRolesByUserId query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgOrgRightFlattened(row))
        .execute(sql.getProps())
        .onItem()
        .transformToMulti((RowSet<OrgRightFlattened> rowset) -> Multi.createFrom().iterable(rowset))
        .collect().asList()
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'USER_DIRECT_ROLES'!", sql, e)));
  }

  @Override
  public Uni<OrgMemberFlattened> getStatusById(String userId) {
    final var sql = sqlBuilder.orgUsers().getStatusByUserId(userId);
    if(log.isDebugEnabled()) {
      log.debug("User getStatusById query, with props: {} \r\n{}", 
          sql.getProps().deepToString(),
          sql.getValue());
    }
    return wrapper.getClient().preparedQuery(sql.getValue())
        .mapping(row -> sqlMapper.orgMemberFlattened(row))
        .execute(sql.getProps())
        .onItem()
        .transform((RowSet<OrgMemberFlattened> rowset) -> {
          final var it = rowset.iterator();
          if(it.hasNext()) {
            return it.next();
          }
          return null;
        })
        .onFailure().invoke(e -> errorHandler.deadEnd(new SqlTupleFailed("Can't find 'USER_STATUS'!", sql, e)));
  }
}
