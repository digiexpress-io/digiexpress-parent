package io.resys.thena.models.org.actions;

import io.resys.thena.api.actions.OrgCommitActions;
import io.resys.thena.models.org.create.CreateOneMemberImpl;
import io.resys.thena.models.org.create.CreateOnePartyImpl;
import io.resys.thena.models.org.create.CreateOneRoleImpl;
import io.resys.thena.models.org.modify.ModifyOneMemberImpl;
import io.resys.thena.models.org.modify.ModifyOneRightImpl;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class OrgCommitActionsImpl implements OrgCommitActions {
  private final DbState state;
  private final String repoId;

  @Override
  public CreateOneMember createOneMember() {
    return new CreateOneMemberImpl(state, repoId);
  }
  @Override
  public CreateOneParty createOneParty() {
    return new CreateOnePartyImpl(state, repoId);
  }
  @Override
  public CreateOneRight createOneRight() {
  	return new CreateOneRoleImpl(state, repoId);
  }
  @Override
  public ModifyOneMember modifyOneMember() {
    return new ModifyOneMemberImpl(state, repoId);
  }
  @Override
  public ModifyOneRight modifyOneRight() {
    return new ModifyOneRightImpl(state, repoId);
  }
  @Override
  public ModifyOneParty modifyOneParty() {
    // TODO Auto-generated method stub
    return null;
  }
}
