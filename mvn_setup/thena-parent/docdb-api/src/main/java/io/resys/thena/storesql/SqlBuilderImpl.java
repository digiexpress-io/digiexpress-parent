package io.resys.thena.storesql;

import io.resys.thena.datasource.SqlQueryBuilder;
import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.registry.org.OrgActorStatusRegistrySqlImpl;
import io.resys.thena.registry.org.OrgCommitRegistrySqlImpl;
import io.resys.thena.registry.org.OrgCommitTreeRegistrySqlImpl;
import io.resys.thena.registry.org.OrgMemberRightRegistrySqlImpl;
import io.resys.thena.registry.org.OrgMemberRegistrySqlImpl;
import io.resys.thena.registry.org.OrgMembershipRegistrySqlImpl;
import io.resys.thena.registry.org.OrgPartyRightRegistrySqlImpl;
import io.resys.thena.registry.org.OrgPartyRegistrySqlImpl;
import io.resys.thena.registry.org.OrgRightRegistrySqlImpl;
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

}
