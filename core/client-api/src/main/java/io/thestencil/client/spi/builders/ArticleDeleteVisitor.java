package io.thestencil.client.spi.builders;

import java.util.ArrayList;

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

import java.util.Optional;
import java.util.stream.Collectors;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableArticle;
import io.thestencil.client.api.ImmutableBatchCommand;
import io.thestencil.client.api.ImmutableEntity;
import io.thestencil.client.api.ImmutableLink;
import io.thestencil.client.api.ImmutableWorkflow;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.StencilAssert;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArticleDeleteVisitor {
  private final StencilClient client;
  private final String articleId;
  
  
  public Uni<Entity<Article>> visit() {
    return client.getStore().query().head().onItem()
        .transformToUni(state -> visitObjects(state));
  }
  
  private Uni<Entity<Article>> visitObjects(SiteState state) {
    final var start = visitArticleId(state, articleId);
    final var updateCommand = ImmutableBatchCommand.builder();

    for(final var page : state.getPages().values()) {
      visitPage(page).ifPresent(changeEntity -> updateCommand.addToBeDeleted(changeEntity));
    }
    
    for(final var wrkf : state.getWorkflows().values()) {
      visitWorkflow(wrkf).ifPresent(changeEntity -> updateCommand.addToBeSaved(changeEntity));
    }
    
    for(final var link : state.getLinks().values()) {
      visitLink(link).ifPresent(changeEntity -> updateCommand.addToBeSaved(changeEntity));
    }
    
    
    updateCommand.addToBeDeleted(start);
    
    final var changedArticles = new ArrayList<Entity<Article>>();
    for(final var article : state.getArticles().values()) {
      if(article.getId().equals(articleId)) {
        continue;
      }
      visitArticle(article).ifPresent(changeEntity -> {
        changedArticles.add(changeEntity);
        updateCommand.addToBeSaved(changeEntity);
      });
    }
    
    
    
    return client.getStore()
        .batch(updateCommand.build())
        .onItem().transform(updated -> start);
  }
  
  public Optional<Entity<Article>> visitArticle(Entity<Article> start) {
    
    if(start.getBody().getParentId() != null  && 
        start.getBody().getParentId().equals(articleId)) {
      return Optional.of(ImmutableEntity.<Article>builder()
          .id(start.getId()).type(start.getType())
          .body(ImmutableArticle.builder().from(start.getBody())
              .parentId(null)
              .build())
          .build());
    }
    
    return Optional.empty();
  }

  public Optional<Entity<?>> visitLink(Entity<Link> start) {
    var newArticles = start.getBody()
        .getArticles().stream().filter(a -> !a.equals(articleId))
        .collect(Collectors.toList());
    
    if(newArticles.size() == start.getBody().getArticles().size()) {
      return Optional.empty();
    }
    
    return Optional.of(ImmutableEntity.<Link>builder()
        .id(start.getId()).type(start.getType())
        .body(ImmutableLink.builder().from(start.getBody())
            .articles(newArticles)
            .build())
        .build());
  }
  
  public Optional<Entity<?>> visitPage(Entity<Page> page) {
    if(page.getBody().getArticle().equals(articleId)) {
      return Optional.of(page);
    }
    return Optional.empty();
  }
  public Optional<Entity<?>> visitWorkflow(Entity<Workflow> start) {
    var newArticles = start.getBody()
        .getArticles().stream().filter(a -> !a.equals(articleId))
        .collect(Collectors.toList());
    
    if(newArticles.size() == start.getBody().getArticles().size()) {
      return Optional.empty();
    }
    
    return Optional.of(ImmutableEntity.<Workflow>builder()
        .id(start.getId()).type(start.getType())
        .body(ImmutableWorkflow.builder().from(start.getBody())
            .articles(newArticles)
            .build())
        .build());
  }
  
  private Entity<Article> visitArticleId(SiteState state, String articleId) {
    final var article = state.getArticles().get(articleId);
    StencilAssert.isTrue(article != null, () -> "Can't find article with id: '" + articleId + "'");
    return article;
  }
}
