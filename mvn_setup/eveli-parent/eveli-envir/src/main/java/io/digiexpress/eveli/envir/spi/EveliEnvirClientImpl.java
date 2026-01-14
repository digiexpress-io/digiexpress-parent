package io.digiexpress.eveli.envir.spi;

/*-
 * #%L
 * eveli-envir
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirTenantQuery;
import io.digiexpress.eveli.envir.spi.actions.CreateOneDeploymentImpl;
import io.digiexpress.eveli.envir.spi.actions.DeploymentStatusBuilderImpl;
import io.digiexpress.eveli.envir.spi.actions.DeploymentQueryImpl;
import io.digiexpress.eveli.envir.spi.actions.EveliDeploymentCompilerImpl;
import io.digiexpress.eveli.envir.spi.actions.EveliRuntimeCache;
import io.digiexpress.eveli.envir.spi.actions.EveliRuntimeQueryImpl;
import io.digiexpress.eveli.envir.spi.actions.ModifyOneDeploymentImpl;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitIdSupplier;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EveliEnvirClientImpl implements EveliEnvirClient {
  private final EveliEnvirStore ctx;

  private final HdesClientConfig hdesClientConfig;
  private final DialobClient dialobClient;
  private final EveliRuntimeCache cache;
  private final boolean isDev;

  
  public EveliEnvirStore getCtx() { return ctx; }
  
  public EveliEnvirClientImpl withTenant(String tenantId) {
    return new EveliEnvirClientImpl(ctx.withTenantId(tenantId), hdesClientConfig, dialobClient, cache, isDev);
  }
  public Uni<Tenant> getTenant() {
    return ctx.getTenant();
  }
  public EveliEnvirTenantQuery tenantQuery() {
    return new EveliEnvirTenantQueryImpl(ctx, hdesClientConfig, dialobClient, cache, isDev);
  }
  @Override
  public CreateOneDeployment createOneDeployment() {
    return new CreateOneDeploymentImpl(ctx);
  }
  @Override
  public ModifyOneDeployment modifyOneDeployment() {
    return new ModifyOneDeploymentImpl(ctx);
  }
  @Override
  public DeploymentQuery deploymentQuery() {
    return new DeploymentQueryImpl(ctx);
  }
  @Override
  public EveliDeploymentCompiler deploymentCompiler() {
    return new EveliDeploymentCompilerImpl(ctx, hdesClientConfig, dialobClient);
  }
  @Override
  public DeploymentStatusBuilder deploymentStatusBuilder() {
    return new DeploymentStatusBuilderImpl(ctx, cache);
  }
  @Override
  public EveliRuntimeQuery runtimeQuery() {
    return new EveliRuntimeQueryImpl(ctx, cache, hdesClientConfig, isDev);
  }

  @Override
  public void invalidateCache() {
    cache.invalidateAll();    
  }

  @Override
  public EveliEnvirClient withCockpitIdSupplier(CockpitIdSupplier supplier) {
    return new EveliEnvirClientImpl(ctx.withCockpitIdSupplier(supplier), hdesClientConfig, dialobClient, cache, isDev);
  }
}
