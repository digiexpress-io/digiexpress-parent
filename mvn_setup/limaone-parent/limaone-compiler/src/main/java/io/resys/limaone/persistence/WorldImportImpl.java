package io.resys.limaone.persistence;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.dialob.api.form.Form;
import io.dialob.api.form.Form.Metadata;
import io.dialob.api.form.FormTag;
import io.resys.limaone.authoring.Authoring.WorldImport;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.ArticleTemplate;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.DialobForm;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableArticleLink;
import io.resys.limaone.model.ImmutableArticlePage;
import io.resys.limaone.model.ImmutableArticleWorkflow;
import io.resys.limaone.model.ImmutableDialobForm;
import io.resys.limaone.model.ImmutableLocaleLabel;
import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Printout;
import io.resys.limaone.model.PrintoutPage;
import io.resys.limaone.model.PrintoutResource;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class WorldImportImpl implements WorldImport {
  private final AuthoringConfig config;
  private ModelWorld source;
  
  
  @Override
  public WorldImport source(ModelWorld source) {
    this.source = Objects.requireNonNull(source, () -> "source must be defined!");
    return this;
  }

  @Override
  public ModelWorld buildSync() {
    return build()
        .runSubscriptionOn(config.getEnvir().getWorkerPool())
        .await().atMost(config.getEnvir().getWorkerPoolMaxTimeout());
  }
  
  @Override
  public Uni<ModelWorld> build() {
    Objects.requireNonNull(source, () -> "source must be defined!");
    
    return config.getPersistence().worldBuilder()
      .createdAt(OffsetDateTime.now())
      .author(config.getEnvir().getCurrentUser().get().getUserName())
      .docs(BodyType.without(BodyType.DEPLOYMENT, BodyType.DIALOB_FORM))
      .build(nextWorld -> {
        final var ctx = new ImportContext();
        
        source.getLocales().values().forEach(e -> merge(e, nextWorld, ctx));
        source.getArticles().values().forEach(e -> merge(e, nextWorld, ctx));
        
        source.getArticleLinks().values().forEach(e -> merge(e, nextWorld, ctx));
        source.getArticlePages().values().forEach(e -> merge(e, nextWorld, ctx));
        source.getArticleTemplates().values().forEach(e -> merge(e, nextWorld, ctx));
        source.getArticleWorkflows().values().forEach(e -> merge(e, nextWorld, ctx));
        source.getFlows().values().forEach(e -> merge(e, nextWorld, ctx));
        source.getFlowTasks().values().forEach(e -> merge(e, nextWorld, ctx));
        source.getDecisionTables().values().forEach(e -> merge(e, nextWorld, ctx));
        source.getPrintouts().values().forEach(e -> merge(e, nextWorld, ctx));
        source.getPrintoutPages().values().forEach(e -> merge(e, nextWorld, ctx));
        source.getPrintoutResources().values().forEach(e -> merge(e, nextWorld, ctx));

        return nextWorld.getCurrentWorld();
      })
      .onItem().transformToUni(oldWorld -> {
        
        if(oldWorld.getForms().isEmpty()) {
          return Uni.createFrom().voidItem();
        }
        return mergeDialob();
      })
      .onItem().transformToUni(ignore -> config.getPersistence().worldQuery().findAll());
  }
  
  
  /** 
   * Dialob form import
   */
  private Uni<Void> mergeDialob() {
    final var formDb = config.getEnvir().getFormDb().withTenant();
    
    return Uni.combine().all().unis(
        formDb.formQuery().findAll().collect().asList(), 
        formDb.formTagQuery().findAll().collect().asList()
      ).asTuple() 
      
      .onItem().transform(tuple -> findAllMissingForms(tuple.getItem2(), tuple.getItem1()))
      .onItem().transformToUni(missing -> createFormsAndTags(missing));
    
  }
  
  
  /**
   * Article import
   */
  private void mergeArticle(Model<Article> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();

    final var prev = existing.getArticles().values()
        .stream()
        .filter(d -> 
            d.getId().equals(target.getId()) || 
            d.getBody().getName().equalsIgnoreCase(target.getBody().getName()))
        .findFirst();
  
    final Model<Article> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(prev.get().getId(), target.getBody().getName(), target.getBody(), null);
    } else {
      next = nextWorld.newModel(target.getBody().getName(), target.getBody(), null);
    }
    ctx.addNewId(target.getId(), next.getId());
  }
  
  /**
   * Article link import
   */
  private void mergeArticleLink(Model<ArticleLink> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();

    final var prev = existing.getArticleLinks().values()
        .stream()
        .filter(d -> 
            d.getId().equals(target.getId()) || 
            d.getBody().getValue().equalsIgnoreCase(target.getBody().getValue()))
        .findFirst();
    
    final var targetBody = ImmutableArticleLink.builder()
        .from(target.getBody())
        .articles(target.getBody().getArticles().stream().map(old -> ctx.getNewId(old)).toList())
        .labels(
            target.getBody().getLabels().stream()
              .map(label -> ImmutableLocaleLabel.builder().from(label).locale(ctx.getNewId(label.getLocale())).build())
              .toList()
        )
        .build();
  
    final Model<ArticleLink> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(prev.get().getId(), targetBody.getValue(), targetBody, null);
    } else {
      next = nextWorld.newModel(target.getBody().getValue(), target.getBody(), null);
    }
  }
  
  /**
   * Article page import 
   */
  private void mergeArticlePage(Model<ArticlePage> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();
    final var articleId = ctx.getNewId(target.getBody().getArticle());
    final var localeId = ctx.getNewId(target.getBody().getLocale());
    
    final var prev = existing.getArticlePages().values()
        .stream()
        .filter(d -> d.getId().equals(target.getId()) || ( 
            d.getBody().getLocale().equalsIgnoreCase(localeId) &&
            d.getBody().getArticle().equalsIgnoreCase(articleId)
          )
        )
        .findFirst();

    final var targetBody = ImmutableArticlePage.builder()
        .from(target.getBody())
        .article(ctx.getNewId(target.getBody().getArticle()))
        .build();
  
    final Model<ArticlePage> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(prev.get().getId(), prev.get().getId(), targetBody, null);
    } else {
      next = nextWorld.newModel(target.getId(), targetBody, null);
    }
  }
  
  /**
   * Article template import
   */
  private void mergeArticleTemplate(Model<ArticleTemplate> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();

    final var prev = existing.getArticleTemplates().values()
        .stream()
        .filter(d -> 
            d.getId().equals(target.getId()) || 
            d.getBody().getName().equalsIgnoreCase(target.getBody().getName()))
        .findFirst();
  
    final Model<ArticleTemplate> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(target.getId(), target.getBody().getName(), target.getBody(), null);
    } else {
      next = nextWorld.newModel(target.getBody().getName(), target.getBody(), null);
    }
  }
  
  /**
   * Article workflow import
   */
  private void mergeArticleWorkflow(Model<ArticleWorkflow> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();

    final var prev = existing.getArticleWorkflows().values()
        .stream()
        .filter(d -> 
            d.getId().equals(target.getId()) || 
            d.getBody().getValue().equalsIgnoreCase(target.getBody().getValue()))
        .findFirst();
  
    final var targetBody = ImmutableArticleWorkflow.builder()
        .from(target.getBody())
        .articles(target.getBody().getArticles().stream().map(old -> ctx.getNewId(old)).toList())
        .labels(
            target.getBody().getLabels().stream()
              .map(label -> ImmutableLocaleLabel.builder().from(label).locale(ctx.getNewId(label.getLocale())).build())
              .toList()
        )
        .build();
    
    final Model<ArticleWorkflow> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(target.getId(), targetBody.getValue(), targetBody, null);
    } else {
      next = nextWorld.newModel(targetBody.getValue(), targetBody, null);
    }
  }
  
  /**
   * Decision table import
   */
  private void mergeDecisionTable(Model<DecisionTable> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();

    final var prev = existing.getDecisionTables().values()
        .stream()
        .filter(d -> 
            d.getId().equals(target.getId()) || 
            d.getBody().getName().equalsIgnoreCase(target.getBody().getName()))
        .findFirst();
  
    final Model<DecisionTable> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(target.getId(), target.getBody().getName(), target.getBody(), null);
    } else {
      next = nextWorld.newModel(target.getBody().getName(), target.getBody(), null);
    }
  }
  
  /**
   * Flow import
   */
  private void mergeFlow(Model<Flow> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();

    final var prev = existing.getFlows().values()
        .stream()
        .filter(d -> 
            d.getId().equals(target.getId()) || 
            d.getBody().getFlowName().equalsIgnoreCase(target.getBody().getFlowName()))
        .findFirst();
  
    final Model<Flow> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(target.getId(), target.getBody().getFlowName(), target.getBody(), null);
    } else {
      next = nextWorld.newModel(target.getBody().getFlowName(), target.getBody(), null);
    }
  }
  
  /**
   * Flow task import
   */
  private void mergeFlowTask(Model<FlowTask> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();

    final var prev = existing.getFlowTasks().values()
        .stream()
        .filter(d -> 
            d.getId().equals(target.getId()) || 
            d.getBody().getTaskName().equalsIgnoreCase(target.getBody().getTaskName()))
        .findFirst();
  
    final Model<FlowTask> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(target.getId(), target.getBody().getTaskName(), target.getBody(), null);
    } else {
      next = nextWorld.newModel(target.getBody().getTaskName(), target.getBody(), null);
    }
  }
  
  private void mergeLocale(Model<Locale> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();

    final var prev = existing.getLocales().values()
        .stream()
        .filter(d -> 
            d.getId().equals(target.getId()) || 
            d.getBody().getValue().equalsIgnoreCase(target.getBody().getValue()))
        .findFirst();
  
    final Model<Locale> next;
    if(prev.isPresent()) {
      // no point in merging its same
      next = prev.get();
    } else {
      next = nextWorld.newModel(target.getBody().getValue(), target.getBody(), null);
    }
    ctx.addNewId(target.getId(), next.getId());
  }
  
  /**
   * Printout import
   */
  private void mergePrintout(Model<Printout> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();
    
    final var prev = existing.getPrintouts().values()
        .stream()
        .filter(d -> d.getId().equals(target.getId()) || d.getBody().getServiceName().equalsIgnoreCase(target.getBody().getServiceName()))
        .findFirst();

    final Model<Printout> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(target.getId(), target.getBody().getServiceName(), target.getBody(), null);
    } else {
      next = nextWorld.newModel(target.getBody().getServiceName(), target.getBody(), null);
    }
  }

  /**
   * Printout page import
   */
  private void mergePrintoutPage(Model<PrintoutPage> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();
    
    final var prev = existing.getPrintoutPages().values()
        .stream()
        .filter(d -> d.getId().equals(target.getId()) || d.getBody().getServiceId().equalsIgnoreCase(target.getBody().getServiceId()))
        .findFirst();

    final Model<PrintoutPage> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(target.getId(), target.getBody().getServiceId(), target.getBody(), null);
    } else {
      next = nextWorld.newModel(target.getBody().getServiceId(), target.getBody(), null);
    }
  }

  /**
   * Printout resource import
   */
  private void mergePrintoutResource(Model<PrintoutResource> target, NextWorld nextWorld, ImportContext ctx) {
    final ModelWorld existing = nextWorld.getCurrentWorld();

    final var prev = existing.getPrintoutResources().values()
        .stream()
        .filter(d -> d.getId().equals(target.getId()) || d.getBody().getResourceName().equalsIgnoreCase(target.getBody().getResourceName()))
        .findFirst();

    final Model<PrintoutResource> next;
    if(prev.isPresent()) {
      next = nextWorld.mergeModel(target.getId(), target.getBody().getResourceName(), target.getBody(), null);
    } else {
      next = nextWorld.newModel(target.getBody().getResourceName(), target.getBody(), null);
    }
  }

  @SuppressWarnings("unchecked")
  private void merge(Model<?> target, NextWorld nextWorld, ImportContext ctx) {
    switch (target.getBodyType()) {
      case ARTICLE -> mergeArticle((Model<Article>) target, nextWorld, ctx);
      case ARTICLE_LINK -> mergeArticleLink((Model<ArticleLink>) target, nextWorld, ctx);
      case ARTICLE_PAGE -> mergeArticlePage((Model<ArticlePage>) target, nextWorld, ctx);
      case ARTICLE_TEMPLATE -> mergeArticleTemplate((Model<ArticleTemplate>) target, nextWorld, ctx);
      case ARTICLE_WORKFLOW -> mergeArticleWorkflow((Model<ArticleWorkflow>) target, nextWorld, ctx);
      case DECISION_TABLE -> mergeDecisionTable((Model<DecisionTable>) target, nextWorld, ctx);
      case DEPLOYMENT -> log.warn("Deployment type should not be merged: {}", target.getBodyType());
      case FLOW -> mergeFlow((Model<Flow>) target, nextWorld, ctx);
      case FLOW_TASK -> mergeFlowTask((Model<FlowTask>) target, nextWorld, ctx);
      case LOCALE -> mergeLocale((Model<Locale>) target, nextWorld, ctx);
      case DIALOB_FORM -> { /** Do nothing different merge mech */  }
      case PRINTOUT -> mergePrintout((Model<Printout>) target, nextWorld, ctx);
      case PRINTOUT_PAGE -> mergePrintoutPage((Model<PrintoutPage>) target, nextWorld, ctx);
      case PRINTOUT_RESOURCE -> mergePrintoutResource((Model<PrintoutResource>) target, nextWorld, ctx);
      case UNKNOWN -> 
        log.error("Unknown body type not supported for merge: {}", target.getBodyType());
    }
  }
  

  private Uni<Void> createFormsAndTags(List<Model<DialobForm>> forms) {
    final var formDb = config.getEnvir().getFormDb().withTenant();

    return Uni.combine().all().unis(forms.stream().map(form -> 
      formDb.createForm().props(form.getBody().getForm()).build()
      .onItem().transformToUni(createdForm -> 
        formDb.createFormTag().formName(createdForm.getName()).formVersion(form.getBody().getFormTagName()).build()
      )
      .onItem().transformToUni(ignore -> Uni.createFrom().voidItem())
      .onFailure().recoverWithUni(throwable -> {
        log.error("Failed to import dialob form, because of: {}", throwable.getMessage(), throwable);
        return Uni.createFrom().voidItem();
      })
    ).toList())
    .usingConcurrencyOf(5).discardItems();
  }
  
  private List<Model<DialobForm>> findAllMissingForms(List<FormTag> tags, List<Form> forms) {
    final var suffix = dialobSuffix();
    final List<Model<DialobForm>> missing = new ArrayList<>();
    for(final var importForm : this.source.getForms().values()) {
      final var isFormMissing = tags.stream().filter(t -> 
          t.getFormName().equals(importForm.getBody().getFormName()) &&
          t.getName().equals(importForm.getBody().getFormTagName())
        ).findFirst().isEmpty();
      
      if(isFormMissing) {
        missing.add(sanitizeForm(importForm, forms, suffix));
      }
    }
    return missing;
  } 
  
  public static String dialobSuffix() {
    final var dateTime = LocalDateTime.now();
    return String.format("%d%s%d",
        dateTime.getYear() % 10,
        dateTime.format(DateTimeFormatter.ofPattern("MMM")).toUpperCase(),
        ChronoUnit.MINUTES.between(dateTime.toLocalDate().atStartOfDay(), dateTime));
}
  
  private Model<DialobForm> sanitizeForm(Model<DialobForm> target, List<Form> forms, String suffix) {
    final var labels = forms.stream().map(ex -> ex.getMetadata().getLabel()).toList();
    final var names = forms.stream().map(ex -> ex.getName()).toList();
    
    final var form = target.getBody().getForm();
    final var isLabelConflict = labels.contains(form.getMetadata().getLabel());
    final var isNameConflict = names.contains(form.getName());
    final var isConflict = isLabelConflict || isNameConflict;
    
    if(isConflict) {
      Form next = form;
      if(isNameConflict) {
        next = new Form.Builder().from(next).rev(next.getRev()).name(next.getName() + "_" + suffix).build();
      }
      if(isLabelConflict) {
        next = new Form.Builder().from(next).rev(next.getRev())
            .metadata(new Metadata.Builder().from(next.getMetadata()).label(next.getMetadata().getLabel() + "_" + suffix).build())
            .build();
      }
      return ImmutableModel.<DialobForm>builder()
          .body(ImmutableDialobForm.builder().from(target.getBody()).form(next).build())
          .build();
    }
    return target;
  }

  private static class ImportContext {
    private final Map<String, String> objectId_replacements = new HashMap<>(); 
    
    public void addNewId(String from, String to) {
      objectId_replacements.put(from, to);
    }
    
    public String getNewId(String oldId) {
      return objectId_replacements.get(oldId);
    }
  }
}
