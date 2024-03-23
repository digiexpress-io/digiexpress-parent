package io.resys.thena.docdb.storesql;

import io.resys.thena.docdb.models.org.OrgQueries;
import io.resys.thena.docdb.storesql.GitDbQueriesSqlImpl.ClientQuerySqlContext;
import io.resys.thena.docdb.storesql.builders.OrgActorStatusQuerySqlPool;
import io.resys.thena.docdb.storesql.builders.OrgGroupQuerySqlPool;
import io.resys.thena.docdb.storesql.builders.OrgGroupRoleQuerySqlPool;
import io.resys.thena.docdb.storesql.builders.OrgRoleQuerySqlPool;
import io.resys.thena.docdb.storesql.builders.OrgUserMembershipsQuerySqlPool;
import io.resys.thena.docdb.storesql.builders.OrgUserQuerySqlPool;
import io.resys.thena.docdb.storesql.builders.OrgMemberRightsQueryImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgDbQueriesSqlImpl implements OrgQueries {
  
  protected final ClientQuerySqlContext context;

  @Override
  public MemberQuery members() {
    return new OrgUserQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
  @Override
  public PartyQuery parties() {
    return new OrgGroupQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
  @Override
  public RightsQuery rights() {
    return new OrgRoleQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
	@Override
	public MembershipQuery memberships() {
    return new OrgUserMembershipsQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
	}
	@Override
	public MemberRightsQuery memberRights() {
    return new OrgMemberRightsQueryImpl(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
	}
	@Override
	public PartyRightsQuery partyRights() {
    return new OrgGroupRoleQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
	}
  @Override
  public CommitQuery commits() {
    return null;
  }
  @Override
  public ActorStatusQuery actorStatus() {
    return new OrgActorStatusQuerySqlPool(context.getWrapper(), context.getMapper(), context.getBuilder(), context.getErrorHandler());
  }
}
