package io.resys.thena.docdb.models.org.actions;

import io.resys.thena.docdb.api.actions.OrgQueryActions;
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
    // TODO Auto-generated method stub
    return null;
  }
}
