package io.resys.thena.tasks.client.spi;

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

import io.resys.thena.tasks.client.api.TasksClient;
import io.resys.thena.tasks.client.api.actions.ExportActions;
import io.resys.thena.tasks.client.api.actions.MigrationActions;
import io.resys.thena.tasks.client.api.actions.RepositoryActions;
import io.resys.thena.tasks.client.api.actions.StatisticsActions;
import io.resys.thena.tasks.client.api.actions.TaskActions;
import io.resys.thena.tasks.client.spi.actions.ExportActionsImpl;
import io.resys.thena.tasks.client.spi.actions.RepositoryActionsImpl;
import io.resys.thena.tasks.client.spi.actions.TaskActionsImpl;
import io.resys.thena.tasks.client.spi.store.DocumentStore;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TaskClientImpl implements TasksClient {
  private final DocumentStore ctx;
  
  @Override
  public TaskActions tasks() {
    return new TaskActionsImpl(ctx);
  }
  @Override
  public StatisticsActions statistics() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public MigrationActions migrate() {
    // TODO Auto-generated method stub
    return null;
  }
  @Override
  public ExportActions export() {
    return new ExportActionsImpl(ctx);
  }
  @Override
  public RepositoryActions repo() {
    return new RepositoryActionsImpl(ctx);
  }
}
