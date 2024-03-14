package io.resys.thena.docdb.models.org.actions;

import io.resys.thena.docdb.api.actions.OrgQueryActions;
import io.resys.thena.docdb.models.org.queries.OrgGroupHierarchyQueryImpl;
import io.resys.thena.docdb.models.org.queries.OrgRoleHierarchyQueryImpl;
import io.resys.thena.docdb.models.org.queries.OrgUserHierarchyQueryImpl;
import io.resys.thena.docdb.models.org.queries.OrgUserObjectsQueryImpl;
import io.resys.thena.docdb.spi.DbState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgQueryActionsImpl implements OrgQueryActions {
  private final DbState state;

  @Override
  public UserObjectsQuery userQuery() {
    return new OrgUserObjectsQueryImpl(state);
  }

	@Override
	public UserHierarchyQuery userHierarchyQuery() {
		return new OrgUserHierarchyQueryImpl(state);
	}

  @Override
  public GroupHierarchyQuery groupHierarchyQuery() {
    return new OrgGroupHierarchyQueryImpl(state);
  }

  @Override
  public RoleHierarchyQuery roleHierarchyQuery() {
    return new OrgRoleHierarchyQueryImpl(state);
  }
}
