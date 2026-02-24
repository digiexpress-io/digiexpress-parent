package io.resys.limaone.model;

import java.io.Serializable;

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
  BodyType getType();
    

  interface Body extends Serializable {
  }
    
  enum BodyType {
    LOCALE,
    
    ARTICLE_LINK,
    ARTICLE_ARTICLE,
    ARTICLE_WORKFLOW,
    ARTICLE_PAGE,
    ARTICLE_TEMPLATE,
    
    FLOW, 
    FLOW_TASK, 
    
    DECISION_TABLE
  } 
}