package io.resys.thena.storesql;

import io.resys.thena.spi.DbCollections;
import io.resys.thena.storesql.statement.DocBranchSqlBuilderImpl;
import io.resys.thena.storesql.statement.DocCommitSqlBuilderImpl;
import io.resys.thena.storesql.statement.DocLogSqlBuilderImpl;
import io.resys.thena.storesql.statement.DocSqlBuilderImpl;
import io.resys.thena.storesql.statement.GitBlobSqlBuilderImpl;
import io.resys.thena.storesql.statement.GitCommitSqlBuilderImpl;
import io.resys.thena.storesql.statement.GitRefSqlBuilderImpl;
import io.resys.thena.storesql.statement.GitTagSqlBuilderImpl;
import io.resys.thena.storesql.statement.GitTreeItemSqlBuilderImpl;
import io.resys.thena.storesql.statement.GitTreeSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgActorStatusSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgCommitSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgCommitTreeSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgGroupRoleSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgGroupSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgRoleSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgUserMembershipsSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgUserRoleSqlBuilderImpl;
import io.resys.thena.storesql.statement.OrgUserSqlBuilderImpl;
import io.resys.thena.storesql.statement.RepoSqlBuilderImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlBuilderImpl implements SqlBuilder {
  protected final DbCollections ctx;

  @Override
  public RepoSqlBuilder repo() {
    return new RepoSqlBuilderImpl(ctx);
  }
  @Override
  public GitRefSqlBuilder refs() {
    return new GitRefSqlBuilderImpl(ctx);
  }
  @Override
  public GitTagSqlBuilder tags() {
    return new GitTagSqlBuilderImpl(ctx);
  }
  @Override
  public GitBlobSqlBuilder blobs() {
    return new GitBlobSqlBuilderImpl(ctx);
  }
  @Override
  public GitTreeSqlBuilder trees() {
    return new GitTreeSqlBuilderImpl(ctx);
  }
  @Override
  public GitTreeItemSqlBuilder treeItems() {
    return new GitTreeItemSqlBuilderImpl(ctx);
  }
  @Override
  public GitCommitSqlBuilder commits() {
    return new GitCommitSqlBuilderImpl(ctx);
  }
  @Override
  public SqlBuilder withOptions(DbCollections options) {
    return new SqlBuilderImpl(options);
  }
  @Override
  public DocSqlBuilder docs() {
    return new DocSqlBuilderImpl(ctx);
  }
  @Override
  public DocLogSqlBuilder docLogs() {
    return new DocLogSqlBuilderImpl(ctx);
  }
  @Override
  public DocCommitSqlBuilder docCommits() {
    return new DocCommitSqlBuilderImpl(ctx);
  }
  @Override
  public DocBranchSqlBuilder docBranches() {
    return new DocBranchSqlBuilderImpl(ctx);
  }
  @Override
  public OrgUserSqlBuilder orgMembers() {
    return new OrgUserSqlBuilderImpl(ctx);
  }
  @Override
  public OrgGroupSqlBuilder orgParties() {
    return new OrgGroupSqlBuilderImpl(ctx);
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
	public OrgUserMembershipsSqlBuilder orgMemberships() {
		return new OrgUserMembershipsSqlBuilderImpl(ctx);
	}
	@Override
	public OrgRoleSqlBuilder orgRights() {
		return new OrgRoleSqlBuilderImpl(ctx);
	}
	@Override
	public OrgUserRoleSqlBuilder orgMemberRights() {
		return new OrgUserRoleSqlBuilderImpl(ctx);
	}
	@Override
	public OrgGroupRoleSqlBuilder orgPartyRights() {
		return new OrgGroupRoleSqlBuilderImpl(ctx);
	}
  @Override
  public OrgActorStatusSqlBuilder orgActorStatus() {
    return new OrgActorStatusSqlBuilderImpl(ctx);
  }
}
