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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableArticle;
import io.thestencil.client.api.ImmutableEntity;
import io.thestencil.client.api.ImmutableLink;
import io.thestencil.client.api.ImmutableLocale;
import io.thestencil.client.api.ImmutablePage;
import io.thestencil.client.api.ImmutableTemplate;
import io.thestencil.client.api.ImmutableWorkflow;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.api.StencilConfig.EntityState;
import io.thestencil.client.api.UpdateBuilder;
import io.thestencil.client.spi.exceptions.ConstraintException;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class UpdateBuilderImpl implements UpdateBuilder {

  private final StencilClient client;
  

  @Override
  public Uni<Entity<Article>> article(ArticleMutator changes) {
    // Get the article
    final Uni<SiteState> query = client.getStore().query().head();
    
    // Change the article
    return query.onItem().transformToUni(state -> changeArticle(state, changes));
  }
  
  private Uni<Entity<Article>> changeArticle(SiteState site, ArticleMutator changes) {
    Entity<Article> start = site.getArticles().get(changes.getArticleId());
    List<Entity<?>> additionalChanges = new ArrayList<>();
    
    if(changes.getArticleId().equals(changes.getParentId())) {
      throw new ConstraintException(start, "Article: '" + changes.getName() + "' parent can't be itself!");      
    }
    
    final var duplicate = site.getArticles().values().stream()
        .filter(p -> !p.getId().equals(changes.getArticleId()))
        .filter(p -> p.getBody().getName().equals(changes.getName()))
        .findFirst();

    if(duplicate.isPresent()) {
      throw new ConstraintException(start, "Article: '" + changes.getName() + "' already exists!");
    }
    
    // update article links
    if(changes.getLinks() != null) {
      for(Entity<Link> link : site.getLinks().values()) {
        
        final var isArticleInLink = link.getBody().getArticles().contains(changes.getArticleId());
        final var isLinkInChanges = changes.getLinks().contains(link.getId());
        
        // link already defined for article
        if(isArticleInLink &&  isLinkInChanges) {
          continue;
        }
        
        // add link
        if(isLinkInChanges && !isArticleInLink) {
          
          
          final var newLink = ImmutableEntity.<Link>builder().from(link)
              .body(ImmutableLink.builder().from(link.getBody())
                  .addArticles(changes.getArticleId())
                  .build())
              .build(); 
          additionalChanges.add(newLink);
        }
        
        // remove link
        if(isArticleInLink && !isLinkInChanges) {
          final var articles = new ArrayList<>(link.getBody().getArticles());
          articles.remove(changes.getArticleId());
          
          final var newLink = ImmutableEntity.<Link>builder().from(link)
              .body(ImmutableLink.builder().from(link.getBody())
                  .articles(articles)
                  .build())
              .build();
          additionalChanges.add(newLink);
        }
      }
    }
    
    // update article workflows
    if(changes.getWorkflows() != null) {
      for(Entity<Workflow> workflow : site.getWorkflows().values()) {
        
        final var isArticleInWorkflow = workflow.getBody().getArticles().contains(changes.getArticleId());
        final var isWorkflowInChanges = changes.getWorkflows().contains(workflow.getId());
        
        // workflow already defined for article
        if(isArticleInWorkflow &&  isWorkflowInChanges) {
          continue;
        }
        
        // add workflow
        if(isWorkflowInChanges && !isArticleInWorkflow) {
          
          
          final var newWorkflow = ImmutableEntity.<Workflow>builder().from(workflow)
              .body(ImmutableWorkflow.builder().from(workflow.getBody())
                  .addArticles(changes.getArticleId())
                  .build())
              .build(); 
          additionalChanges.add(newWorkflow);
        }
        
        // remove link
        if(isArticleInWorkflow && !isWorkflowInChanges) {
          final var articles = new ArrayList<>(workflow.getBody().getArticles());
          articles.remove(changes.getArticleId());
          
          final var newWorkflow = ImmutableEntity.<Workflow>builder().from(workflow)
              .body(ImmutableWorkflow.builder().from(workflow.getBody())
                  .articles(articles)
                  .build())
              .build();
          additionalChanges.add(newWorkflow);
        }
      }
    }


    
    final var result = ImmutableEntity.<Article>builder()
        .from(start)
        .body(ImmutableArticle.builder()
            .from(start.getBody())
            .devMode(changes.getDevMode())
            .name(changes.getName())
            .order(changes.getOrder())
            .parentId(changes.getParentId())
            .build())
        .build();
    
    final var allChanges = new ArrayList<Entity<?>>();
    allChanges.add(result);
    allChanges.addAll(additionalChanges);
    
    return client.getStore().saveAll(allChanges).onItem().transform(e -> result); 
  }
  
  @Override
  public Uni<Entity<Locale>> locale(LocaleMutator changes) {
    final Uni<SiteState> query = client.getStore().query().head();
  
    // Change the locale
    return query.onItem().transformToUni(state -> client.getStore().save(changeLocale(state, changes)));
  }
  
  private Entity<Locale> changeLocale(SiteState site, LocaleMutator changes) {
    final Entity<Locale> start = site.getLocales().get(changes.getLocaleId());

    final var duplicate = site.getLocales().values().stream()
        .filter(p -> !p.getId().equals(changes.getLocaleId()))
        .filter(p -> p.getBody().getValue().equals(changes.getValue()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      throw new ConstraintException(start, "Locale: '" + changes.getValue() + "' already exists!");
    }
    
    
    return ImmutableEntity.<Locale>builder()
        .from(start)
        .body(ImmutableLocale.builder().from(start.getBody())
            .value(changes.getValue())
            .enabled(changes.getEnabled())
            .build())
        .build();
  }

  @Override
  public Uni<Entity<Template>> template(TemplateMutator changes) {
    final Uni<SiteState> query = client.getStore().query().head();
  
    // Change the template
    return query.onItem().transformToUni(state -> client.getStore().save(changeTemplate(state, changes)));
  }
  
  private Entity<Template> changeTemplate(SiteState site, TemplateMutator changes) {
    final Entity<Template> start = site.getTemplates().get(changes.getTemplateId());

    final var duplicate = site.getTemplates().values().stream()
        .filter(p -> !p.getId().equals(changes.getTemplateId()))
        .filter(p -> p.getBody().getName().equals(changes.getName()))
        .findFirst();
    
    if(duplicate.isPresent()) {
      throw new ConstraintException(start, "Template: '" + changes.getName() + "' already exists!");
    }
    
    
    return ImmutableEntity.<Template>builder()
        .from(start)
        .body(ImmutableTemplate.builder().from(start.getBody())
            .name(changes.getName())
            .content(changes.getContent())
            .type(changes.getType())
            .description(changes.getDescription())
            .build())
        .build();
  }
  
  @Override
  public Uni<Entity<Page>> page(PageMutator changes) {
    // Get the page
    final Uni<EntityState<Page>> query = client.getStore().get(changes.getPageId(), EntityType.PAGE);
    
    // Change the page
    return query.onItem().transformToUni(state -> client.getStore().save(changePage(state, changes)));
  }

  @Override
  public Uni<List<Entity<Page>>> pages(List<PageMutator> mutators) {
    // Get the page
    
    final List<String> ids = new ArrayList<>();
    final Map<String, PageMutator> changes = new HashMap<>();
    for(var m : mutators) {
      changes.put(m.getPageId(), m);
      ids.add(m.getPageId());
    }
    
    final Uni<List<Entity<Page>>> query = client.getStore().query().head(ids, EntityType.PAGE);

    return query.onItem().transformToUni(state -> {
          
          final List<Entity<Page>> toBeSaved = state.stream()
          .map(start -> {
            final PageMutator mutator = changes.get(start.getId());
            final Entity<Page> end = ImmutableEntity.<Page>builder()
                .from(start)
                .body(ImmutablePage.builder().from(start.getBody())
                    .content(mutator.getContent())
                    .locale(mutator.getLocale())
                    .devMode(mutator.getDevMode())
                    .build())
                .build();
            return end;
          }).collect(Collectors.toList());

          return client.getStore()
              .saveAll(new ArrayList<>(toBeSaved))
              .onItem().transform(e -> new ArrayList<>(toBeSaved));
        });
  }
  
  private Entity<Page> changePage(EntityState<Page> state, PageMutator changes) {
    final var start = state.getEntity();
    return ImmutableEntity.<Page>builder()
        .from(start)
        .body(ImmutablePage.builder().from(start.getBody())
            .content(changes.getContent())
            .devMode(changes.getDevMode())
            .locale(changes.getLocale())
            .build())
        .build();
  }

  @Override
  public Uni<Entity<Link>> link(LinkMutator changes) {
    // Get the link
    
    final Uni<SiteState> query = client.getStore().query().head();
    
    // Change the link
    return query.onItem().transformToUni(state -> client.getStore().save(changeLink(state, changes)));
  }
  
  private Entity<Link> changeLink(SiteState site, LinkMutator changes) {
    final var start = site.getLinks().get(changes.getLinkId());
    
    if(changes.getLabels() != null ) {
      for(final var label : changes.getLabels()) {
        final var localeId = label.getLocale();
        if(!site.getLocales().containsKey(localeId)) {
          throw new ConstraintException(start, "Locale with id: '" + localeId + "' does not exist in: '" + String.join(",", site.getLocales().keySet()) + "'!");          
        }
      }
    }
    return ImmutableEntity.<Link>builder()
        .from(start)
        .body(ImmutableLink.builder().from(start.getBody())
            .value(changes.getValue())
            .devMode(changes.getDevMode())
            .labels(changes.getLabels() == null ? start.getBody().getLabels() : changes.getLabels())
            .contentType(changes.getType())
            .articles(changes.getArticles() == null ? start.getBody().getArticles() : changes.getArticles())
            .build())
        .build();
  }

  @Override
  public Uni<Entity<Workflow>> workflow(WorkflowMutator changes) {
    // Get the Workflow
    final Uni<SiteState> query = client.getStore().query().head();
    
    // Change the Workflow
    return query.onItem().transformToUni(state -> client.getStore().save(changeWorkflow(state, changes)));
  }
  
  private Entity<Workflow> changeWorkflow(SiteState site, WorkflowMutator changes) {
    final var start = site.getWorkflows().get(changes.getWorkflowId());
    if(changes.getLabels() != null ) {
      for(final var label : changes.getLabels()) {
        final var localeId = label.getLocale();
        if(!site.getLocales().containsKey(localeId)) {
          throw new ConstraintException(start, "Locale with id: '" + localeId + "' does not exist in: '" + String.join(",", site.getLocales().keySet()) + "'!");          
        }
      }
    }
    return ImmutableEntity.<Workflow>builder()
        .from(start)
        .body(ImmutableWorkflow.builder().from(start.getBody())
            .devMode(changes.getDevMode())
            .value(changes.getValue())
            .labels(changes.getLabels() == null ? start.getBody().getLabels() : changes.getLabels())
            .articles(changes.getArticles() == null ? start.getBody().getArticles() : changes.getArticles())
            .build())
        .build();
  }
}
