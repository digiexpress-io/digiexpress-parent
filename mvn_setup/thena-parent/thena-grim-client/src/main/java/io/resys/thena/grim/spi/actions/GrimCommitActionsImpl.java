package io.resys.thena.grim.spi.actions;

import io.resys.thena.grim.api.GrimCommitActions;
import io.resys.thena.grim.spi.GrimDataSource;
import io.resys.thena.grim.spi.create.CreateManyMissionsImpl;
import io.resys.thena.grim.spi.create.CreateOneMissionsImpl;
import io.resys.thena.grim.spi.modify.ModifyManyCommitViewersImpl;
import io.resys.thena.grim.spi.modify.ModifyManyMissionsImpl;
import io.resys.thena.grim.spi.modify.ModifyOneMissionImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrimCommitActionsImpl implements GrimCommitActions {
  private final GrimDataSource state;
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
