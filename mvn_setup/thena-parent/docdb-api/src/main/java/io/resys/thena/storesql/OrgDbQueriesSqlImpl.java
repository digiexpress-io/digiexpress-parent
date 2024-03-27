package io.resys.thena.storesql;

import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.builders.OrgActorStatusQuerySqlPool;
import io.resys.thena.storesql.builders.OrgGroupQuerySqlPool;
import io.resys.thena.storesql.builders.OrgGroupRoleQuerySqlPool;
import io.resys.thena.storesql.builders.OrgMemberRightsQueryImpl;
import io.resys.thena.storesql.builders.OrgRoleQuerySqlPool;
import io.resys.thena.storesql.builders.OrgUserMembershipsQuerySqlPool;
import io.resys.thena.storesql.builders.OrgUserQuerySqlPool;
import io.resys.thena.structures.org.OrgQueries;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgDbQueriesSqlImpl implements OrgQueries {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public MemberQuery members() {
    return new OrgUserQuerySqlPool(dataSource);
  }
  @Override
  public PartyQuery parties() {
    return new OrgGroupQuerySqlPool(dataSource);
  }
  @Override
  public RightsQuery rights() {
    return new OrgRoleQuerySqlPool(dataSource);
  }
	@Override
	public MembershipQuery memberships() {
    return new OrgUserMembershipsQuerySqlPool(dataSource);
	}
	@Override
	public MemberRightsQuery memberRights() {
    return new OrgMemberRightsQueryImpl(dataSource);
	}
	@Override
	public PartyRightsQuery partyRights() {
    return new OrgGroupRoleQuerySqlPool(dataSource);
	}
  @Override
  public ActorStatusQuery actorStatus() {
    return new OrgActorStatusQuerySqlPool(dataSource);
  }
  @Override
  public CommitQuery commits() {
    return null;
  }
  @Override
  public ThenaDataSource getDataSource() {
    return dataSource;
  }
}
