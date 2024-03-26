package io.resys.thena.models.org.actions;

import io.resys.thena.api.actions.OrgQueryActions;
import io.resys.thena.models.org.queries.MemberHierarchyQueryImpl;
import io.resys.thena.models.org.queries.MemberObjectsQueryImpl;
import io.resys.thena.models.org.queries.PartyHierarchyQueryImpl;
import io.resys.thena.models.org.queries.RightHierarchyQueryImpl;
import io.resys.thena.spi.DbState;
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
