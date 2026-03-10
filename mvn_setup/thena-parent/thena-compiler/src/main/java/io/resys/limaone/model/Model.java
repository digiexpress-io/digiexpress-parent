package io.resys.limaone.model;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

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
    
    DIALOB,
    
    PRINTOUT,
    PRINTOUT_PAGE,
    PRINTOUT_SCRIPT,
    PRINTOUT_RESOURCE,
    
    DEPLOYMENT
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableModelWorld.class)
  @JsonDeserialize(as = ImmutableModelWorld.class)
  interface ModelWorld {
    String getName();
    @Nullable String getRefId();
    @Nullable String getCommitId();
    
    Map<String, Model<Article>> getArticles();
    Map<String, Model<ArticleLink>> getArticleLinks();
    Map<String, Model<ArticlePage>> getArticlePages();
    Map<String, Model<ArticleTemplate>> getArticleTemplates();
    Map<String, Model<ArticleWorkflow>> getArticleWorkflows();
    Map<String, Model<DecisionTable>> getDecisionTables();
    Map<String, Model<Flow>> getFlows();
    Map<String, Model<FlowTask>> getFlowTasks();
    Map<String, Model<Locale>> getLocales();
    Map<String, Model<Dialob>> getForms();
    

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
  }
}