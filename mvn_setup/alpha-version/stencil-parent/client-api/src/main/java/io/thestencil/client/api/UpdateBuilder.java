package io.thestencil.client.api;

/*-
 * #%L
 * stencil-persistence-api
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import java.util.List;

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.LocaleLabel;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;

public interface UpdateBuilder {
  
  Uni<Entity<Article>> article(ArticleMutator changes);
  Uni<Entity<Locale>> locale(LocaleMutator changes);
  Uni<Entity<Page>> page(PageMutator changes);
  Uni<List<Entity<Page>>> pages(List<PageMutator> changes);
  Uni<Entity<Link>> link(LinkMutator changes);
  Uni<Entity<Template>> template(TemplateMutator changes);
  Uni<Entity<Workflow>> workflow(WorkflowMutator changes);

  
  @Value.Immutable
  @JsonSerialize(as = ImmutableTemplateMutator.class)
  @JsonDeserialize(as = ImmutableTemplateMutator.class)
  interface TemplateMutator {
    String getTemplateId(); 
	String getName();
    String getDescription();
	String getContent();
	String getType();
  }
    
  @Value.Immutable
  @JsonSerialize(as = ImmutableLocaleMutator.class)
  @JsonDeserialize(as = ImmutableLocaleMutator.class)
  interface LocaleMutator {
    String getLocaleId(); 
    String getValue();
    Boolean getEnabled();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableArticleMutator.class)
  @JsonDeserialize(as = ImmutableArticleMutator.class)
  interface ArticleMutator {
    String getArticleId();
    @Nullable
    String getParentId();
    String getName();
    Integer getOrder();
    
    @Nullable
    List<String> getLinks();
    @Nullable
    List<String> getWorkflows();
    @Nullable
    Boolean getDevMode();
  }
  @Value.Immutable
  @JsonSerialize(as = ImmutablePageMutator.class)
  @JsonDeserialize(as = ImmutablePageMutator.class)
  interface PageMutator {
    String getPageId();
    String getContent();
    String getLocale();
    @Nullable
    Boolean getDevMode();
  }
  @Value.Immutable
  @JsonSerialize(as = ImmutableLinkMutator.class)
  @JsonDeserialize(as = ImmutableLinkMutator.class)
  interface LinkMutator {
    String getLinkId();
    String getValue(); 
    String getType();
    @Nullable
    List<LocaleLabel> getLabels();
    @Nullable
    List<String> getArticles();
    @Nullable
    Boolean getDevMode();
  }
  @Value.Immutable
  @JsonSerialize(as = ImmutableWorkflowMutator.class)
  @JsonDeserialize(as = ImmutableWorkflowMutator.class)
  interface WorkflowMutator {
    String getWorkflowId(); 
    String getValue();
    @Nullable
    List<LocaleLabel> getLabels();
    @Nullable
    List<String> getArticles();
    @Nullable
    Boolean getDevMode();
  }
}
