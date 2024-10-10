package io.resys.thena.storesql;

import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.storesql.builders.OrgMemberQuerySqlPool;
import io.resys.thena.storesql.builders.OrgMemberRightsQueryImpl;
import io.resys.thena.storesql.builders.OrgMembershipsQuerySqlPool;
import io.resys.thena.storesql.builders.OrgPartyQuerySqlPool;
import io.resys.thena.storesql.builders.OrgPartyRightsQuerySqlPool;
import io.resys.thena.storesql.builders.OrgRightsQuerySqlPool;
import io.resys.thena.structures.org.OrgQueries;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgDbQueriesSqlImpl implements OrgQueries {
  private final ThenaSqlDataSource dataSource;
  
  @Override
  public MemberQuery members() {
    return new OrgMemberQuerySqlPool(dataSource);
  }
  @Override
  public PartyQuery parties() {
    return new OrgPartyQuerySqlPool(dataSource);
  }
  @Override
  public RightsQuery rights() {
    return new OrgRightsQuerySqlPool(dataSource);
  }
	@Override
	public MembershipQuery memberships() {
    return new OrgMembershipsQuerySqlPool(dataSource);
	}
	@Override
	public MemberRightsQuery memberRights() {
    return new OrgMemberRightsQueryImpl(dataSource);
	}
	@Override
	public PartyRightsQuery partyRights() {
    return new OrgPartyRightsQuerySqlPool(dataSource);
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
