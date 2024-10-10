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

import java.util.List;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.InvocationNode.SimpleInvocation;

public interface MappingNode extends HdesNode {

  /**
   * Mapping types
   */
  interface MappingDef extends MappingNode { }
  
  @Value.Immutable  
  interface ObjectMappingDef extends MappingDef {
    List<MappingDef> getValues();
  }
  
  @Value.Immutable
  interface FieldMappingDef extends MappingDef {
    SimpleInvocation getLeft();
    MappingDef getRight();
  }
  
  @Value.Immutable  
  interface ExpressionMappingDef extends MappingDef {
    ExpressionBody getValue();
  }
  
  @Value.Immutable  
  interface ArrayMappingDef extends MappingDef {
    List<MappingDef> getValues();
  }
  
  @Value.Immutable  
  interface FastMappingDef extends MappingDef {
    InvocationNode getValue();
  }
}
