package io.resys.thena.structures.org.actions;

import io.resys.thena.api.actions.OrgQueryActions;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.queries.MemberHierarchyQueryImpl;
import io.resys.thena.structures.org.queries.MemberObjectsQueryImpl;
import io.resys.thena.structures.org.queries.PartyHierarchyQueryImpl;
import io.resys.thena.structures.org.queries.RightHierarchyQueryImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgQueryActionsImpl implements OrgQueryActions {
  private final DbState state;
  private final String repoId;
  
  @Override
  public MemberObjectsQuery memberQuery() {
    return new MemberObjectsQueryImpl(state, repoId);
  }

	@Override
	public MemberHierarchyQuery memberHierarchyQuery() {
		return new MemberHierarchyQueryImpl(state, repoId);
	}

  @Override
  public PartyHierarchyQuery partyHierarchyQuery() {
    return new PartyHierarchyQueryImpl(state, repoId);
  }

  @Override
  public RightHierarchyQuery rightHierarchyQuery() {
    return new RightHierarchyQueryImpl(state, repoId);
  }
}
