package io.digiexpress.eveli.assets.spi.builders;

/*-
 * #%L
 * eveli-assets
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import io.dialob.api.form.Form;
import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Publication;
import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.Deployment;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.DeploymentBuilder;
import io.digiexpress.eveli.assets.api.ImmutableDeployment;
import io.digiexpress.eveli.assets.spi.exceptions.AssetsAssert;
import io.digiexpress.eveli.assets.spi.exceptions.AssetsAssert.EveliAssetsAssertException;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.ast.AstTag;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilComposer.SiteState;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DeploymentBuilderImpl implements DeploymentBuilder {
  private String idOrName;
  
  private final EveliAssetClient client;
  private final StencilClient stencilClient;
  private final HdesClient hdesClient;
  private final DialobClient dialobClient;
  
  @Override
  public DeploymentBuilder id(String idOrName) {
    this.idOrName = idOrName;
    return this;
  }

  @Override
  public Uni<Optional<Deployment>> build() {
    AssetsAssert.notNull(idOrName, () -> "idOrName must be defined!");
    
    return client.queryBuilder().findOnePublicationByName(idOrName)
    .onItem().transformToUni(resp -> {
      
      // query by id
      if(resp.isEmpty()) {
        return client.queryBuilder().findAllPublicationsById(Arrays.asList(idOrName)).onItem().transform(e -> e.stream().findFirst());
      }
      
      return Uni.createFrom().item(resp);
    }).onItem().transformToUni(publication -> {
      
      // nothing to do
      if(publication.isEmpty()) {
        return Uni.createFrom().item(Optional.empty());
      }
      
      return createDeployment(publication.get().getBody());
    });
  }

  
  private Uni<Optional<Deployment>> createDeployment(Publication publication) {
    return Uni.combine().all().unis(
        getWrench(publication), 
        getStencil(publication),
        getDialob(publication),
        getWorkflow(publication)
    ).asTuple().onItem().transform(tuple -> {
      
      
      final var deployment = ImmutableDeployment.builder()
          .addAllDialobTag(tuple.getItem3())
          .stencilTag(tuple.getItem2())
          .wrenchTag(tuple.getItem1())
          .workflowTag(tuple.getItem4())
          .source(publication)
          .created(LocalDateTime.now())
          .build();
      
      return Optional.of(deployment);
    });
  }
  

  private Uni<AstTag> getWrench(Publication publication) {
    return hdesClient.store().query().get().onItem().transform(state -> {
      for(final var src : state.getTags().values()) {
        final var tag = hdesClient.ast().commands(src.getBody()).tag();
        final var name = tag.getName();
        if(name.equals(publication.getWrenchTagName())) {
          return tag;
        }
      }
      throw new EveliAssetsAssertException("Can't find wrench tag: " + publication.getWrenchTagName() + "!");
    });
  }
  
  private Uni<SiteState> getStencil(Publication publication) {
    return stencilClient.getStore().query().head().onItem().transformToUni(e -> {
      
      final var release = e.getReleases().values().stream()
          .filter(r -> r.getBody().getName().equals(publication.getStencilTagName())).findFirst()
          .orElseThrow(() -> new EveliAssetsAssertException("Can't find stencil tag: " + publication.getStencilTagName() + "!"));
      return stencilClient.getStore().query().release(release.getId());
    });
  }
  
  private Uni<List<Form>> getDialob(Publication publication) {
    return client.queryBuilder().findOneWorkflowTagByName(publication.getWorkflowTagName())
    .onItem().transform(wk -> {
      
      return wk
        .orElseThrow(() -> new EveliAssetsAssertException("Can't find workflow tag: " + publication.getWorkflowTagName() + "!"))
        .getBody().getEntries().stream()
        .map(entry -> dialobClient.getFormById(entry.getFormId()))
        .toList();
    });
  }
  
  private Uni<WorkflowTag> getWorkflow(Publication publication) {
    return client.queryBuilder().findOneWorkflowTagByName(publication.getWorkflowTagName())
    .onItem().transform(wk -> {
      return wk
        .orElseThrow(() -> new EveliAssetsAssertException("Can't find workflow tag: " + publication.getWorkflowTagName() + "!"))
        .getBody();
    }); 
  }
}
