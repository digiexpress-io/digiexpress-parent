package io.resys.thena.docdb.spi.objects;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.api.actions.ObjectsActions;
import io.resys.thena.docdb.spi.ClientState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ObjectsActionsDefault implements ObjectsActions {
  private final ClientState state;

  @Override
  public RepoStateBuilder repoState() {
    return new RepoStateBuilderDefault(state);
  }
  @Override
  public RefStateBuilder refState() {
    return new RefStateBuilderDefault(state);
  }
  @Override
  public CommitStateBuilder commitState() {
    return new CommitStateBuilderDefault(state);
  }
  @Override
  public BlobStateBuilder blobState() {
    return new BlobStateBuilderDefault(state);
  }
}
