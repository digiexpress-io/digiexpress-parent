package io.resys.hdes.client.spi.flow.ast;

/*-
 * #%L
 * wrench-assets-flow
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import io.resys.hdes.client.api.HdesAstTypes.DataTypeAstBuilder;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstBody.AstCommandRange;
import io.resys.hdes.client.api.ast.AstBody.Headers;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowInputNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowNode;
import io.resys.hdes.client.api.ast.AstFlow.AstFlowRoot;
import io.resys.hdes.client.api.ast.AstFlow.FlowAstAutocomplete;
import io.resys.hdes.client.api.ast.ImmutableAstCommandRange;
import io.resys.hdes.client.api.ast.ImmutableFlowAstAutocomplete;
import io.resys.hdes.client.api.ast.ImmutableHeaders;
import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Direction;
import io.resys.hdes.client.api.ast.TypeDef.ValueType;
import io.resys.hdes.client.api.exceptions.FlowAstException;

public class AstFlowNodesFactory {

  public static HeadersBuilder headers(HdesClient types) {
    return new HeadersBuilder(() -> types.types().dataType());
  }
  public static HeadersBuilder headers(HdesTypesMapper types) {
    return new HeadersBuilder(() -> types.dataType());
  }
  public static AcBuilder ac() {
    return new AcBuilder();
  }
  
  public static RangeBuilder range() {
    return new RangeBuilder();
  }
  
  public static class RangeBuilder {
    
    public ImmutableAstCommandRange build(int start, int end, Boolean insert) {
      return ImmutableAstCommandRange.builder().start(start).end(end).insert(insert).build();
    }

    public ImmutableAstCommandRange build(int start, int end, Boolean insert, Integer column) {
      return ImmutableAstCommandRange.builder().start(start).end(end).insert(insert).column(column).build();
    }

    public ImmutableAstCommandRange build(int start, int end) {
      return ImmutableAstCommandRange.builder().start(start).end(end).build();
    }

    public ImmutableAstCommandRange build(int start) {
      return ImmutableAstCommandRange.builder().start(start).end(start).build();
    }
  }

  public static class AcBuilder {
    private static final String FIELD = ":";
    private String id;
    private final Collection<AstCommandRange> range = new ArrayList<>();
    private final Collection<String> value = new ArrayList<>();

    public AcBuilder id(String id) {
      this.id = id;
      return this;
    }

    public AcBuilder addRange(int start, int end) {
      range.add(AstFlowNodesFactory.range().build(start, end));
      return this;
    }
    public AcBuilder addRange(AstCommandRange range) {
      this.range.add(range);
      return this;
    }
    public AcBuilder addRange(Collection<AstCommandRange> range) {
      this.range.addAll(range);
      return this;
    }
    public AcBuilder addField(int indent, String fieldName) {
      this.value.add(getIndent(indent) + fieldName + FIELD);
      return this;
    }

    public AcBuilder addField(int indent, String fieldName, Serializable value) {
      this.value.add(getIndent(indent) + fieldName + FIELD + " " + value);
      return this;
    }

    public AcBuilder addField(String fieldName) {
      this.value.add(fieldName + FIELD);
      return this;
    }

    public AcBuilder addField(String fieldName, Serializable value) {
      this.value.add(fieldName + FIELD + " " + value);
      return this;
    }

    public AcBuilder addValue(String ... value) {
      this.value.addAll(Arrays.asList(value));
      return this;
    }
    public AcBuilder addValue(Collection<String> value) {
      this.value.addAll(value);
      return this;
    }
    private String getIndent(int indent) {
      StringBuilder result = new StringBuilder();
      for(int index = 0; index < indent; index++) {
        result.append(" ");
      }
      return result.toString();
    }
    public FlowAstAutocomplete build() {
      return ImmutableFlowAstAutocomplete.builder()
          .id(id)
          .range(range)
          .value(value)
          .build();
    }

  }
  
  public static String getStringValue(AstFlowNode node) {
    if (node == null || node.getValue() == null) {
      return null;
    }
    return node.getValue();
  }

  public static boolean getBooleanValue(AstFlowNode node) {
    if (node == null || node.getValue() == null) {
      return false;
    }
    return Boolean.parseBoolean(node.getValue());
  }
  
  public static class HeadersBuilder {
    private final Supplier<DataTypeAstBuilder> types;
    public HeadersBuilder(Supplier<DataTypeAstBuilder> types) {
      this.types = types;      
    }
    public Headers build(AstFlowRoot data) {
      Map<String, AstFlowInputNode> inputs = data.getInputs();

      int index = 0;
      Collection<TypeDef> result = new ArrayList<>();
      for (Map.Entry<String, AstFlowInputNode> entry : inputs.entrySet()) {
        if (entry.getValue().getType() == null) {
          continue;
        }
        try {
          ValueType valueType = ValueType.valueOf(entry.getValue().getType().getValue());
          boolean required = getBooleanValue(entry.getValue().getRequired());
          result.add(this.types.get()
              .id(entry.getValue().getStart() + "")
              .order(index++)
              .name(entry.getKey()).valueType(valueType).direction(Direction.IN).required(required)
              .values(getStringValue(entry.getValue().getDebugValue()))
              .build());
          
        } catch (Exception e) {
          final String msg = String.format("Failed to convert data type from: %s, error: %s", entry.getValue().getType().getValue(), e.getMessage());
          throw new FlowAstException(msg, e);
        }
      }
      return ImmutableHeaders.builder().acceptDefs(result).build();
    } 
  }

}
