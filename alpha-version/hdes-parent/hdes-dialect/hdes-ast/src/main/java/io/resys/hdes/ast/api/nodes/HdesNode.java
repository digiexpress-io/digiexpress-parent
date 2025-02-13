package io.resys.hdes.ast.api.nodes;
/*-
 * #%L
 * hdes-ast
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
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

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nullable;

import org.immutables.value.Value;

public interface HdesNode extends Serializable {
  Token getToken();

  @Value.Immutable
  interface ErrorNode {
    String getBodyId();
    HdesNode getTarget();
    Optional<HdesNode> getTargetLink();
    String getMessage();
  }
  
  @Value.Immutable
  interface Token {
    String getText();
    Position getStart();
    @Nullable
    Position getEnd();
  }

  @Value.Immutable
  interface Position {
    int getLine();
    int getCol();
  }
  
  @Value.Immutable
  interface EmptyNode extends HdesNode { }
}
