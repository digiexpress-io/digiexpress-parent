package io.resys.thena.structures.grim.actions;

import io.resys.thena.api.actions.GrimCommitActions;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.grim.create.CreateManyMissionsImpl;
import io.resys.thena.structures.grim.modify.ModifyManyMissionsImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimCommitActionsImpl implements GrimCommitActions {
  private final DbState state;
  private final String repoId;

  @Override
  public CreateOneMission createOneMission() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CreateManyMissions createManyMissions() {
    return new CreateManyMissionsImpl(state, repoId);
  }

  @Override
  public ModifyOneMission modifyOneMission() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ModifyManyMissions modifyManyMission() {
    return new ModifyManyMissionsImpl(state, repoId);
  }

}
