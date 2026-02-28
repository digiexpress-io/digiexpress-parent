package io.resys.limaone.model;

import java.time.OffsetDateTime;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableArticleWorkflow.class)
@JsonDeserialize(as = ImmutableArticleWorkflow.class)
public interface ArticleWorkflow extends Body {
  
  String getValue();
  List<String> getArticles();
  List<LocaleLabel> getLabels();
  
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Nullable Boolean getDevMode();
  
  @JsonInclude(JsonInclude.Include.NON_NULL) 
  @Nullable Boolean getAssignable();
  
  @JsonInclude(JsonInclude.Include.NON_NULL) 
  @Nullable Boolean getAnon();
  
  @JsonInclude(JsonInclude.Include.NON_NULL) 
  @Nullable Boolean getDisabled();
  
  @JsonInclude(JsonInclude.Include.NON_NULL) 
  @Nullable OffsetDateTime getStartDate();
  
  @JsonInclude(JsonInclude.Include.NON_NULL) 
  @Nullable OffsetDateTime getEndDate();
  
  @JsonInclude(JsonInclude.Include.NON_NULL) 
  @Nullable String getFormId();
  
  @JsonInclude(JsonInclude.Include.NON_NULL) 
  @Nullable String getFormName();
  
  @JsonInclude(JsonInclude.Include.NON_NULL) 
  @Nullable String getFormTag();
  
  @JsonInclude(JsonInclude.Include.NON_NULL) 
  @Nullable String getFlowName();
}