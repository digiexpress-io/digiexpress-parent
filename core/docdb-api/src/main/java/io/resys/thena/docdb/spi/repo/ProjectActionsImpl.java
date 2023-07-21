package io.resys.thena.docdb.spi.repo;

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

import io.resys.thena.docdb.api.actions.ProjectActions;
import io.resys.thena.docdb.spi.ClientState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ProjectActionsImpl implements ProjectActions {
  private final ClientState state;

  @Override
  public ProjectsQuery projectsQuery() {
    return new RepoQueryBuilderImpl(state);
  }

  @Override
  public ProjectBuilder projectBuilder() {
    return new RepoCreateBuilderImpl(state);
  }

  @Override
  public ProjectObjectsQuery projectQuery() {
    return new RepoStateBuilderImpl(state);
  }
}
