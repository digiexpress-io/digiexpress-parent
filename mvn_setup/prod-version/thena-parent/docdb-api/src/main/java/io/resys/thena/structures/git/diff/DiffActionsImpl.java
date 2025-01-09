package io.resys.thena.structures.git.diff;

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

import java.util.function.Supplier;

import io.resys.thena.api.ThenaClient.GitTenantQuery;
import io.resys.thena.api.actions.GitCommitActions;
import io.resys.thena.api.actions.GitDiffActions;
import io.resys.thena.api.actions.GitPullActions;
import io.resys.thena.spi.DbState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DiffActionsImpl implements GitDiffActions {
  private final DbState state;
  private final GitPullActions objects;
  private final GitCommitActions commits;
  private final Supplier<GitTenantQuery> repos;
  
  @Override
  public DiffQuery diffQuery() {
    return new DiffQueryImpl(state, objects, commits, repos);
  }
}
