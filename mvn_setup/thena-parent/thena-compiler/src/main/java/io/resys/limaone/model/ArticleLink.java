package io.resys.limaone.model;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableArticleLink.class)
@JsonDeserialize(as = ImmutableArticleLink.class)
public interface ArticleLink extends Body {
  
  String getValue();
  String getContentType();
  List<String> getArticles();
  List<LocaleLabel> getLabels();
  
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Nullable Boolean getDevMode();
}