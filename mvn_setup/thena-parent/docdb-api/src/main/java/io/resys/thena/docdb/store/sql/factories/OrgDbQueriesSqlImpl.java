package io.resys.thena.docdb.store.sql.factories;

import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.store.sql.factories.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.resys.thena.docdb.store.sql.queries.OrgGroupQuerySqlPool;
import io.resys.thena.docdb.store.sql.queries.OrgGroupRoleQuerySqlPool;
import io.resys.thena.docdb.store.sql.queries.OrgRoleQuerySqlPool;
import io.resys.thena.docdb.store.sql.queries.OrgUserMembershipsQuerySqlPool;
import io.resys.thena.docdb.store.sql.queries.OrgUserQuerySqlPool;
import io.resys.thena.docdb.store.sql.queries.OrgUserRoleQuerySqlPool;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgDbQueriesSqlImpl implements OrgQueries {
  
  protected final ClientQuerySqlContext context;

  @Override
  public UserQuery users() {
    return new OrgUserQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
  @Override
  public GroupQuery groups() {
    return new OrgGroupQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
  @Override
  public RoleQuery roles() {
    return new OrgRoleQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
	@Override
	public UserMembershipQuery userMemberships() {
    return new OrgUserMembershipsQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
	}
	@Override
	public UserRolesQuery userRoles() {
    return new OrgUserRoleQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
	}
	@Override
	public GroupRolesQuery groupRoles() {
    return new OrgGroupRoleQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
	}
  @Override
  public CommitQuery commits() {
    return null;
  }
}
