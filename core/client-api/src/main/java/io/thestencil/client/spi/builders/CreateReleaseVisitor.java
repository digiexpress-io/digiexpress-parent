package io.thestencil.client.spi.builders;

/*-
 * #%L
 * stencil-client
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.resys.thena.docdb.spi.commits.Sha2;
import io.thestencil.client.api.ImmutableArticleReleaseItem;
import io.thestencil.client.api.ImmutableLinkReleaseItem;
import io.thestencil.client.api.ImmutableLocaleReleaseItem;
import io.thestencil.client.api.ImmutablePageReleaseItem;
import io.thestencil.client.api.ImmutableRelease;
import io.thestencil.client.api.ImmutableWorkflowReleaseItem;
import io.thestencil.client.api.StencilClient.ArticleReleaseItem;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.LinkReleaseItem;
import io.thestencil.client.api.StencilClient.LocaleReleaseItem;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.PageReleaseItem;
import io.thestencil.client.api.StencilClient.WorkflowReleaseItem;
import io.thestencil.client.api.StencilComposer.SiteState;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CreateReleaseVisitor {

  private final SiteState state;
  private final Set<String> enabledLocales = new HashSet<>();
  private final Set<String> enabledArticles = new HashSet<>();

  public ImmutableRelease.Builder visit(ImmutableRelease.Builder releaseBuilder) {
    return releaseBuilder
      .addAllLocales(visitLocale())
      .addAllPages(visitPage())
      .addAllArticles(visitArticle())
      .addAllWorkflows(visitWorkflow())
      .addAllLinks(visitLink());
  }

  private List<LocaleReleaseItem> visitLocale() {
    final var result = new ArrayList<LocaleReleaseItem>();

    for (final var locale : this.state.getLocales().values()) {
      if (locale.getBody().getEnabled()) {
        enabledLocales.add(locale.getId());
        
        result.add(ImmutableLocaleReleaseItem.builder()
            .id(locale.getId())
            .value(locale.getBody().getValue())
            .hash(Sha2.blobId(locale.getBody().getValue().toString()))
            .build());
      }
    }
    return result;
  }

  private List<PageReleaseItem> visitPage() {
    final var result = new ArrayList<PageReleaseItem>();
    for(final var page : this.state.getPages().values()) {
      if(!enabledLocales.contains(page.getBody().getLocale())) {
        continue;
      }
      this.enabledArticles.add(page.getBody().getArticle());
      result.add(ImmutablePageReleaseItem.builder()
          .id(page.getId())
          .hash(Sha2.blobId(page.getBody().toString()))
          .locale(page.getBody().getLocale())
          .h1(visitH1(page))
          .build());
    }
    
    return result;
  }
  
  private String visitH1(Entity<Page> page) {
    final var h1Start = page.getBody().getContent().indexOf("# ");
    if(h1Start < 0) {
      return "";
    }
    
    var lineEnd = page.getBody().getContent().indexOf("\r\n", h1Start);
    if(lineEnd < 0) {
      lineEnd = page.getBody().getContent().indexOf("\n", h1Start);
    }
    if(lineEnd < 0) {
      return page.getBody().getContent().substring(2).trim();
    }
    return page.getBody().getContent().substring(2, lineEnd).trim();
  }

  private List<ArticleReleaseItem> visitArticle() {
    final var result = new ArrayList<ArticleReleaseItem>();
    for(final var article : this.state.getArticles().values()) {
      if(!this.enabledArticles.contains(article.getId())) {
        continue;
      }
      
      final var item = ImmutableArticleReleaseItem.builder()
          .id(article.getId())
          .hash("")
          .parentId(article.getBody().getParentId())
          .name(article.getBody().getName())
          .build();
      
      result.add(ImmutableArticleReleaseItem.builder().from(item)
          .hash(Sha2.blobId(item.toString()))
          .build());
    }
    return result;
  }

  private List<LinkReleaseItem> visitLink() {
    final var result = new ArrayList<LinkReleaseItem>();
    for(final var link : this.state.getLinks().values()) {
      
      final var labels = link.getBody().getLabels().stream()
        .filter(label -> enabledLocales.contains(label.getLocale()))
        .collect(Collectors.toList());
      
      final var articles = link.getBody().getArticles().stream()
          .filter(article -> enabledArticles.contains(article))
          .collect(Collectors.toList());
      
      if(articles.isEmpty() || labels.isEmpty()) {
        continue;
      }
      
      final var item = ImmutableLinkReleaseItem.builder()
          .id(link.getId())
          .hash("")
          .contentType(link.getBody().getContentType())
          .value(link.getBody().getValue())
          .addAllLabels(labels)
          .addAllArticles(articles)
          .build();
      
      result.add(ImmutableLinkReleaseItem.builder().from(item)
          .hash(Sha2.blobId(item.toString()))
          .build());
    }
    return result;
  }

  private List<WorkflowReleaseItem> visitWorkflow() {
    final var result = new ArrayList<WorkflowReleaseItem>();
    for(final var workflow : this.state.getWorkflows().values()) {
      
      final var labels = workflow.getBody().getLabels().stream()
        .filter(label -> enabledLocales.contains(label.getLocale()))
        .collect(Collectors.toList());
      
      final var articles = workflow.getBody().getArticles().stream()
          .filter(article -> enabledArticles.contains(article))
          .collect(Collectors.toList());
      
      if(articles.isEmpty() || labels.isEmpty()) {
        continue;
      }
      
      final var item = ImmutableWorkflowReleaseItem.builder()
          .id(workflow.getId())
          .hash("")
          .value(workflow.getBody().getValue())
          .addAllLabels(labels)
          .addAllArticles(articles)
          .build();
      
      result.add(ImmutableWorkflowReleaseItem.builder().from(item)
          .hash(Sha2.blobId(item.toString()))
          .build());
    }
    return result;
  }
}
