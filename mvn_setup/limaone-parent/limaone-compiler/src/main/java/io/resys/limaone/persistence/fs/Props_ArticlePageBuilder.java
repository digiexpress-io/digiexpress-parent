package io.resys.limaone.persistence.fs;

import java.util.Collections;

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

import io.resys.limaone.fs.ImmutableArticlePageProps;
import io.resys.limaone.fs.WorldFsProps.ArticlePageProps;
import io.resys.limaone.fs.WorldFsProps.ConfigOption;
import io.resys.limaone.model.ArticlePage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_ArticlePageBuilder {
  
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public ArticlePageProps build() {
    final ArticlePage articlePage = currentState.getBodyOfType(node);
    final var builder = ImmutableArticlePageProps.builder();

    if (Boolean.TRUE.equals(articlePage.getDevMode())) {
      builder.addConfigOptions(ConfigOption.DEV_MODE);
    }
    if (Boolean.TRUE.equals(articlePage.getDisabledMode())) {
      builder.addConfigOptions(ConfigOption.DISABLED_MODE);
    }

    return builder
        .id(node.getObjectId())
        .type(node.getBodyType())
        .locked(false)
        .content(articlePage.getContent())
        .articleId(articlePage.getArticle())
        .localeCode(articlePage.getLocale())
        .assetDescription(node.getDescription().map(e -> e.getText()).orElse(null))
        .labels(node.getLabels().map(e -> e.getValues()).orElse(Collections.emptyList()))
        .build();
  }
  
  public static ArticlePageProps of(WorldFsState curreState, NodeAndBody node) {
    return new Props_ArticlePageBuilder(curreState, node).build();
  }

}
