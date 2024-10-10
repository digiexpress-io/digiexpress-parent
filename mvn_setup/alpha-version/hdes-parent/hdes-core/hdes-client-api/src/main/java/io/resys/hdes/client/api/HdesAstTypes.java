package io.resys.hdes.client.api;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Direction;
import io.resys.hdes.client.api.ast.TypeDef.ValueType;

import java.util.List;

public interface HdesAstTypes {

  DecisionAstBuilder decision();
  FlowAstBuilder flow();
  ServiceAstBuilder service();
  DataTypeAstBuilder dataType();
  TagAstBuilder tag();
  
  interface DataTypeAstBuilder {
    DataTypeAstBuilder id(String id);
    DataTypeAstBuilder order(Integer order);
    DataTypeAstBuilder valueType(ValueType valueType);
    
    DataTypeAstBuilder ref(String ref, TypeDef dataType);
    DataTypeAstBuilder extRef(String extRef);
    DataTypeAstBuilder required(boolean required);
    DataTypeAstBuilder data(boolean data);
    DataTypeAstBuilder name(String name);
    DataTypeAstBuilder script(String script);
    DataTypeAstBuilder direction(Direction direction);
    DataTypeAstBuilder description(String description);
    
    DataTypeAstBuilder beanType(Class<?> beanType);
    DataTypeAstBuilder values(String values);
    DataTypeAstBuilder property();
    DataTypeAstBuilder valueSet(List<String> valueSet);
    TypeDef build();
  }

  interface TagAstBuilder {
    TagAstBuilder src(List<AstCommand> src);
    TagAstBuilder src(JsonNode src);
    AstTag build();
  }
  
  interface DecisionAstBuilder {
    DecisionAstBuilder src(List<AstCommand> src);
    DecisionAstBuilder src(JsonNode src);
    DecisionAstBuilder rev(Integer version);
    AstDecision build();
  }
  
  interface FlowAstBuilder {
    FlowAstBuilder src(List<AstCommand> src);
    FlowAstBuilder src(ArrayNode src);
    FlowAstBuilder srcAdd(int line, String value);
    FlowAstBuilder srcDel(int line);
    FlowAstBuilder rev(Integer version);
    AstFlow build();
  }

  interface ServiceAstBuilder {
    ServiceAstBuilder src(List<AstCommand> src);
    ServiceAstBuilder src(ArrayNode src);
    ServiceAstBuilder rev(Integer version);
    AstService build();
  }
}
