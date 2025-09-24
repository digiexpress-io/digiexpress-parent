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

import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.api.ExternalDeploymentProvider;
import io.digiexpress.eveli.envir.api.ImmutableEveliDeployment;
import io.digiexpress.eveli.envir.api.ImmutableEveliSources;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.entities.doc.Doc;
import io.resys.thena.api.entities.doc.DocBranch;
import io.resys.thena.doc.api.ThenaDocConfig;
import io.resys.thena.doc.spi.DocStoreImpl;
import io.thestencil.client.api.StencilComposer.SiteState;



public class EveliEnvirStore extends DocStoreImpl<EveliEnvirStore> {

  public static String DOC_TYPE_DEPLOYMENT = "deployment";
  
  private final ExternalDeploymentProvider externalProvider;
  private final boolean externalProviderOnly;
  
  public EveliEnvirStore(
      ThenaDocConfig config, 
      DocStoreFactory<EveliEnvirStore> factory,
      ExternalDeploymentProvider externalProvider,
      boolean externalProviderOnly) {
    super(config, factory);
    this.externalProvider = externalProvider;
    this.externalProviderOnly = externalProviderOnly;
  }

  public static Builder<EveliEnvirStore> builder(ExternalDeploymentProvider externalProvider, boolean externalProviderOnly) {
    final DocStoreFactory<EveliEnvirStore> factory = (config, delegate) -> new EveliEnvirStore(config, delegate, externalProvider, externalProviderOnly);
    return new Builder<EveliEnvirStore>(factory);
  }
  
  @Override
  public StoreTenantQuery<EveliEnvirStore> query() {
    final var resp = super.query().repoType(StructureType.doc).repoName(config.getRepoId());
    
    return resp;
  }
  
  public static String formatDescription(String description, SiteState stencil, AstTag wrench) {
    final var desc = new StringBuilder()
      .append("stencil tag: ").append(stencil.getName()).append(System.lineSeparator())
      .append("wrench tag: ").append(wrench.getName()).append(System.lineSeparator())
      .append("workflows: ").append(System.lineSeparator());
    
    for(final var wk : stencil.getWorkflows().values()) {
      final var body = wk.getBody();
      
      final var start = Optional.ofNullable(body.getStartDate()).map(e -> e.toString()).orElse("N/A");
      final var end = Optional.ofNullable(body.getEndDate()).map(e -> e.toString()).orElse("N/A");
      
      
      desc
        .append("  - ").append(body.getValue()).append(System.lineSeparator())
        .append("    start-end/anon/dev: ")
          .append(start).append(" - ").append(end)
          .append("/").append(Boolean.TRUE.equals(body.getAnon()))
          .append("/").append(Boolean.TRUE.equals(body.getDevMode()))
        .append(System.lineSeparator())
        .append("    flow name: ").append(body.getFlowName()).append(System.lineSeparator())
        .append("    dialob id: ").append(body.getFormId()).append(System.lineSeparator())
        .append("    dialob name/tag: ").append(body.getFormName()).append("/").append(body.getFormTag()).append(System.lineSeparator())
        .append("    articles (").append(body.getArticles().size()).append("): ").append(System.lineSeparator());
      for(final var articleId : body.getArticles()) {
        final var article = stencil.getArticles().get(articleId);
        desc.append("      - ").append(article.getBody().getName()).append(System.lineSeparator());;
      }
      
    }
    
    if(description != null && !description.isBlank()) {
      desc.insert(0, description + System.lineSeparator() + System.lineSeparator());
    }
    return desc.toString();
  }
  
  public static EveliDeployment map(Doc doc, Optional<DocBranch> branch) {
    return ImmutableEveliDeployment.builder()
        .id(doc.getId())
        .externalId(doc.getExternalId())
        .startsAt(doc.getStartsAt())
        .status(EveliDeploymentStatus.valueOf(doc.getSubStatus()))
        .externalId(doc.getExternalId())
        .createdAt(doc.getCreatedAt())
        .name(doc.getName())
        .createdBy(doc.getOwnerId())
        .description(doc.getDescription())
        .errors(doc.getMeta())
        .sources(branch
            .filter(src -> !src.getValue().isEmpty())
            .map(src -> src.getValue().mapTo(ImmutableEveliSources.class))
            .orElse(null))
        .build();
  }

  public ExternalDeploymentProvider getExternalProvider() {
    return externalProvider;
  }

  public boolean isExternalProviderOnly() {
    return externalProviderOnly;
  }
}
