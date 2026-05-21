package io.resys.limaone.persistence.fs;

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

import java.util.Collections;
import java.util.List;

import io.resys.limaone.fs.ImmutableArticleProps;
import io.resys.limaone.fs.ImmutableLabel;
import io.resys.limaone.fs.WorldFsProps.ArticleProps;
import io.resys.limaone.fs.WorldFsProps.ConfigOption;
import io.resys.limaone.fs.WorldFsProps.Label;
import io.resys.limaone.model.Article;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class Props_ArticleBuilder {
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  
  public ArticleProps build() {
    final Article article = currentState.getBodyOfType(node);
    final var builder = ImmutableArticleProps.builder();

    if(Boolean.TRUE.equals(article.getDevMode())) {
      builder.addConfigOptions(ConfigOption.DEV_MODE);
    }
    if(Boolean.TRUE.equals(article.getAuthOnly())) {
      builder.addConfigOptions(ConfigOption.AUTH_ONLY_MODE);
    }

    final List<Label> labels = article.getLabels() == null ? Collections.emptyList() :
        article.getLabels().stream()
            .map(v -> (Label) ImmutableLabel.builder().id(v).value(v).build())
            .toList();

    return builder
        .id(node.getObjectId())
        .type(node.getBodyType())
        .locked(false)
        .orderNumber(article.getOrder())
        .description(article.getDescription())
        .labels(labels)
        .build();
  }
  
  public static ArticleProps of(WorldFsState currentState, NodeAndBody node) {
    return new Props_ArticleBuilder(currentState, node).build();
  }
}
