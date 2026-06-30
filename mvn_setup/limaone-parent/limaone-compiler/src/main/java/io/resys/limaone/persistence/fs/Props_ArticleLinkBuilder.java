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

import java.util.List;
import java.util.stream.Collectors;

import io.resys.limaone.fs.ImmutableLinkProps;
import io.resys.limaone.fs.WorldFsProps;
import io.resys.limaone.fs.WorldFsProps.LinkProps;
import io.resys.limaone.model.ArticleLink;
import io.resys.limaone.model.LocaleLabel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_ArticleLinkBuilder {
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public LinkProps build() {
    final ArticleLink link = currentState.getBodyOfType(node);
    final List<LocaleLabel> localeLabels = link.getLabels();

    final var builder = ImmutableLinkProps.builder();

    if (Boolean.TRUE.equals(link.getDevMode())) {
      builder.addConfigOptions(WorldFsProps.ConfigOption.DEV_MODE);
    }


    return builder
        .id(node.getObjectId())
        .type(node.getBodyType())
        .contentType(link.getContentType())
        .locked(false)
        .articles(link.getArticles())
        .intlValues(localeLabels.stream()
            .collect(Collectors.toMap(
                l -> l.getLocale(),
                l -> l.getLabelValue()
              )))
        .assetDescription(node.getDescription().map(e -> e.getText()).orElse(null))
        .labels(node.getLabels().map(e -> e.getValues()).orElse(Collections.emptyList()))
        .urlValue(link.getValue())
        .build();
  }
  
  public static LinkProps of(WorldFsState currentState, NodeAndBody node) {
    return new Props_ArticleLinkBuilder(currentState, node).build();
  }
  
}
