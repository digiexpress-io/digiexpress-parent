package io.resys.limaone.persistence.fs;

import java.util.Collections;

import io.resys.limaone.fs.ImmutablePropertyObjectProps;

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

import io.resys.limaone.fs.WorldFsProps.PropertyObjectProps;
import io.resys.limaone.model.PropertyObject;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Props_PropertyObjectBuilder {
  private final WorldFsState currentState;
  private final NodeAndBody node;
  
  public PropertyObjectProps build() {
    final PropertyObject propObject = currentState.getBodyOfType(node);
    
    return ImmutablePropertyObjectProps.builder()
          .id(node.getObjectId())
          .type(node.getBodyType())
          .locked(false)
          .content(propObject.getContent())
          .name(propObject.getName())
          .objectType(propObject.getObjectType())
          .assetDescription(node.getDescription().map(e -> e.getText()).orElse(null))
          .labels(node.getLabels().map(e -> e.getValues()).orElse(Collections.emptyList()))
        .build();
  }
  
  public static PropertyObjectProps of(WorldFsState currentState, NodeAndBody node ) {
    return new Props_PropertyObjectBuilder(currentState, node).build();
  }

}
