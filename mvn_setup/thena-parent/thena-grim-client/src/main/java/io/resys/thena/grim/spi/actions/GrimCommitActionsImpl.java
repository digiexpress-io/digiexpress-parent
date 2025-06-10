package io.resys.thena.grim.spi.actions;

/*-
 * #%L
 * thena-grim-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
