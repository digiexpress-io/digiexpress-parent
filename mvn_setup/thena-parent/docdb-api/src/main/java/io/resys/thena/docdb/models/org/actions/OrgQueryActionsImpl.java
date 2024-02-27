package io.resys.thena.docdb.models.org.actions;

import io.resys.thena.docdb.api.actions.OrgQueryActions;
import io.resys.thena.docdb.spi.DbState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgQueryActionsImpl implements OrgQueryActions {
  private final DbState state;

  @Override
  public UserObjectsQuery userQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GroupObjectsQuery groupQuery() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GroupObjectsQuery roleQuery() {
    // TODO Auto-generated method stub
    return null;
  }

}
