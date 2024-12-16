package io.resys.thena.structures.org.actions;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.actions.OrgCommitActions;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.org.create.CreateOneMemberImpl;
import io.resys.thena.structures.org.create.CreateOnePartyImpl;
import io.resys.thena.structures.org.create.CreateOneRightImpl;
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
  	return new CreateOneRightImpl(state, repoId);
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
