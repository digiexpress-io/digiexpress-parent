package io.resys.thena.structures.org.actions;

import io.resys.thena.api.actions.OrgCommitActions;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.create.CreateOneMemberImpl;
import io.resys.thena.structures.org.create.CreateOnePartyImpl;
import io.resys.thena.structures.org.create.CreateOneRoleImpl;
import io.resys.thena.structures.org.modify.ModifyOneMemberImpl;
import io.resys.thena.structures.org.modify.ModifyOnePartyImpl;
import io.resys.thena.structures.org.modify.ModifyOneRightImpl;
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
    return new ModifyOnePartyImpl(state, repoId);
  }
}
