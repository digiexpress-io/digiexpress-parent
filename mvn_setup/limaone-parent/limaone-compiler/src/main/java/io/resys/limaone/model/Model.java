package io.resys.limaone.model;

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

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import jakarta.annotation.Nullable;


@Value.Immutable
@JsonSerialize(as = ImmutableModel.class)
@JsonDeserialize(as = ImmutableModel.class)
public interface Model<T extends Body>  extends Serializable {
  String getId();
  T getBody();
  BodyType getBodyType();
  String getBodyHash();

  @SuppressWarnings("unchecked")
  public static <T extends Body> Model<T> of(Model<?> any, Body body) {
    return ImmutableModel.<T>builder().from((Model<T>) any).body((T) body).build();
  }
  
  interface Body extends Serializable {  
    @JsonIgnore
    BodyType getBodyType();
  }
    
  enum BodyType {
    LOCALE,
    
    ARTICLE_LINK,
    ARTICLE,
    ARTICLE_WORKFLOW,
    ARTICLE_PAGE,
    ARTICLE_TEMPLATE,
    
    FLOW, 
    FLOW_TASK, 
    DECISION_TABLE,
    
    DIALOB_FORM,
    
    PRINTOUT,
    PRINTOUT_PAGE,
    PRINTOUT_SCRIPT,
    PRINTOUT_RESOURCE,
    
    DEPLOYMENT,
    UNKNOWN;
    
    public static BodyType[] without(BodyType ...without) {
      final var filter = Arrays.asList(without);
      return Arrays.asList(BodyType.values()).stream()
        .filter(t -> !filter.contains(t))
        .toArray(BodyType[]::new);
    }
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableModelWorld.class)
  @JsonDeserialize(as = ImmutableModelWorld.class)
  interface ModelWorld {
    String getName();
    @Nullable UUID getRefId();
    @Nullable UUID getCommitId();
    
    Map<String, Model<Article>> getArticles();
    Map<String, Model<ArticleLink>> getArticleLinks();
    Map<String, Model<ArticlePage>> getArticlePages();
    Map<String, Model<ArticleTemplate>> getArticleTemplates();
    Map<String, Model<ArticleWorkflow>> getArticleWorkflows();
    Map<String, Model<DecisionTable>> getDecisionTables();
    Map<String, Model<Deployment>> getDeployments();
    Map<String, Model<Flow>> getFlows();
    Map<String, Model<FlowTask>> getFlowTasks();
    Map<String, Model<Locale>> getLocales();
    Map<String, Model<DialobForm>> getForms();
    
    
    
    default Optional<Model<?>> findAnyObject(String id) {
      if (getArticles().containsKey(id)) {
        return Optional.ofNullable(getArticles().get(id));
      }
      if (getArticleLinks().containsKey(id)) {
        return Optional.ofNullable(getArticleLinks().get(id));
      }
      if (getArticlePages().containsKey(id)) {
        return Optional.ofNullable(getArticlePages().get(id));
      }
      if (getArticleTemplates().containsKey(id)) {
        return Optional.ofNullable(getArticleTemplates().get(id));
      }
      if (getArticleWorkflows().containsKey(id)) {
        return Optional.ofNullable(getArticleWorkflows().get(id));
      }
      if (getDecisionTables().containsKey(id)) {
        return Optional.ofNullable(getDecisionTables().get(id));
      }
      if (getDeployments().containsKey(id)) {
        return Optional.ofNullable(getDeployments().get(id));
      }
      if (getFlows().containsKey(id)) {
        return Optional.ofNullable(getFlows().get(id));
      }
      if (getFlowTasks().containsKey(id)) {
        return Optional.ofNullable(getFlowTasks().get(id));
      }
      if (getLocales().containsKey(id)) {
        return Optional.ofNullable(getLocales().get(id));
      }
      if (getForms().containsKey(id)) {
        return Optional.ofNullable(getForms().get(id));
      }
      return Optional.empty();
    }

    default Optional<Model<Locale>> findOneLocale(String idOrValue) {
      final var allLocales = this.getLocales();
      final var localeRef = idOrValue;
      final var locale = allLocales.containsKey(localeRef) ? 
          Optional.of(allLocales.get(localeRef)) : 
          allLocales.values().stream().filter(l -> l.getBody().getValue().equalsIgnoreCase(localeRef)).findFirst();
       return locale;
    }
    
    default Optional<Model<Article>> findOneArticle(String articleRef) {
      final var allArticles = this.getArticles();
      return allArticles.containsKey(articleRef) ? 
          Optional.of(allArticles.get(articleRef)) : 
          allArticles.values().stream().filter(l -> l.getBody().getName().equalsIgnoreCase(articleRef)).findFirst();
    }
    
    
    default ModelWorld withAny(String id, Model.Body body) {
      final var builder = ImmutableModelWorld.builder().from(this);
      final var current = findAnyObject(id).orElseThrow();

      switch (body.getBodyType()) {
        case ARTICLE -> {
          final var map = new HashMap<>(this.getArticles());
          map.put(id, Model.of(current, body));
          builder.articles(map);
        }
        case ARTICLE_LINK -> {
          final var map = new HashMap<>(this.getArticleLinks());
          map.put(id, Model.of(current, body));
          builder.articleLinks(map);
        }
        case ARTICLE_PAGE -> {
          final var map = new HashMap<>(this.getArticlePages());
          map.put(id, Model.of(current, body));
          builder.articlePages(map);
        }
        case ARTICLE_TEMPLATE -> {
          final var map = new HashMap<>(this.getArticleTemplates());
          map.put(id, Model.of(current, body));
          builder.articleTemplates(map);
        }
        case ARTICLE_WORKFLOW -> {
          final var map = new HashMap<>(this.getArticleWorkflows());
          map.put(id, Model.of(current, body));
          builder.articleWorkflows(map);
        }
        case DECISION_TABLE -> {
          final var map = new HashMap<>(this.getDecisionTables());
          map.put(id, Model.of(current, body));
          builder.decisionTables(map);
        }
        case DEPLOYMENT -> {
          final var map = new HashMap<>(this.getDeployments());
          map.put(id, Model.of(current, body));
          builder.deployments(map);
        }
        case FLOW -> {
          final var map = new HashMap<>(this.getFlows());
          map.put(id, Model.of(current, body));
          builder.flows(map);
        }
        case FLOW_TASK -> {
          final var map = new HashMap<>(this.getFlowTasks());
          map.put(id, Model.of(current, body));
          builder.flowTasks(map);
        }
        case LOCALE -> {
          final var map = new HashMap<>(this.getLocales());
          map.put(id, Model.of(current, body));
          builder.locales(map);
        }
        case DIALOB_FORM -> {
          final var map = new HashMap<>(this.getForms());
          map.put(id, Model.of(current, body));
          builder.forms(map);
        }
        case PRINTOUT, PRINTOUT_PAGE, PRINTOUT_SCRIPT, PRINTOUT_RESOURCE, UNKNOWN -> 
        throw new UnsupportedOperationException("PRINTOUT types not mapped to ModelWorld: " + body.getBodyType());
      } 
      return builder.build();
    }
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableModelWorldIndex.class)
  @JsonDeserialize(as = ImmutableModelWorldIndex.class)
  interface ModelWorldIndex {
    String getObjectId();
    
    OffsetDateTime getCreatedAt();
    String getCreatedBy();
    
    OffsetDateTime getUpdatedAt();
    String getUpdatedBy();
  }
  
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableModelWorldDiff.class)
  @JsonDeserialize(as = ImmutableModelWorldDiff.class)
  interface ModelWorldDiff {
    String getBaseName();
    String getTargetName();
    String getBaseId();
    String getTargetId();
    OffsetDateTime getCreated();
    String getBody();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableModelWorldSummary.class)
  @JsonDeserialize(as = ImmutableModelWorldSummary.class)
  interface ModelWorldSummary extends Serializable {
    String getTagName();
    List<ModelWorldSummaryItem> getFlows();
    List<ModelWorldSummaryItem> getDecisions();
    List<ModelWorldSummaryItem> getServices();
    // todo for other types
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableModelWorldSummaryItem.class)
  @JsonDeserialize(as = ImmutableModelWorldSummaryItem.class)
  interface ModelWorldSummaryItem {
    String getId();
    String getName();
    String getBody();
  }
}
