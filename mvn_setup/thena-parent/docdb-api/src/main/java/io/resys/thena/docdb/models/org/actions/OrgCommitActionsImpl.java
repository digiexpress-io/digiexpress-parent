package io.resys.thena.docdb.models.org.actions;

import io.resys.thena.docdb.api.actions.OrgCommitActions;
import io.resys.thena.docdb.models.org.create.CreateOneGroupImpl;
import io.resys.thena.docdb.models.org.create.CreateOneRoleImpl;
import io.resys.thena.docdb.models.org.create.CreateOneUserImpl;
import io.resys.thena.docdb.models.org.modify.ModifyOneMemberImpl;
import io.resys.thena.docdb.models.org.modify.ModifyOneRightImpl;
import io.resys.thena.docdb.spi.DbState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgCommitActionsImpl implements OrgCommitActions {
  private final DbState state;

  @Override
  public CreateOneMember createOneMember() {
    return new CreateOneUserImpl(state);
  }
  @Override
  public CreateOneParty createOneParty() {
    return new CreateOneGroupImpl(state);
  }
  @Override
  public CreateOneRight createOneRight() {
  	return new CreateOneRoleImpl(state);
  }
  @Override
  public ModifyOneMember modifyOneMember() {
    return new ModifyOneMemberImpl(state);
  }
  @Override
  public ModifyOneRight modifyOneRight() {
    return new ModifyOneRightImpl(state);
  }
  @Override
  public ModifyOneParty modifyOneParty() {
    // TODO Auto-generated method stub
    return null;
  }
}
