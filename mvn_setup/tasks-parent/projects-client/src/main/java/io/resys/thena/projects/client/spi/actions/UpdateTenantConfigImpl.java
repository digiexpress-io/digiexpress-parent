package io.resys.thena.projects.client.spi.actions;

import java.util.Arrays;

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

import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.projects.client.api.TenantConfigClient.UpdateTenantConfigAction;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.TenantConfigUpdateCommand;
import io.resys.thena.projects.client.spi.store.ProjectStore;
import io.resys.thena.projects.client.spi.visitors.UpdateTenantConfigVisitor;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UpdateTenantConfigImpl implements UpdateTenantConfigAction {

  private final ProjectStore ctx;

  @Override
  public Uni<TenantConfig> updateOne(TenantConfigUpdateCommand command) {        
    return updateOne(Arrays.asList(command));
  }

  @Override
  public Uni<TenantConfig> updateOne(List<TenantConfigUpdateCommand> commands) {
    RepoAssert.notNull(commands, () -> "commands must be defined!");
    RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
    
    final var uniqueTaskIds = commands.stream().map(command -> command.getTenantConfigId()).distinct().collect(Collectors.toList());
    RepoAssert.isTrue(uniqueTaskIds.size() == 1, () -> "TenantConfig id-s must be same, but got: %s!", uniqueTaskIds);
    
    return ctx.getConfig().accept(new UpdateTenantConfigVisitor(commands, ctx))
        .onItem().transformToUni(resp -> resp)
        .onItem().transform(tasks -> tasks.get(0));
  }

  @Override
  public Uni<List<TenantConfig>> updateMany(List<TenantConfigUpdateCommand> commands) {
    RepoAssert.notNull(commands, () -> "commands must be defined!");
    RepoAssert.isTrue(commands.size() > 0, () -> "No commands to apply!");
    
    return ctx.getConfig().accept(new UpdateTenantConfigVisitor(commands, ctx)).onItem()
        .transformToUni(item -> item);
  }
}

