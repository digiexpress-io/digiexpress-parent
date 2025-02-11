package io.digiexpress.eveli.client.web.resources.assets;

/*-
 * #%L
 * eveli-client
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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;
import io.digiexpress.eveli.client.api.AuthClient;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.ImmutableCreateEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.spi.HdesComposerImpl;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.thestencil.client.api.ImmutableCreateRelease;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.StencilComposerImpl;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequiredArgsConstructor
@RequestMapping("/worker/rest/api/assets/publications")
@Slf4j
public class AssetsPublicationController {
  private final EveliEnvirClient envirClient;
  private final StencilClient stencilClient;
  private final HdesClient wrenchClient;
  private final DialobClient dialobClient;
  private final AuthClient securityClient;

  @Value.Immutable
  @JsonSerialize(as = ImmutableCreatePublication.class)
  @JsonDeserialize(as = ImmutableCreatePublication.class)
  public interface CreatePublication {
    @Nullable String getStencilTag(); // auto-create tag on null
    @Nullable String getWrenchTag();  // auto-create tag on null
    
    @Nullable String getName();  // autoname on null
    @Nullable String getDescription();
    @Nullable LocalDateTime getLiveDate();
    
  }
  
  @GetMapping
  public Uni<List<EveliDeployment>> findAllPublications() {
    return envirClient.deploymentQuery().findAll();
  }
  
  @GetMapping("/{name}")
  public Uni<EveliDeployment> getOnePublicationByName(@PathVariable("name") String name) {
    return envirClient.deploymentQuery().getOneById(name);
  }
  
  @PostMapping
  public Uni<EveliDeployment> createOnePublication(@RequestBody CreatePublication publication) {
    
    final var stencilSrc = getOrCreateStencilSrc(publication)
        .onItem().transformToUni(stencil -> getForms(stencil).onItem().transform(forms -> Tuple2.of(stencil, forms)));
    final var hdesSrc = getOrCreateWrenchSrc(publication);

    final var userName = securityClient.getUser().getPrincipal().getUsername();
    final LocalDateTime liveDateTime = Optional.ofNullable(publication.getLiveDate()).orElse(LocalDateTime.now());
    final OffsetDateTime liveDate = OffsetDateTime.of(liveDateTime, ZoneOffset.UTC);
  
    
    return Uni.combine().all().unis(stencilSrc, hdesSrc).asTuple()
      .onItem().transformToUni(src -> envirClient.createOneDeployment()
          .startsAt(liveDate)
          .userId(userName)
          .name(Optional.ofNullable(publication.getName()).orElse(LocalDateTime.now().toString()))
          .wrench(src.getItem2())
          .stencil(src.getItem1().getItem1())
          .dialob(src.getItem1().getItem2())
          .build()
      );
  }
  
  private Uni<AstTag> getOrCreateWrenchSrc(CreatePublication workflowRelease) {    
    final Uni<ComposerState> composerState;
    
    if(workflowRelease.getWrenchTag() == null) {    
      composerState = new HdesComposerImpl(wrenchClient).create(ImmutableCreateEntity.builder()
          .type(AstBodyType.TAG)
          .name(workflowRelease.getWrenchTag())
          .desc("auto-created")
          .build());
    } else {
      composerState = new HdesComposerImpl(wrenchClient).get();
    }
    
    final var tagName = Optional.ofNullable(workflowRelease.getWrenchTag()).orElse(workflowRelease.getName());
    return composerState.onItem().transform(state -> {
      final var rel = state.getTags().values().stream().filter(tag -> tag.getAst().getName().equals(tagName)).findFirst();
      if(rel.isEmpty()) {
        throw new WrenchTagNotFoundException("Wrench tag with name: '" + tagName + "' is not found!");
      }
      return rel.get().getAst();
    });
  }
  
  private Uni<SiteState> getOrCreateStencilSrc(CreatePublication workflowRelease) {    
    final var tagName = Optional.ofNullable(workflowRelease.getStencilTag()).orElse(workflowRelease.getName());
    final var getRelease = stencilClient.getStore().query().head()
      .onItem().transform(state -> state.getReleases().values().stream().filter(f -> f.getBody().getName().equals(tagName)).findFirst())
      .onItem().transformToUni(rel -> {
        if(rel.isEmpty()) {
          throw new StencilTagNotFoundException("Stencil tag with name: '" + tagName + "' is not found!");
        }
        return stencilClient.getStore().query().release(rel.get().getId());
      });
  
    if(workflowRelease.getStencilTag() == null) {
      final Uni<Entity<Release>> createRelease = new StencilComposerImpl(stencilClient).create().release(ImmutableCreateRelease.builder()
          .name(workflowRelease.getStencilTag())
          .note("auto-created")
          .build());
      return createRelease.onItem().transformToUni(createdRelease -> getRelease);
    }
    return getRelease;
  }
  
  
  private Uni<List<Form>> getForms(SiteState site) {    
    final var workflows = site.getWorkflows().values().stream()
      .filter(e -> e.getBody().getFormId() != null)
      .filter(e -> !Boolean.TRUE.equals(e.getBody().getDevMode()))
      .toList();
    
    return Multi.createFrom().items(workflows.stream())
        .onItem().transform(this::getFormIdById)
        .collect().asList();
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
  
  public class StencilTagNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 7190168525508589141L;

    public StencilTagNotFoundException(String message) {
      super(message);
    }
  }
  public class WrenchTagNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 7190168525508589141L;

    public WrenchTagNotFoundException(String message) {
      super(message);
    }
  }
  public class DialobFormNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 7190168525508589141L;

    public DialobFormNotFoundException(String message) {
      super(message);
    }
  }
}
