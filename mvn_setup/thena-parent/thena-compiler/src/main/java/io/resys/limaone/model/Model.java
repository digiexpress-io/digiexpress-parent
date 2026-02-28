package io.resys.limaone.model;

import java.io.Serializable;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;


@Value.Immutable
@JsonSerialize(as = ImmutableModel.class)
@JsonDeserialize(as = ImmutableModel.class)
public interface Model<T extends Body>  extends Serializable {
  String getId();
  T getBody();
  BodyType getBodyType();
  String getBodyHash();

  interface Body extends Serializable {
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
    PRINTOUT_RESOURCE
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableModelWorld.class)
  @JsonDeserialize(as = ImmutableModelWorld.class)
  interface ModelWorld {
    String getName();
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
  }
}