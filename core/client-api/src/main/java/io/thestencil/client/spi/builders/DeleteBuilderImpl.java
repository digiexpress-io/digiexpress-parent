package io.thestencil.client.spi.builders;

/*-
 * #%L
 * stencil-persistence
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

import java.util.stream.Collectors;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.DeleteBuilder;
import io.thestencil.client.api.ImmutableEntity;
import io.thestencil.client.api.ImmutableLink;
import io.thestencil.client.api.ImmutableWorkflow;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilConfig.EntityState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DeleteBuilderImpl implements DeleteBuilder {
  private final StencilClient client;

  @Override
  public Uni<Entity<Article>> article(String articleId) {
    // Delete the article
    return new ArticleDeleteVisitor(client, articleId).visit();
  
  }
  @Override
  public Uni<Entity<Locale>> locale(String localeId) {
    // Get the locale
    final Uni<EntityState<Locale>> query = client.getStore().get(localeId, EntityType.LOCALE);
    
    // Delete the locale
    return query.onItem().transformToUni(state -> client.getStore().delete(state.getEntity()));
  }
  
  @Override
  public Uni<Entity<Template>> template(String templateId) {
    // Get the page
    final Uni<EntityState<Template>> query = client.getStore().get(templateId, EntityType.TEMPLATE);
    
    // Delete the template
    return query.onItem().transformToUni(state -> client.getStore().delete(state.getEntity()));
  }

  @Override
  public Uni<Entity<Page>> page(String pageId) {
    // Get the page
    final Uni<EntityState<Page>> query = client.getStore().get(pageId, EntityType.PAGE);
    
    // Delete the page
    return query.onItem().transformToUni(state -> client.getStore().delete(state.getEntity()));
  }

  @Override
  public Uni<Entity<Link>> link(String linkId) {
    // Get the link
    final Uni<EntityState<Link>> query = client.getStore().get(linkId, EntityType.LINK);
    
    // Delete the link
    return query.onItem().transformToUni(state -> client.getStore().delete(state.getEntity()));
  }
  
  @Override
  public Uni<Entity<Workflow>> workflow(String workflowId) {
    // Get the workflow
    final Uni<EntityState<Workflow>> query = client.getStore().get(workflowId, EntityType.WORKFLOW);
    
    // Delete the workflow
    return query.onItem().transformToUni(state -> client.getStore().delete(state.getEntity()));
  }

  @Override
  public Uni<Entity<Link>> linkArticlePage(LinkArticlePage linkArticlePage) {
    
    // Get the link
    final Uni<EntityState<Link>> query = client.getStore().get(linkArticlePage.getLinkId(), EntityType.LINK);
    
    return query.onItem().transformToUni(state -> {
      final Entity<Link> start = state.getEntity();
      
      var newArticles = start.getBody()
          .getArticles().stream().filter(a -> !a.equals(linkArticlePage.getArticleId()))
          .collect(Collectors.toList());
      
      if(newArticles.size() == start.getBody().getArticles().size()) {
        return Uni.createFrom().item(start);
      }
      
      Entity<Link> end = ImmutableEntity.<Link>builder()
        .id(start.getId()).type(start.getType())
        .body(ImmutableLink.builder().from(start.getBody())
            .articles(newArticles)
            .build())
        .build();
      
      return client.getStore().save(end);
    });
  }

  @Override
  public Uni<Entity<Workflow>> workflowArticlePage(WorkflowArticlePage workflowArticlePage) {
    // Get the workflow
    final Uni<EntityState<Workflow>> query = client.getStore().get(workflowArticlePage.getWorkflowId(), EntityType.WORKFLOW);
    
    return query.onItem().transformToUni(state -> {
      final Entity<Workflow> start = state.getEntity();
      
      var newArticles = start.getBody()
          .getArticles().stream().filter(a -> !a.equals(workflowArticlePage.getArticleId()))
          .collect(Collectors.toList());
      
      if(newArticles.size() == start.getBody().getArticles().size()) {
        return Uni.createFrom().item(start);
      }
      
      Entity<Workflow> end = ImmutableEntity.<Workflow>builder()
        .id(start.getId()).type(start.getType())
        .body(ImmutableWorkflow.builder().from(start.getBody())
            .articles(newArticles)
            .build())
        .build();
      return client.getStore().save(end);
    });
  }

  @Override
  public Uni<Entity<Release>> release(String releaseId) {
    // Get the release
    final Uni<EntityState<Release>> query = client.getStore().get(releaseId, EntityType.RELEASE);
    
    // Delete the release
    return query.onItem().transformToUni(state -> client.getStore().delete(state.getEntity()));
  }
}
