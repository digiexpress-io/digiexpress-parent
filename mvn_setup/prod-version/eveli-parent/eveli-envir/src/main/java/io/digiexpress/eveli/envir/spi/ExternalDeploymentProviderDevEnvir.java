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

import java.time.OffsetDateTime;
import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.api.ExternalDeploymentProvider;
import io.digiexpress.eveli.envir.api.ImmutableEveliDeployment;
import io.digiexpress.eveli.envir.api.ImmutableEveliSources;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilComposer.SiteState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExternalDeploymentProviderDevEnvir implements ExternalDeploymentProvider {
  private final StencilClient stencilClient;
  private final HdesClient wrenchClient;

  @Override
  public Uni<Optional<EveliDeployment>> getDeployment() {
    return Uni.combine().all()
        .unis(stencilState(), wrenchState())
        .asTuple().onItem().transform(tuple -> {
          
          final var now = OffsetDateTime.now();
          final AstTag wrench = tuple.getItem2();
          final SiteState stencil = tuple.getItem1();
          
          
          
          final var deployment = ImmutableEveliDeployment.builder()
            .id(wrench.getCreated() + stencil.getCommit() + "-dev")
            .createdAt(now)
            .startsAt(now)
            .name("dev")
            .externalId(null)
            .sources(ImmutableEveliSources.builder()
                .stencil(stencil)
                .wrench(wrench)
                .build())
            .status(EveliDeploymentStatus.READY)
            .build();
          
          return Optional.ofNullable(deployment);
        });
  }

  private Uni<SiteState> stencilState() {
    return stencilClient.getStore().query().head();
  }
  
  private Uni<AstTag> wrenchState() {
    return wrenchClient.store().query().get().onItem().transform(ComposerEntityMapper::toTag);
  }
}
