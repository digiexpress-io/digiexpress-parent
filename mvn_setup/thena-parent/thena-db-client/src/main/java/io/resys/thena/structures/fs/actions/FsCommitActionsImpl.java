package io.resys.thena.structures.fs.actions;

/*-
 * #%L
 * thena-db-client
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

import io.resys.thena.api.actions.FsCommitActions;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.fs.actions.create.CreateManyDirentsImpl;
import io.resys.thena.structures.fs.actions.create.CreateOneDirentImpl;
import io.resys.thena.structures.fs.actions.modify.ModifyManyDirentsImpl;
import io.resys.thena.structures.fs.actions.modify.ModifyOneDirentImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class FsCommitActionsImpl implements FsCommitActions {
  private final DbState startingState;
  private final String repoId;
  
  @Override
  public CreateOneDirent createOneDirent() {
    return new CreateOneDirentImpl(startingState, repoId);
  }
  @Override
  public CreateManyDirents createManyDirents() {
    return new CreateManyDirentsImpl(startingState, repoId);
  }
  @Override
  public ModifyOneDirent modifyOneDirent() {
    return new ModifyOneDirentImpl(startingState, repoId);
  }
  @Override
  public ModifyManyDirents modifyManyDirents() {
    return new ModifyManyDirentsImpl(startingState, repoId);
  }
}
