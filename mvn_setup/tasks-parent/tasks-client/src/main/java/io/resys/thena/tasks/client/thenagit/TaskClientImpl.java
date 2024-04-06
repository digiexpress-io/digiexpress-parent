package io.resys.thena.tasks.client.thenagit;

/*-
 * #%L
 * thena-tasks-client
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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

import io.resys.thena.tasks.client.api.TaskClient;
import io.resys.thena.tasks.client.api.actions.ExportActions;
import io.resys.thena.tasks.client.api.actions.RepositoryActions;
import io.resys.thena.tasks.client.api.actions.TaskActions;
import io.resys.thena.tasks.client.thenagit.actions.ExportActionsImpl;
import io.resys.thena.tasks.client.thenagit.actions.RepositoryActionsImpl;
import io.resys.thena.tasks.client.thenagit.actions.TaskActionsImpl;
import io.resys.thena.tasks.client.thenagit.store.DocumentStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskClientImpl implements TaskClient {
  private final DocumentStore ctx;
  
  @Override
  public TaskActions tasks() {
    return new TaskActionsImpl(ctx);
  }
  @Override
  public ExportActions export() {
    return new ExportActionsImpl(ctx);
  }
  @Override
  public RepositoryActions repo() {
    return new RepositoryActionsImpl(ctx);
  }
  public DocumentStore getCtx() {
    return ctx;
  }
  @Override
  public TaskClient withRepoId(String repoId) {
    return new TaskClientImpl(ctx.withRepoId(repoId));
  }
}
