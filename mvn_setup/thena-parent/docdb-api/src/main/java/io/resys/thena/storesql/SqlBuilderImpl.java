package io.resys.thena.storesql;

import io.resys.thena.datasource.SqlQueryBuilder;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.storesql.statement.OrgActorStatusSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgCommitSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgCommitTreeSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgMemberRightSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgMemberSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgMembershipsSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgPartyRightSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgPartySqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgRightSqlBuilderImpl;
import io.resys.thena.storesql.statement.RepoSqlBuilderImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlBuilderImpl implements SqlQueryBuilder {
  protected final TenantTableNames ctx;

  @Override
  public TenantSqlBuilder repo() {
    return new RepoSqlBuilderImpl(ctx);
  }
  @Override
  public SqlQueryBuilder withTenant(TenantTableNames options) {
    return new SqlBuilderImpl(options);
  }
  @Override
  public OrgMemberSqlBuilder orgMembers() {
    return new OrgMemberSqlBuilderImpl(ctx);
  }
  @Override
  public OrgPartySqlBuilder orgParties() {
    return new OrgPartySqlBuilderImpl(ctx);
  }
  @Override
  public OrgCommitSqlBuilder orgCommits() {
    return new OrgCommitSqlBuilderImpl(ctx);
  }
  @Override
  public OrgCommitTreeSqlBuilder orgCommitTrees() {
    return new OrgCommitTreeSqlBuilderImpl(ctx);
  }
	@Override
	public OrgMembershipsSqlBuilder orgMemberships() {
		return new OrgMembershipsSqlBuilderImpl(ctx);
	}
	@Override
	public OrgRightSqlBuilder orgRights() {
		return new OrgRightSqlBuilderImpl(ctx);
	}
	@Override
	public OrgMemberRightSqlBuilder orgMemberRights() {
		return new OrgMemberRightSqlBuilderImpl(ctx);
	}
	@Override
	public OrgPartyRightSqlBuilder orgPartyRights() {
		return new OrgPartyRightSqlBuilderImpl(ctx);
	}
  @Override
  public OrgActorStatusSqlBuilder orgActorStatus() {
    return new OrgActorStatusSqlBuilderImpl(ctx);
  }
}
