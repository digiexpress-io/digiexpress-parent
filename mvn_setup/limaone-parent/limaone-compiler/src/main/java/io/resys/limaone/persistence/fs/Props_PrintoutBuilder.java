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

import java.util.stream.Collectors;

import io.resys.limaone.fs.ImmutablePrintoutProps;
import io.resys.limaone.fs.WorldFsProps.PrintoutProps;
import io.resys.limaone.model.Printout;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_PrintoutBuilder {
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public PrintoutProps build() {
    final Printout printout = currentState.getBodyOfType(node);
    
    return ImmutablePrintoutProps.builder()
        .id(node.getObjectId())
        .type(node.getBodyType())
        .locked(false)
        .printoutServiceName(printout.getServiceName())
        .orchestratorName(printout.getOrchestratorName())
        .intlValues(printout.getLabels()
            .stream()
            .collect(Collectors.toMap(
                l -> l.getLocale(),
                l -> l.getLabelValue()
            )))
        .assetDescription(node.getDescription().map(e -> e.getText()).orElse(null))
        .labels(node.getLabels().map(e -> e.getValues()).orElse(Collections.emptyList()))
        .build();
  }
  
  public static PrintoutProps of(WorldFsState currentState, NodeAndBody node) {
    return new Props_PrintoutBuilder(currentState, node).build();
  }
  
}
