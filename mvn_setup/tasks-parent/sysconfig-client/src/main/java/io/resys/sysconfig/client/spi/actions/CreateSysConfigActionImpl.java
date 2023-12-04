package io.resys.sysconfig.client.spi.actions;

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

import io.resys.sysconfig.client.api.AssetClient;
import io.resys.sysconfig.client.api.SysConfigClient.CreateSysConfigAction;
import io.resys.sysconfig.client.api.model.SysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfig;
import io.resys.sysconfig.client.api.model.SysConfigCommand.CreateSysConfigRelease;
import io.resys.sysconfig.client.api.model.SysConfigDeployment;
import io.resys.sysconfig.client.api.model.SysConfigDeploymentCommand.CreateSysConfigDeployment;
import io.resys.sysconfig.client.api.model.SysConfigRelease;
import io.resys.sysconfig.client.spi.store.DocumentStore;
import io.resys.sysconfig.client.spi.visitors.CreateSysConfigDeploymentVisitor;
import io.resys.sysconfig.client.spi.visitors.CreateSysConfigVisitor;
import io.resys.sysconfig.client.spi.visitors.CreateSysConfigReleaseVisitor;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateSysConfigActionImpl implements CreateSysConfigAction {
  private final DocumentStore ctx;
  private final AssetClient assetClient;
  
  @Override
  public Uni<SysConfig> createOne(CreateSysConfig command) {
    return ctx.getConfig().accept(new CreateSysConfigVisitor(Arrays.asList(command)))
       .onItem().transform(tasks -> tasks.get(0));
  }

  @Override
  public Uni<SysConfigDeployment> createOne(CreateSysConfigDeployment command) {
    return ctx.getConfig().accept(new CreateSysConfigDeploymentVisitor(Arrays.asList(command)))
        .onItem().transform(tasks -> tasks.get(0));
  }

  @Override
  public Uni<SysConfigRelease> createOne(CreateSysConfigRelease command) {
    return ctx.getConfig().accept(new CreateSysConfigReleaseVisitor(command, ctx, assetClient))
        .onItem().transformToUni(uni -> uni);
  }
}
