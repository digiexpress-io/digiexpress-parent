package io.resys.thena.structures.grim.actions;

import io.resys.thena.api.actions.GrimCommitActions;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.grim.create.CreateManyMissionsImpl;
import io.resys.thena.structures.grim.create.CreateOneMissionsImpl;
import io.resys.thena.structures.grim.modify.ModifyManyCommitViewersImpl;
import io.resys.thena.structures.grim.modify.ModifyManyMissionsImpl;
import io.resys.thena.structures.grim.modify.ModifyOneMissionImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimCommitActionsImpl implements GrimCommitActions {
  private final DbState state;
  private final String repoId;

  @Override
  public CreateManyMissions createManyMissions() {
    return new CreateManyMissionsImpl(state, repoId);
  }
  @Override
  public CreateOneMission createOneMission() {
    return new CreateOneMissionsImpl(state, repoId);
  }
  @Override
  public ModifyOneMission modifyOneMission() {
    return new ModifyOneMissionImpl(state, repoId);
  }
  @Override
  public ModifyManyMissions modifyManyMissions() {
    return new ModifyManyMissionsImpl(state, repoId);
  }
  @Override
  public ModifyManyCommitViewers modifyManyCommitViewer() {
    return new ModifyManyCommitViewersImpl(state, repoId);
  }

}
