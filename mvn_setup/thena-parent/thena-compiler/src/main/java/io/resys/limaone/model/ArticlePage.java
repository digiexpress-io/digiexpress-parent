package io.resys.limaone.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableArticlePage.class)
@JsonDeserialize(as = ImmutableArticlePage.class)
public interface ArticlePage extends Body {
  
  String getArticle();
  String getLocale();
  String getContent();
  
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Nullable Boolean getDevMode();
}