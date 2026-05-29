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

import io.resys.limaone.fs.ImmutableFlowProps;
import io.resys.limaone.fs.ImmutableLabel;
import io.resys.limaone.fs.WorldFsProps;
import io.resys.limaone.fs.WorldFsProps.FlowProps;
import io.resys.limaone.fs.WorldFsProps.Label;
import io.resys.limaone.model.Flow;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_FlowBuilder {
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public FlowProps build() {
    final Flow flow = currentState.getBodyOfType(node);

    final var builder = ImmutableFlowProps.builder();

    if (Boolean.TRUE.equals(flow.getDevMode())) {
      builder.addConfigOptions(WorldFsProps.ConfigOption.DEV_MODE);
    }
    if (Boolean.TRUE.equals(flow.getDisabledMode())) {
      builder.addConfigOptions(WorldFsProps.ConfigOption.DISABLED_MODE);
    }

    return builder
        .id(node.getObjectId())
        .type(node.getBodyType())
        .locked(false)
        .name(flow.getFlowName())
        .description(flow.getDescription())
        .labels(flow.getTagLabels() == null ? Collections.emptyList() :
            flow.getTagLabels().stream()
                .map(v -> (Label) ImmutableLabel.builder().id(v).value(v).build())
                .toList())
        .build();
  }
  
  public static FlowProps of(WorldFsState currentState, NodeAndBody node) {
    return new Props_FlowBuilder(currentState, node).build();
  }

}
