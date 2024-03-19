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
  public MemberObjectsQuery memberQuery() {
    return new OrgUserObjectsQueryImpl(state);
  }

	@Override
	public MemberHierarchyQuery memberHierarchyQuery() {
		return new OrgUserHierarchyQueryImpl(state);
	}

  @Override
  public PartyHierarchyQuery partyHierarchyQuery() {
    return new OrgGroupHierarchyQueryImpl(state);
  }

  @Override
  public RightHierarchyQuery rightHierarchyQuery() {
    return new OrgRoleHierarchyQueryImpl(state);
  }
}
