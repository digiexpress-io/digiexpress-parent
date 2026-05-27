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

import java.time.OffsetDateTime;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
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

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Nullable Boolean getAuthOnly();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Nullable List<String> getTagLabels();


  default BodyType getBodyType() {
    return BodyType.ARTICLE_WORKFLOW;
  }
}
