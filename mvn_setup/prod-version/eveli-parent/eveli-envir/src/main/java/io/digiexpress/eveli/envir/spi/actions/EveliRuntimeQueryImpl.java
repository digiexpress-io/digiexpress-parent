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

import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntime;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliRuntimeQuery;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EveliRuntimeQueryImpl implements EveliRuntimeQuery {

  private final EveliEnvirStore ctx;
  private final EveliRuntimeCache cache;
  private final HdesClientConfig hdesClientConfig;
  
  private final boolean isDev;
  

  private EveliRuntime createEnvir(EveliDeployment deployment) {
    final var envir = new EveliRuntimeImpl(deployment, hdesClientConfig, isDev);
    cache.save(envir);
    return envir;
  }
  
  private Uni<EveliRuntime> getOrCreateEnvir(EveliDeployment deployment) {
    final var currentEnvir = cache.get();
    
    // already created
    if(currentEnvir.isPresent() && currentEnvir.get().getDeploymentId().equals(deployment.getId())) {
      return Uni.createFrom().item(currentEnvir.get());
    }
    return new DeploymentQueryImpl(ctx).emptyBranchBody(true)
      .status(EveliDeploymentStatus.DEPLOYED)
      .emptyBranchBody(false)
      .getOneById(deployment.getId())
      .onItem().transform(this::createEnvir);
    
  }
  
  private Uni<Optional<EveliDeployment>> getLastDeployment() {
    return new DeploymentQueryImpl(ctx).emptyBranchBody(true)
      .status(EveliDeploymentStatus.DEPLOYED)
      .emptyBranchBody(true)
      .findAll()
      .onItem().transform(deployments -> deployments.stream()
          .sorted((a, b) -> b.getStartsAt().compareTo(b.getStartsAt()))
          .findFirst()
      );
  }
  
  
  @Override
  public Uni<EveliRuntime> getOne() {
    return getLastDeployment().onItem().transformToUni(last -> {
      if(last.isEmpty()) {
        
        return ctx.getExternalProvider().getDeployment().onItem().transformToUni(resp -> {
          if(resp.isPresent()) {
            final Uni<EveliRuntime> external = getOrCreateEnvir(resp.get());
            return external;
          }
          
          throw new EveliRuntimeQueryException("No deployments that can be activated!");
        });
        
        
      }
      
      return getOrCreateEnvir(last.get());
    });
  }

  @Override
  public Uni<Optional<EveliRuntime>> findOne() {
    return getLastDeployment().onItem().transformToUni(last -> {
      if(last.isEmpty()) {
        
        return ctx.getExternalProvider().getDeployment().onItem().transformToUni(resp -> {
          if(resp.isPresent()) {
            final Uni<EveliRuntime> external = getOrCreateEnvir(resp.get());
            return external.onItem().transform(e -> Optional.of(e));
          }
          return Uni.createFrom().item(Optional.<EveliRuntime>empty());
          
        });
      }
      return getOrCreateEnvir(last.get()).onItem().transform(e -> Optional.of(e));
    });
  }
  
  

  public static class EveliRuntimeQueryException extends RuntimeException {
    private static final long serialVersionUID = -6001308683183662536L;

    public EveliRuntimeQueryException(String error) {
      super(error);
    }

  }
}
