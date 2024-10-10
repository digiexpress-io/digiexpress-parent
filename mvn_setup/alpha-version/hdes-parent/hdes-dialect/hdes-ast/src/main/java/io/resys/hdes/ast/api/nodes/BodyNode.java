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
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;

public interface BodyNode extends HdesNode {
  
  BodyId getId();
  Headers getHeaders();
  
  @Value.Immutable
  interface Headers extends HdesNode {
    List<TypeDef> getAcceptDefs();
    List<TypeDef> getReturnDefs();
  }
  
  interface TypeDef extends HdesNode {
    Boolean getRequired();
    String getName();
    Boolean getArray();
    ContextTypeDef getContext();
  }
  
  enum ContextTypeDef { 
    ACCEPTS, RETURNS, 
    CONSTANTS, MATCHES, EXPRESSION, 
    
    STEP_END, STEP_ITERATOR, 
    STEP_CALL, STEP_AS, STEP_RETURNS }
  
  enum ScalarType {
    STRING, INTEGER, BOOLEAN, DECIMAL,
    DATE, DATETIME, TIME,
  }
  
  @Value.Immutable
  interface EmptyBody extends BodyNode { }
  
  
  @Value.Immutable
  interface BodyId extends HdesNode {
    String getValue();
  }

  @Value.Immutable
  interface Literal extends HdesNode {
    ScalarType getType();
    String getValue();
  }
  
  @Value.Immutable
  interface ObjectDef extends TypeDef {
    List<TypeDef> getValues();
  }
  
  @Value.Immutable
  interface ScalarDef extends TypeDef {
    Optional<String> getDebugValue();
    Optional<ExpressionBody> getFormula();
    Optional<Boolean> getFormulaOverAll();
    ScalarType getType();
  }
}
