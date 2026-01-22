package io.digiexpress.eveli.envir.spi.actions;

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

import java.time.OffsetDateTime;
import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.resys.hdes.client.api.HdesClient.ExecutorBuilder;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.spi.HdesClientEnvirBuilder;
import io.resys.hdes.client.spi.HdesClientExecutorBuilder;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.hdes.client.spi.envir.ProgramEnvirFactory;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilEnvir;
import io.thestencil.client.spi.builders.StencilEnvirImpl;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class EveliRuntimeImpl implements EveliRuntime {
  private final HdesClientConfig hdesClientConfig;
  private final ProgramEnvir wrenchEnvir;
  private final StencilEnvir stencilEnvir;
  private final EveliDeployment deployment;
  private final boolean isDev;
  
  public EveliRuntimeImpl(
      EveliDeployment deployment, 
      HdesClientConfig hdesClientConfig,
      boolean isDev) {
    
    this.hdesClientConfig = hdesClientConfig;
    this.hdesClientConfig.getCache().flushAll();
    this.wrenchEnvir = new HdesClientEnvirBuilder(new ProgramEnvirFactory(hdesClientConfig), hdesClientConfig.getTypes())
        .tagName(deployment.getName())
        .callback(builder -> ComposerEntityMapper.toEnvir(builder, deployment.getSources().getWrench()).build())
        .build();
    
    this.stencilEnvir = StencilEnvirImpl.of(deployment.getSources().getStencil(), deployment.getName(), isDev);
    this.deployment = deployment;
    this.isDev = isDev;
  }
  
  @Override
  public String getName() {
    return deployment.getName();
  }
  @Override
  public String getDeploymentId() {
    return deployment.getId();
  }
  @Override
  public ExecutorBuilder getWrench() {
    return new HdesClientExecutorBuilder(wrenchEnvir, hdesClientConfig.getTypes(), hdesClientConfig.getDependencyInjectionContext());
  }
  @Override
  public Sites getStencil(OffsetDateTime now, boolean auth) {
    return stencilEnvir.get(now, auth);
  }
  @Override
  public Sites getStencil(OffsetDateTime now) {
    return stencilEnvir.get(now, true);
  }
  public boolean isDev() {
    return isDev;
  }
  @Override
  public String getWrenchTagName() {
    return deployment.getSources().getWrench().getName();
  }
  @Override
  public String getStencilTagName() {
    return deployment.getSources().getStencil().getName();
  }
  @Override
  public OffsetDateTime getStartsAt() {
    return deployment.getStartsAt();
  }
  @Override
  public Optional<String> getCockpitId() {
    return Optional.ofNullable(deployment.getCockpitId());
  }
}
