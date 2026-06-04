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

import java.util.List;
import java.util.stream.Collectors;

import io.resys.limaone.fs.ImmutableServiceProps;
import io.resys.limaone.fs.WorldFsProps.ConfigOption;
import io.resys.limaone.fs.WorldFsProps.ServiceProps;
import io.resys.limaone.model.ArticleWorkflow;
import io.resys.limaone.model.LocaleLabel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_ArticleWorkflowBuilder {
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public ServiceProps build() {
    final ArticleWorkflow service = currentState.getBodyOfType(node);
    final List<LocaleLabel> labels = service.getLabels();
    
//    final List<Label> tagLabels = service.getTagLabels() == null ? Collections.emptyList() :
//      service.getTagLabels().stream()
//          .map(v -> (Label) ImmutableLabel.builder().id(v).value(v).build())
//          .toList();
//    
    
    final var builder = ImmutableServiceProps.builder();
    
    if (Boolean.TRUE.equals(service.getDevMode())) {
      builder.addConfigOptions(ConfigOption.DEV_MODE);
    }
    if (Boolean.TRUE.equals(service.getAssignable())) {
      builder.addConfigOptions(ConfigOption.ASSIGNABLE_MODE);
    }
    if (Boolean.TRUE.equals(service.getDisabled())) {
      builder.addConfigOptions(ConfigOption.DISABLED_MODE);
    }
    if (Boolean.TRUE.equals(service.getAnon())) {
      builder.addConfigOptions(ConfigOption.ANONYMOUS_MODE);
    }
//    if (Boolean.TRUE.equals(service.getAuthOnly())) {
//      builder.addConfigOptions(ConfigOption.AUTH_ONLY_MODE);
//    }

    return builder
        .id(node.getObjectId())
        .type(node.getBodyType())
        .locked(false)
        .articles(service.getArticles())
        .intlValues(labels.stream()
            .collect(Collectors.toMap(
                l -> l.getLocale(),
                l -> l.getLabelValue()
              )))
        .serviceName(service.getValue())
        .dialobFormName(service.getFormName())
        .dialobFormTag(service.getFormTag())
        .flowName(service.getFlowName())
        .validityStart(service.getStartDate() != null ? service.getStartDate().toString() : null)
        .validityEnd(service.getEndDate() != null ? service.getEndDate().toString() : null)
//        .description(service.getDescription())
//        .labels(tagLabels)
        .build();
  }
  
  public static ServiceProps of(WorldFsState currentState, NodeAndBody node) {
    return new Props_ArticleWorkflowBuilder(currentState, node).build();
  }

}
