package io.resys.thena.projects.client.spi.actions;

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

import java.util.Arrays;
import java.util.List;

import io.resys.thena.projects.client.api.TenantConfigClient.CreateTenantConfigAction;
import io.resys.thena.projects.client.api.model.TenantConfig;
import io.resys.thena.projects.client.api.model.TenantConfigCommand.CreateTenantConfig;
import io.resys.thena.projects.client.spi.store.ProjectStore;
import io.resys.thena.projects.client.spi.visitors.CreateTenantConfigsVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateTenantConfigImpl implements CreateTenantConfigAction {
  private final ProjectStore ctx;
  
  @Override
  public Uni<TenantConfig> createOne(CreateTenantConfig command) {
    return this.createMany(Arrays.asList(command))
       .onItem().transform(tasks -> tasks.get(0)) ;
  }
  
  @Override
  public Uni<List<TenantConfig>> createMany(List<? extends CreateTenantConfig> commands) {
    return ctx.getConfig().accept(new CreateTenantConfigsVisitor(commands));
  }

}
