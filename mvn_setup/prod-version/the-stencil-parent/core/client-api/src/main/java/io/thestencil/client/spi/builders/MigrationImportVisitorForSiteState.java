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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import io.thestencil.client.api.ImmutableArticle;
import io.thestencil.client.api.ImmutableBatchCommand;
import io.thestencil.client.api.ImmutableEntity;
import io.thestencil.client.api.ImmutableLink;
import io.thestencil.client.api.ImmutableLocale;
import io.thestencil.client.api.ImmutableLocaleLabel;
import io.thestencil.client.api.ImmutablePage;
import io.thestencil.client.api.ImmutableWorkflow;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.LocaleLabel;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore.BatchCommand;

public class MigrationImportVisitorForSiteState {
  private final ImmutableBatchCommand.Builder batch;
  private final SiteState current;
  
  private final Map<String, Entity<Article>> articlesByTopicName = new LinkedHashMap<>();
  private final Map<String, Entity<Link>> links = new LinkedHashMap<>();
  private final Map<String, Entity<Workflow>> workflows = new LinkedHashMap<>();
  private final Map<String, Entity<Locale>> locales = new LinkedHashMap<>();
  private final Map<String, Entity<Page>> pages = new LinkedHashMap<>();
  // new to -> old id
  private final Map<String, String> idMap = new HashMap<>();
  private final List<String> commitedIds = new ArrayList<>();
  
  
  public MigrationImportVisitorForSiteState(SiteState current) {
    super();
    this.current = current;
    this.batch = ImmutableBatchCommand.builder();
  }
  
  public BatchCommand visit(SiteState sites) {
    
    visitCurrentStateStart(current);
    
    sites.getLocales().forEach((id, locale) -> visitLocale(locale));
    sites.getArticles().forEach((id, article) -> visitArticle(article));
    sites.getPages().forEach((id, page) -> visitPage(page));
    sites.getWorkflows().forEach((id, workflow) -> visitWorkflow(workflow));
    sites.getLinks().forEach((id, link) -> visitLink(link));

    visitCurrentStateEnd(current);
    
    return this.batch.build();
  }
  
  private void visitCurrentStateStart(SiteState current) {
    current.getLocales().values().stream()
    .forEach(e -> {
      this.locales.put(e.getBody().getValue(), e);
      visitCommit(e);
    });
    current.getPages().values().stream()
    .forEach(e -> {
      this.pages.put(pageId(e), e);
      visitCommit(e);
    });
    
    current.getArticles().values().stream()
    .forEach(e -> {
       articlesByTopicName.put(e.getBody().getName(), e);
       visitCommit(e);
    });
    current.getLinks().values().stream()
    .forEach(e -> {
      final var topicLinkId = topicLinkId(e);
      this.links.put(topicLinkId, e);
      visitCommit(e);
    });
    current.getWorkflows().values().stream()
    .forEach(e -> {
      final var topicLinkId = workflowId(e);
      this.workflows.put(topicLinkId, e);
      visitCommit(e);
    });    
  }
  
  private void visitCurrentStateEnd(SiteState current) {
    current.getLocales().values().stream()
      .filter(e -> !commitedIds.contains(e.getId()))
      .forEach(e -> batch.addToBeDeleted(e));
    current.getPages().values().stream()
      .filter(e -> !commitedIds.contains(e.getId()))
      .forEach(e -> batch.addToBeDeleted(e));
    current.getLinks().values().stream()
      .filter(e -> !commitedIds.contains(e.getId()))
      .forEach(e -> batch.addToBeDeleted(e));
    current.getArticles().values().stream()
      .filter(e -> !commitedIds.contains(e.getId()))
      .forEach(e -> batch.addToBeDeleted(e));
    current.getWorkflows().values().stream()
      .filter(e -> !commitedIds.contains(e.getId()))
      .forEach(e -> batch.addToBeDeleted(e));
  }
  
  private void visitCommit(Entity<?> entity) {
    if(commitedIds.contains(entity.getId())) {
      throw new IllegalArgumentException("id already in commit: " + entity.getId());
    }
    commitedIds.add(entity.getId());
    batch.addToBeCreated(entity);
  }
  
  private Entity<Workflow> visitWorkflow(Entity<Workflow> input) {
    final var workflowId = workflowId(input);
    final var newArticles = input.getBody()
        .getArticles().stream().map(e -> getSavedId(e))
        .collect(Collectors.toList());
    
    if(workflows.containsKey(workflowId)) {
      final var created = workflows.get(workflowId);
      final List<String> articles = merge(created.getBody().getArticles(), newArticles);      
      
      final var next = ImmutableEntity.<Workflow>builder()
          .from(created)
          .body(ImmutableWorkflow.builder()
              .from(created.getBody())
              .articles(articles)
              .addAllLabels(merge(created.getBody().getLabels(), input.getBody().getLabels()))
              .build())
          .build();
      this.idMap.put(input.getId(), created.getId());
      this.workflows.put(workflowId, next);
      return next;
    }
    
    final var gid = input.getId();
    final var workflow = ImmutableWorkflow.builder()
      .value(input.getBody().getValue()) // pointer
      .addAllLabels(input.getBody().getLabels().stream()
          .map(label -> ImmutableLocaleLabel.builder()
              .labelValue(label.getLabelValue())
              .locale(getSavedId(label.getLocale()))
              .build())
          .collect(Collectors.toList()))
      .articles(newArticles)
      .build();
    final Entity<Workflow> entity = ImmutableEntity.<Workflow>builder()
        .id(gid)
        .type(EntityType.WORKFLOW)
        .body(workflow)
        .build();
    workflows.put(workflowId, entity);
    visitCommit(entity);
    return entity;
  }

  private Entity<Link> visitLink(Entity<Link> input) {
    final var topicLinkId = topicLinkId(input);
    final var newArticles = input.getBody()
        .getArticles().stream().map(e -> getSavedId(e))
        .collect(Collectors.toList());
    
    if(links.containsKey(topicLinkId)) {
      
      final var created = links.get(topicLinkId);
      final List<String> articles = merge(created.getBody().getArticles(), newArticles);      
    
      final var next = ImmutableEntity.<Link>builder()
          .from(created)
          .body(ImmutableLink.builder()
              .from(created.getBody())
              .articles(articles)
              .addAllLabels(merge(created.getBody().getLabels(), input.getBody().getLabels()))
              .build())
          .build();
      links.put(topicLinkId, next);
      return next;
    }
    
    final var gid = input.getId();
    
    final var link = ImmutableLink.builder()
      .contentType(input.getBody().getContentType())
      .value(input.getBody().getValue())
      .addAllLabels(input.getBody().getLabels().stream()
          .map(label -> ImmutableLocaleLabel.builder()
              .labelValue(label.getLabelValue())
              .locale(getSavedId(label.getLocale()))
              .build())
          .collect(Collectors.toList()))
      .articles(newArticles)
      .build();
    
    final Entity<Link> entity = ImmutableEntity.<Link>builder()
      .id(gid)
      .type(EntityType.LINK)
      .body(link)
      .build();
    links.put(topicLinkId, entity);
    visitCommit(entity);
    return entity;
  }

  private String topicLinkId(Entity<Link> topicLink) {
    return topicLink.getBody().getContentType() + "::" + topicLink.getBody().getValue();
  }
  private String workflowId(Entity<Workflow> topicLink) {
    return "workflow::" + topicLink.getBody().getValue();
  }
  private String pageId(Entity<Page> page) {
    return page.getBody().getArticle() + "::" + page.getBody().getLocale();
  }
  
  private Entity<Page> visitPage(Entity<Page> input) {
    final var gid = input.getId();
    final var page = ImmutablePage.builder()
        .article(getSavedId(input.getBody().getArticle()))
        .locale(getSavedId(input.getBody().getLocale()))
        .content(Optional.ofNullable(input.getBody().getContent()).orElse(""))
        .build();
    
    final Entity<Page> entity = ImmutableEntity.<Page>builder()
        .id(gid)
        .type(EntityType.PAGE)
        .body(page)
        .build();
    final var pageId = pageId(entity);
    
    // merge
    if(pages.containsKey(pageId)) {
      final var old = pages.get(pageId);
      idMap.put(input.getId(), old.getId());
      final var result = ImmutableEntity.<Page>builder()
          .from(old)
          .body(page)
          .build();
      return result;
    }
    
    
    pages.put(pageId, entity);
    visitCommit(entity);
    return entity;
  }
  
  private Entity<Article> visitArticle(Entity<Article> input) {
    final var name = input.getBody().getName();
    final var parentId = getSavedId(input.getBody().getParentId());
    final var order = input.getBody().getOrder();
    final var gid = input.getId();
    
    if(articlesByTopicName.containsKey(name)) {
      final var old = articlesByTopicName.get(name);
      final var article = ImmutableArticle.builder()
          .name(name)
          .parentId(parentId)
          .order(order)
          .build();
      final Entity<Article> entity = ImmutableEntity.<Article>builder()
          .from(old)
          .body(article)
          .build();
      
      idMap.put(gid, old.getId());
      return articlesByTopicName.put(article.getName(), entity);
    }
  
    final var article = ImmutableArticle.builder()
        .name(name)
        .parentId(parentId)
        .order(order)
        .build();
    final Entity<Article> entity = ImmutableEntity.<Article>builder()
        .id(gid)
        .type(EntityType.ARTICLE)
        .body(article)
        .build();

    articlesByTopicName.put(name, entity);
    visitCommit(entity);
    return entity;
  }
  
  private Entity<Locale> visitLocale(Entity<Locale> input) {
    if(this.locales.containsKey(input.getBody().getValue())) {
      final var old = locales.get(input.getBody().getValue());
      idMap.put(input.getId(), old.getId());
      return old;
    }

    final var locale = ImmutableLocale.builder()
        .value(input.getBody().getValue())
        .enabled(true)
        .build();
    
    final Entity<Locale> entity = ImmutableEntity.<Locale>builder()
        .id(input.getId())
        .type(EntityType.LOCALE)
        .body(locale)
        .build();
    this.locales.put(input.getBody().getValue(), entity);
    visitCommit(entity);
    return entity;
  }
  
  private List<String> merge(Collection<String> input1, Collection<String> input2 ) {
    List<String> result = new ArrayList<>();
    
    for(final var val : input1) {
      if(result.contains(val)) {
        continue;
      }
      result.add(val);
    }
    for(final var val : input2) {
      if(result.contains(val)) {
        continue;
      }
      result.add(val);
    }
    
    return result;
  }
  
  private Collection<LocaleLabel> merge(List<LocaleLabel> oldEntries, List<LocaleLabel> newEntries) {
    Map<String, LocaleLabel> labels = new LinkedHashMap<>();
    
    for(final var entry : oldEntries) {
      labels.put(entry.getLocale(), entry);
    }

    for(final var entry : newEntries) {
      final var id = getSavedId(entry.getLocale());
      labels.put(id, entry);
    }
    
    return labels.values();
  }

  //new id -> old
  private String getSavedId(String gid) {
    if(this.idMap.containsKey(gid)) {
      return this.idMap.get(gid);
    }
    return gid;
  }
  
//  private String gid(EntityType type) {
//    return config.getGidProvider().getNextId(type);
//  }

}
