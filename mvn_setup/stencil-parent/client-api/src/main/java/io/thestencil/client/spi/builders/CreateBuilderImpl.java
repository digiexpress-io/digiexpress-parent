package io.thestencil.client.spi.builders;

import java.util.ArrayList;
import java.util.List;

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

import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.CreateBuilder;
import io.thestencil.client.api.ImmutableArticle;
import io.thestencil.client.api.ImmutableEntity;
import io.thestencil.client.api.ImmutableLink;
import io.thestencil.client.api.ImmutableLocale;
import io.thestencil.client.api.ImmutableLocaleLabel;
import io.thestencil.client.api.ImmutablePage;
import io.thestencil.client.api.ImmutableTemplate;
import io.thestencil.client.api.ImmutableWorkflow;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityBody;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilStore;
import io.thestencil.client.spi.exceptions.ConstraintException;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateBuilderImpl implements CreateBuilder {
  private final StencilStore client;

  @Override
  public Uni<List<Entity<?>>> batch(BatchSite batch) {
    final Uni<SiteState> query = client.stencilQuery().head();
    return query.onItem().transformToUni(state -> client.batch(new BatchSiteCommandVisitor(state, client).visit(batch)));
  }
  @Override
  public Uni<Entity<Article>> article(CreateArticle init) {
    final Uni<SiteState> query = client.stencilQuery().head();
    return query.onItem().transformToUni(state -> client.create(article(init, state, client)));
  }
  @Override
  public Uni<Entity<Template>> template(CreateTemplate init) {
    final Uni<SiteState> query = client.stencilQuery().head();
    return query.onItem().transformToUni(state -> client.create(template(init, state, client)));
  }
  @Override
  public Uni<Entity<Locale>> locale(CreateLocale init) {
    final Uni<SiteState> query = client.stencilQuery().head();
    return query.onItem().transformToUni(state -> client.create(locale(init, state, client)));
  }
  @Override
  public Uni<Entity<Page>> page(CreatePage init) {
    final Uni<SiteState> query = client.stencilQuery().head();
    return query.onItem().transformToUni(state -> client.create(page(init, state, client)));
  }
  @Override
  public Uni<Entity<Link>> link(CreateLink init) {
    final Uni<SiteState> query = client.stencilQuery().head();
    return query.onItem().transformToUni(state -> client.create(link(init, state, client)));
  }
  @Override
  public Uni<Entity<Workflow>> workflow(CreateWorkflow init) {
    final Uni<SiteState> query = client.stencilQuery().head();
    return query.onItem().transformToUni(state -> client.create(workflow(init, state, client)));
  }

  
  public static Entity<Template> template(CreateTemplate init, SiteState state, StencilStore client) {
    final var gid = OidUtils.gen();
    final var template = ImmutableTemplate.builder()
        .name(init.getName())
        .description(init.getDescription())
        .type(init.getType())
        .content(init.getContent())
        .build();
    final Entity<Template> entity = ImmutableEntity.<Template>builder()
        .id(gid)
        .type(EntityType.TEMPLATE)
        .body(template)
        .build();
    
    final var duplicate = state.getTemplates().values().stream()
        .filter(p -> p.getBody().getName().equals(init.getName()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      throw new ConstraintException(entity, "Template: '" + init.getName() + "' already exists!");
    }
    return assertUniqueId(entity, state);
  }
  
  public static Entity<Article> article(CreateArticle init, SiteState state, StencilStore client) {
    final var gid = OidUtils.gen();
    final var article = ImmutableArticle.builder()
        .devMode(init.getDevMode())
        .name(init.getName())
        .parentId(init.getParentId())
        .order(Optional.ofNullable(init.getOrder()).orElse(0))
        .build();
    final Entity<Article> entity = ImmutableEntity.<Article>builder()
        .id(gid)
        .type(EntityType.ARTICLE)
        .body(article)
        .build();
    
    final var duplicate = state.getArticles().values().stream()
        .filter(p -> p.getBody().getName().equals(init.getName()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      throw new ConstraintException(entity, "Article: '" + init.getName() + "' already exists!");
    }

    if(init.getParentId() != null && !state.getArticles().containsKey(init.getParentId())) {
      throw new ConstraintException(entity, "Article: '" + init.getName() + "', parent: '" + init.getParentId() + "' does not exist!");
    }
    return assertUniqueId(entity, state);
  }
  
  public static Entity<Locale> locale(CreateLocale init, SiteState state, StencilStore client) {
    final var gid = OidUtils.gen();
    final var locale = ImmutableLocale.builder()
        .value(init.getLocale())
        .enabled(true)
        .build();
    
    final Entity<Locale> entity = ImmutableEntity.<Locale>builder()
        .id(gid)
        .type(EntityType.LOCALE)
        .body(locale)
        .build();
    
    final var duplicate = state.getLocales().values().stream()
        .filter(p -> p.getBody().getValue().equals(init.getLocale()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      throw new ConstraintException(entity, "Locale: '" + init.getLocale() + "' already exists!");
    }
    return assertUniqueId(entity, state);
  }
  
  public static Entity<Page> page(CreatePage init, SiteState state, StencilStore client) {
    final var gid = OidUtils.gen();
    final var localeRef = init.getLocale();
    final var locale = resolveLocale(localeRef, state);
    
    final var articleRef = init.getArticleId();
    final var article = state.getArticles().containsKey(articleRef) ? 
        Optional.of(state.getArticles().get(articleRef)) : 
        state.getArticles().values().stream().filter(l -> l.getBody().getName().equalsIgnoreCase(articleRef)).findFirst();

    final var page = ImmutablePage.builder()
        .devMode(init.getDevMode())
        .article(article.map(e -> e.getId()).orElse(articleRef))
        .locale(locale.map(e -> e.getId()).orElse(localeRef))
        .content(Optional.ofNullable(init.getContent()).orElse(""))
        .build();
    
    final Entity<Page> entity = ImmutableEntity.<Page>builder()
        .id(gid)
        .type(EntityType.PAGE)
        .body(page)
        .build();

    if(locale.isEmpty()) {
      throw new ConstraintException(entity, "Locale with id: '" + localeRef + "' does not exist in: '" + String.join(",", state.getLocales().keySet()) + "'!");          
    }
    if(article.isEmpty()) {
      throw new ConstraintException(entity, "Article with id: '" + articleRef + "' does not exist in: '" + String.join(",", state.getArticles().keySet()) + "'!");          
    }

    final var duplicate = state.getPages().values().stream()
        .filter(p -> p.getBody().getArticle().equals(init.getArticleId()))
        .filter(p -> p.getBody().getLocale().equals(init.getLocale()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      throw new ConstraintException(entity, "Page locale with id: '" + locale.get().getId() + "' already exists!");
    }
    return assertUniqueId(entity, state);
  }
  
  public static Entity<Link> link(CreateLink init, SiteState state, StencilStore client) {
    final var gid = OidUtils.gen();
    final var link = ImmutableLink.builder()
      .devMode(init.getDevMode())
      .contentType(init.getType())
      .value(init.getValue());
    
    final var articles = new ArrayList<String>();
    for(final var articleRef : init.getArticles()) {
      final var article = state.getArticles().containsKey(articleRef) ? 
          Optional.of(state.getArticles().get(articleRef)) : 
          state.getArticles().values().stream().filter(l -> l.getBody().getName().equalsIgnoreCase(articleRef)).findFirst();

      if(article.isEmpty()) {
        throw new ConstraintException(
            ImmutableEntity.<Link>builder().id(gid).type(EntityType.LINK).body(link.articles(init.getArticles()).build()).build(), 
            "Article with id: '" + articleRef + "' does not exist in: '" + String.join(",", state.getArticles().keySet()) + "'!");          
      }
      articles.add(article.get().getId());
    }
    link.articles(articles);
    
    for(final var label : init.getLabels()) {
      
      final var localeRef = label.getLocale();
      final var locale = resolveLocale(localeRef, state);
      
      link.addLabels(ImmutableLocaleLabel.builder()
          .locale(locale.map(e -> e.getId()).orElse(localeRef))
          .labelValue(label.getLabelValue())
          .build());
      
      if(locale.isEmpty()) {
        throw new ConstraintException(
            ImmutableEntity.<Link>builder().id(gid).type(EntityType.LINK).body(link.build()).build(), 
            "Locale with id: '" + label.getLocale() + "' does not exist in: '" + String.join(",", state.getLocales().keySet()) + "'!");          
      }
    }
    return assertUniqueId(ImmutableEntity.<Link>builder().id(gid).type(EntityType.LINK).body(link.build()).build(), state);
  }
  
  public static Entity<Workflow> workflow(CreateWorkflow init, SiteState state, StencilStore client) {
    final var gid = OidUtils.gen();
    final var workflow = ImmutableWorkflow.builder().devMode(init.getDevMode())
        .value(init.getValue());

    final var articles = new ArrayList<String>();
    for(final var articleRef : init.getArticles()) {
      final var article = state.getArticles().containsKey(articleRef) ? 
          Optional.of(state.getArticles().get(articleRef)) : 
          state.getArticles().values().stream().filter(l -> l.getBody().getName().equalsIgnoreCase(articleRef)).findFirst();

      if(article.isEmpty()) {
        throw new ConstraintException(
            ImmutableEntity.<Workflow>builder().id(gid).type(EntityType.WORKFLOW).body(workflow.articles(init.getArticles()).build()).build(), 
            "Article with id: '" + articleRef + "' does not exist in: '" + String.join(",", state.getArticles().keySet()) + "'!");          
      }
      articles.add(article.get().getId());
    }
    workflow.articles(articles);
    
    for(final var label : init.getLabels()) {        

      final var localeRef = label.getLocale();
      final var locale = resolveLocale(localeRef, state);
          
      workflow.addLabels(ImmutableLocaleLabel.builder()
          .locale(locale.map(e -> e.getId()).orElse(localeRef))
          .labelValue(label.getLabelValue())
          .build());

      if(locale.isEmpty()) {
        throw new ConstraintException(
            ImmutableEntity.<Workflow>builder().id(gid).type(EntityType.WORKFLOW).body(workflow.build()).build(), 
            "Locale with id: '" + label.getLocale() + "' does not exist in: '" + String.join(",", state.getLocales().keySet()) + "'!");          
      }
    }
    return assertUniqueId(ImmutableEntity.<Workflow>builder().id(gid).type(EntityType.WORKFLOW).body(workflow.build()).build(), state);
  }
  
  public static Optional<Entity<Locale>> resolveLocale(String idOrValue, SiteState state) {
    final var localeRef = idOrValue;
    final var locale = state.getLocales().containsKey(localeRef) ? 
        Optional.of(state.getLocales().get(localeRef)) : 
        state.getLocales().values().stream().filter(l -> l.getBody().getValue().equalsIgnoreCase(localeRef)).findFirst();
     return locale;
  }
  
  @Override
  public Uni<SiteState> repo() {
    return client.tenantQuery().create().onItem().transformToUni(e -> e.stencilQuery().head());
  }
  
  private static <T extends EntityBody> Entity<T> assertUniqueId(Entity<T> entity, SiteState state) {
    if( state.getLocales().containsKey(entity.getId()) ||
        state.getPages().containsKey(entity.getId()) ||
        state.getLinks().containsKey(entity.getId()) ||
        state.getArticles().containsKey(entity.getId()) ||
        state.getWorkflows().containsKey(entity.getId()) ||
        state.getTemplates().containsKey(entity.getId())) {
      
      throw new ConstraintException(entity, "Entity with id: '" + entity.getId() + "' already exist!");  
    }
    
    return entity;
  }
}
