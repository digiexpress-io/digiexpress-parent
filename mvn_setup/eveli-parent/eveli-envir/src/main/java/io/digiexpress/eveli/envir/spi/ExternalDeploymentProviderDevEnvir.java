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
import java.util.List;
import java.util.Optional;

import io.dialob.api.form.Form;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.api.ExternalDeploymentProvider;
import io.digiexpress.eveli.envir.api.ImmutableEveliDeployment;
import io.digiexpress.eveli.envir.api.ImmutableEveliSources;
import io.digiexpress.thena.cockpit.client.api.CockpitAware.CockpitIdSupplier;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple3;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
public class ExternalDeploymentProviderDevEnvir implements ExternalDeploymentProvider {
  private final StencilClient stencilClient;
  private final HdesClient wrenchClient;
  private final DialobClient dialobClient;
  private final CockpitIdSupplier cockpitIdSupplier;
  
  public ExternalDeploymentProviderDevEnvir(
      StencilClient stencilClient,
      HdesClient wrenchClient, 
      DialobClient dialobClient) {
    super();
    this.stencilClient = stencilClient;
    this.wrenchClient = wrenchClient;
    this.dialobClient = dialobClient;
    this.cockpitIdSupplier = () -> Uni.createFrom().item(Optional.<String>empty());
  }
  
  @Override
  public ExternalDeploymentProvider withCockpitIdSupplier(CockpitIdSupplier supplier) {
    return new ExternalDeploymentProviderDevEnvir(stencilClient, wrenchClient, dialobClient, supplier);
  }
  
  @Override
  public Uni<Optional<EveliDeployment>> getDeployment(boolean emptyBranchBody) {
    if(emptyBranchBody) {
      return getDeploymentWithoutBody();
    }
    return getDeploymentWithBody();
  }
  
  private Uni<Optional<EveliDeployment>> getDeploymentWithoutBody() {
    return this.cockpitIdSupplier.apply().onItem().transformToUni(optionalId -> Uni.combine().all()
        .unis(
            wrenchClient.withCockpit(optionalId).onItem().transformToUni(client -> client.store().query().getBranch()), 
            stencilClient.withCockpit(optionalId).onItem().transformToUni(client -> client.getStore().query().getBranch()),
            Uni.createFrom().item(optionalId)
        )
        .asTuple())
        .onItem().transform(tuple -> {
          
          final var now = OffsetDateTime.now();
  
          final var wrenchCommitId = tuple.getItem1().map(e -> e.getCommit()).orElse("not-created");
          final var stencilCommitId = tuple.getItem2().map(e -> e.getCommit()).orElse("not-created");
          final var cockpitId = tuple.getItem3().orElse(null);
          
          final var deployment = ImmutableEveliDeployment.builder()
            .id(createId(wrenchCommitId, stencilCommitId))
            .createdAt(now)
            .startsAt(now)
            .createdBy(ExternalDeploymentProviderDevEnvir.class.getCanonicalName())
            .description("live deployment")
            .name("editable asset envir")
            .externalId(null)
            .cockpitId(cockpitId)
            .external(true)
            .sources(null)
            .status(EveliDeploymentStatus.READY)
            .build();
          
          return Optional.ofNullable(deployment);
        });
  }
  
  

  private Uni<Optional<EveliDeployment>> getDeploymentWithBody() {
    return this.cockpitIdSupplier.apply().onItem()
      .transformToUni(cockpitId -> Uni.combine().all().unis(
          stencilState(cockpitId).onItem().transformToUni(stencil -> {
            return getForms(stencil).onItem().transform(forms -> Tuple3.of(stencil, forms, cockpitId));
          }),
          wrenchState(cockpitId)
      ).asTuple())
      .onItem().transform(tuple -> {
          
          final var now = OffsetDateTime.now();
          final AstTag wrench = tuple.getItem2();
          final SiteState stencil = tuple.getItem1().getItem1();
          final List<Form> dialob = tuple.getItem1().getItem2();
          final String cockpitId = tuple.getItem1().getItem3().orElse(null);
          
          
          final var deployment = ImmutableEveliDeployment.builder()
            .id(createId(wrench.getCommitId(), stencil.getCommit()))
            .createdAt(now)
            .startsAt(now)
            .createdBy(ExternalDeploymentProviderDevEnvir.class.getCanonicalName())
            .description("live deployment")
            .name("editable asset envir")
            .externalId(null)
            .cockpitId(cockpitId)
            .external(true)
            .sources(ImmutableEveliSources.builder()
                .stencil(stencil)
                .wrench(wrench)
                .dialob(dialob)
                .build())
            .status(EveliDeploymentStatus.READY)
            .build();
          
          return Optional.ofNullable(deployment);
        });
  }

  
  private String createId(String wrenchCommitId, String stencilCommitId) {
    return wrenchCommitId + "/dev/" + stencilCommitId;
  }
  
  private Uni<List<Form>> getForms(SiteState site) {
    final var workflows = site.getWorkflows().values().stream()
      .filter(e -> e.getBody().getFormId() != null)
      .filter(e -> !Boolean.TRUE.equals(e.getBody().getDevMode()))
      .toList();
    
    return Multi.createFrom().items(workflows.stream())
        .onItem().transform(entity -> {
          
          try {
            return Optional.ofNullable(getFormIdById(entity));
          } catch(Exception e) {
            log.error(e.getMessage(), e);
            return Optional.<Form>empty();
          }
          
        })
        .collect().asList()
        .onItem().transform(e -> e.stream().filter(sub -> sub.isPresent()).map(sub -> sub.get()).toList());
  }

  private Form getFormIdById(final Entity<Workflow> stencilService) {

    try {
      return dialobClient.getFormById(stencilService.getBody().getFormId());
    } catch(Exception e) {
      throw new DialobFormNotFoundException(
          "Can't resolve for by tag or form name, will try by form id for topic: " + 
          JsonObject.mapFrom(stencilService).encodePrettily());
    }
  }


  private Uni<SiteState> stencilState(Optional<String> cockpitId) {
    return stencilClient.withCockpit(cockpitId)
        .onItem().transformToUni(client -> client.getStore().query().head());
  }
  
  private Uni<AstTag> wrenchState(Optional<String> cockpitId) {
    return wrenchClient.withCockpit(cockpitId)
        .onItem().transformToUni(client -> client.store().query().get().onItem().transform(ComposerEntityMapper::toTag));
  }
  
  public static class DialobFormNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1781444267360040922L;
    public DialobFormNotFoundException(String message) {
      super(message);
    }
  }
}
