package io.resys.thena.docdb.models.org.actions;

import io.resys.thena.docdb.api.actions.OrgQueryActions;
import io.resys.thena.docdb.models.org.queries.PartyHierarchyQueryImpl;
import io.resys.thena.docdb.models.org.queries.RightHierarchyQueryImpl;
import io.resys.thena.docdb.models.org.queries.MemberHierarchyQueryImpl;
import io.resys.thena.docdb.models.org.queries.MemberObjectsQueryImpl;
import io.resys.thena.docdb.spi.DbState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgQueryActionsImpl implements OrgQueryActions {
  private final DbState state;

  @Override
  public MemberObjectsQuery memberQuery() {
    return new MemberObjectsQueryImpl(state);
  }

	@Override
	public MemberHierarchyQuery memberHierarchyQuery() {
		return new MemberHierarchyQueryImpl(state);
	}

  @Override
  public PartyHierarchyQuery partyHierarchyQuery() {
    return new PartyHierarchyQueryImpl(state);
  }

  @Override
  public RightHierarchyQuery rightHierarchyQuery() {
    return new RightHierarchyQueryImpl(state);
  }
}
