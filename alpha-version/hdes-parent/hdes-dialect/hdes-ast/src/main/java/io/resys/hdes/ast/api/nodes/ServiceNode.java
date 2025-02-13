package io.resys.hdes.ast.api.nodes;

import java.util.Optional;

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

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.MappingNode.ObjectMappingDef;

public interface ServiceNode extends HdesNode {
  
  @Value.Immutable
  interface ServiceBody extends ServiceNode, BodyNode {
    CommandInvocation getCommand();
  }
  
  @Value.Immutable
  interface CommandInvocation extends ServiceNode {
    InvocationNode getClassName();
    ObjectMappingDef getMapping();
    Optional<ServicePromise> getPromise();
    
  }
  
  @Value.Immutable
  interface ServicePromise extends ServiceNode {
    Optional<ExpressionBody> getTimeout();
  }
}