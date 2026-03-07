package io.resys.limaone.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;

@Value.Immutable
@JsonSerialize(as = ImmutableArticleTemplate.class)
@JsonDeserialize(as = ImmutableArticleTemplate.class)
public interface ArticleTemplate extends Body {
  
  String getName();
  String getDescription();
  String getContent();
  String getType();
  
  default BodyType getBodyType() {
    return BodyType.ARTICLE_TEMPLATE;
  }
}