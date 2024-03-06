package io.resys.thena.docdb.models.org.actions;

import io.resys.thena.docdb.api.actions.OrgCommitActions;
import io.resys.thena.docdb.models.org.create.CreateOneGroupImpl;
import io.resys.thena.docdb.models.org.create.CreateOneRoleImpl;
import io.resys.thena.docdb.models.org.create.CreateOneUserImpl;
import io.resys.thena.docdb.models.org.modify.ModifyOneUserImpl;
import io.resys.thena.docdb.spi.DbState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgCommitActionsImpl implements OrgCommitActions {
  private final DbState state;

  @Override
  public CreateOneUser createOneUser() {
    return new CreateOneUserImpl(state);
  }
  @Override
  public CreateOneGroup createOneGroup() {
    return new CreateOneGroupImpl(state);
  }
  @Override
  public CreateOneRole createOneRole() {
  	return new CreateOneRoleImpl(state);
  }
  @Override
  public ModifyOneUser modifyOneUser() {
    return new ModifyOneUserImpl(state);
  }

  @Override
  public ModifyOneGroup modifyOneGroup() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ModifyOneRole modifyOneRole() {
    // TODO Auto-generated method stub
    return null;
  }
}
