package io.resys.thena.git.spi.actions.diff;

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

import io.resys.thena.git.api.GitClient.GitTenantQuery;
import io.resys.thena.git.api.GitCommitActions;
import io.resys.thena.git.api.GitDataSource;
import io.resys.thena.git.api.GitDiffActions;
import io.resys.thena.git.api.GitPullActions;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DiffActionsImpl implements GitDiffActions {
  private final GitDataSource state;
  private final GitPullActions objects;
  private final GitCommitActions commits;
  private final Supplier<GitTenantQuery> repos;
  
  @Override
  public DiffQuery diffQuery() {
    return new DiffQueryImpl(state, objects, commits, repos);
  }
}
