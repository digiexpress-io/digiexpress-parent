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

import io.resys.limaone.fs.ImmutableArticleTemplateProps;
import io.resys.limaone.fs.WorldFsProps;
import io.resys.limaone.fs.WorldFsProps.ArticleTemplateProps;
import io.resys.limaone.model.ArticleTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_ArticleTemplateBuilder {
  
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public ArticleTemplateProps build() {
    final ArticleTemplate template = currentState.getBodyOfType(node);


    return ImmutableArticleTemplateProps.builder()
        .id(node.getObjectId())
        .type(template.getBodyType())
        .locked(false)
        .content(template.getContent())
        .assetDescription(node.getDescription().map(e -> e.getText()).orElse(null))
        .labels(node.getLabels().map(e -> e.getValues()).orElse(Collections.emptyList()))
        .build();
  }
  
  public static WorldFsProps of(WorldFsState curreState, NodeAndBody node) {
    return new Props_ArticleTemplateBuilder(curreState, node).build();
  }

}
