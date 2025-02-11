package io.digiexpress.eveli.envir.spi.actions;

import java.time.OffsetDateTime;

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

  private final EveliRuntimeLogger logging = new EveliRuntimeLogger();
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
    final var currentEnvir = cache.getRuntime(deployment.getId());
    logging.cachedRuntime(currentEnvir);
    
    // already created
    if(currentEnvir.isPresent() && currentEnvir.get().getDeploymentId().equals(deployment.getId())) {
      return Uni.createFrom().item(currentEnvir.get());
    }
    
    return new DeploymentQueryImpl(ctx).emptyBranchBody(true)
      .status(EveliDeploymentStatus.DEPLOYED)
      .emptyBranchBody(false)
      .getOneById(deployment.getId())
      .onItem().transform(this::createEnvir)
      .onItem().invoke(created -> logging.cachingRuntime(created));
    
  }
  
  private Uni<Optional<EveliDeployment>> getLastDeployment() {
    final var cached = cache.getDeployment();
    logging.lastCachedDeployment(cached);

    if(cached.isPresent()) {
      return Uni.createFrom().item(cached);
    }
    final OffsetDateTime now = OffsetDateTime.now();
    return new DeploymentQueryImpl(ctx).emptyBranchBody(true)
      .status(EveliDeploymentStatus.READY)
      .emptyBranchBody(true)
      .findAll()
      .onItem().transform(deployments -> deployments.stream()
          .filter((a) -> a.getStartsAt().isBefore(now) || a.getStartsAt().isEqual(now))
          .sorted((a, b) -> b.getStartsAt().compareTo(b.getStartsAt()))
          .findFirst()
      )
      .onItem().transformToUni(latest -> {
        logging.lastQueriedDeployment(latest);
        
        if(latest.isEmpty()) {
          return ctx.getExternalProvider().getDeployment().onItem().invoke(ext -> logging.lastExternalDeployment(latest));  
        }
        
        return Uni.createFrom().item(latest);
      })
      
      .onItem().transform(latest -> {
        if(latest.isPresent()) {
          logging.cachingDeployment(latest);
          cache.save(latest.get());
        }
        return latest;
      });
  }
  
  
  @Override
  public Uni<EveliRuntime> getOne() {
    return getLastDeployment().onItem().transformToUni(last -> {
      
      if(last.isPresent()) {
        final Uni<EveliRuntime> external = getOrCreateEnvir(last.get());
        logging.info();
        return external;
      }
      logging.error();
      throw new EveliRuntimeQueryException("No deployments that can be activated!");
    });
  }

  @Override
  public Uni<Optional<EveliRuntime>> findOne() {
    return getLastDeployment().onItem().transformToUni(last -> {
      if(last.isEmpty()) {
        return Uni.createFrom().item(Optional.<EveliRuntime>empty());
      }
      return getOrCreateEnvir(last.get()).onItem().transform(e -> Optional.of(e));
    }).onItem().invoke(e -> logging.info());
  }
  public static class EveliRuntimeQueryException extends RuntimeException {
    private static final long serialVersionUID = -6001308683183662536L;

    public EveliRuntimeQueryException(String error) {
      super(error);
    }

  }
}
