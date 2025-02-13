package io.resys.hdes.client.spi;

/*-
 * #%L
 * hdes-client-api
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.resys.hdes.client.api.HdesAstTypes.DataTypeAstBuilder;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.ImmutableTypeDef;
import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Deserializer;
import io.resys.hdes.client.api.ast.TypeDef.Direction;
import io.resys.hdes.client.api.ast.TypeDef.Serializer;
import io.resys.hdes.client.api.ast.TypeDef.ValueType;
import io.resys.hdes.client.api.ast.TypeDef.ValueTypeResolver;
import io.resys.hdes.client.api.exceptions.HdesJsonException;
import io.resys.hdes.client.api.programs.ExpressionProgram;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.hdes.client.spi.expression.ExpressionProgramFactory;
import io.resys.hdes.client.spi.serializers.DateDataTypeDeserializer;
import io.resys.hdes.client.spi.serializers.DateTimeDataTypeDeserializer;
import io.resys.hdes.client.spi.serializers.GenericDataTypeDeserializer;
import io.resys.hdes.client.spi.serializers.GenericDataTypeSerializer;
import io.resys.hdes.client.spi.serializers.JsonObjectDataTypeDeserializer;
import io.resys.hdes.client.spi.serializers.TimeDataTypeDeserializer;
import io.resys.hdes.client.spi.util.HdesAssert;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HdesTypeDefsFactory implements HdesTypesMapper {
  private final HdesClientConfig config;
  private final Map<ValueType, Deserializer> deserializers;
  private final Map<ValueType, Serializer> serializers;
  private final ValueTypeResolver valueTypeResolver;
  private final ObjectMapper objectMapper; 

  public HdesTypeDefsFactory(ObjectMapper objectMapper, HdesClientConfig config) {
    this.objectMapper = objectMapper;
    this.config = config;
    
    Map<ValueType, Deserializer> deserializers = new HashMap<>();
    this.deserializers = Collections.unmodifiableMap(deserializers);

    deserializers.put(ValueType.ARRAY, new GenericDataTypeDeserializer(objectMapper, List.class));
    deserializers.put(ValueType.DURATION, null);
    deserializers.put(ValueType.PERIOD, null);
    deserializers.put(ValueType.TIME, new TimeDataTypeDeserializer(objectMapper));

    deserializers.put(ValueType.OBJECT, new JsonObjectDataTypeDeserializer(objectMapper));
    deserializers.put(ValueType.STRING, new GenericDataTypeDeserializer(objectMapper, String.class));
    deserializers.put(ValueType.BOOLEAN, new GenericDataTypeDeserializer(objectMapper, Boolean.class));
    deserializers.put(ValueType.DECIMAL, new GenericDataTypeDeserializer(objectMapper, BigDecimal.class));
    deserializers.put(ValueType.INTEGER, new GenericDataTypeDeserializer(objectMapper, Integer.class));
    deserializers.put(ValueType.LONG, new GenericDataTypeDeserializer(objectMapper, Long.class));
    deserializers.put(ValueType.PERCENT, new GenericDataTypeDeserializer(objectMapper, BigDecimal.class));
    deserializers.put(ValueType.DATE, new DateDataTypeDeserializer(objectMapper));
    deserializers.put(ValueType.DATE_TIME, new DateTimeDataTypeDeserializer(objectMapper));

    Map<ValueType, Serializer> serializers = new HashMap<>();
    this.serializers = Collections.unmodifiableMap(serializers);

    Serializer dataTypeSerializer = new GenericDataTypeSerializer(objectMapper);
    serializers.put(ValueType.ARRAY, dataTypeSerializer);
    serializers.put(ValueType.OBJECT, dataTypeSerializer);
    serializers.put(ValueType.DURATION, dataTypeSerializer);
    serializers.put(ValueType.PERIOD, dataTypeSerializer);
    serializers.put(ValueType.TIME, dataTypeSerializer);
    serializers.put(ValueType.STRING, dataTypeSerializer);
    serializers.put(ValueType.BOOLEAN, dataTypeSerializer);
    serializers.put(ValueType.DECIMAL, dataTypeSerializer);
    serializers.put(ValueType.INTEGER, dataTypeSerializer);
    serializers.put(ValueType.LONG, dataTypeSerializer);
    serializers.put(ValueType.PERCENT, dataTypeSerializer);
    serializers.put(ValueType.DATE, dataTypeSerializer);
    serializers.put(ValueType.DATE_TIME, dataTypeSerializer);

    Map<Class<?>, ValueType> valueTypes = new HashMap<>();
    valueTypes.put(List.class, ValueType.ARRAY);
    valueTypes.put(Duration.class, ValueType.DURATION);
    valueTypes.put(Period.class, ValueType.PERIOD);
    valueTypes.put(LocalTime.class, ValueType.TIME);
    valueTypes.put(String.class, ValueType.STRING);
    valueTypes.put(Boolean.class, ValueType.BOOLEAN);
    valueTypes.put(BigDecimal.class, ValueType.DECIMAL);
    valueTypes.put(Integer.class, ValueType.INTEGER);
    valueTypes.put(Long.class, ValueType.LONG);
    valueTypes.put(LocalDate.class, ValueType.DATE);
    valueTypes.put(LocalDateTime.class, ValueType.DATE_TIME);
    valueTypeResolver = src -> valueTypes.containsKey(src) ? valueTypes.get(src) : ValueType.OBJECT;

  }

  @Override
  public DataTypeAstBuilder dataType() {
    return new GenericDataTypeBuilder();
  }
  
  @Override
  public ExpressionProgram expression(ValueType valueType, String src) {
    ExpressionProgram expression = ExpressionProgramFactory.builder()
        .objectMapper(objectMapper)
        .valueType(valueType)
        .src(src).build();
    return expression;
  }
  
  public class GenericDataTypeBuilder implements DataTypeAstBuilder {
    private Boolean required;
    private String name;
    private String extRef;
    private ValueType valueType;
    private Direction direction;
    private Class<?> beanType;
    private String description;
    private String values;
    private List<TypeDef> properties = new ArrayList<>();
    private String ref;
    private TypeDef dataType;
    private Integer order;
    private String script;
    private String id;
    private List<String> valueSet;
    private boolean data = true;

    @Override
    public DataTypeAstBuilder required(boolean required) {
      this.required = required;
      return this;
    }
    @Override
    public DataTypeAstBuilder data(boolean data) {
      this.data = data;
      return this;
    }
    @Override
    public DataTypeAstBuilder name(String name) {
      this.name = name;
      return this;
    }
    @Override
    public DataTypeAstBuilder order(Integer order) {
      this.order = order;
      return this;
    }
    @Override
    public DataTypeAstBuilder script(String script) {
      this.script = script;
      return this;
    }
    @Override
    public DataTypeAstBuilder id(String id) {
      this.id = id;
      return this;
    }
    @Override
    public DataTypeAstBuilder extRef(String extRef) {
      this.extRef = extRef;
      return this;
    }
    @Override
    public DataTypeAstBuilder valueType(ValueType valueType) {
      this.valueType = valueType;
      return this;
    }
    @Override
    public DataTypeAstBuilder direction(Direction direction) {
      this.direction = direction;
      return this;
    }
    @Override
    public DataTypeAstBuilder beanType(Class<?> beanType) {
      this.beanType = beanType;
      return this;
    }
    @Override
    public DataTypeAstBuilder description(String description) {
      this.description = description;
      return this;
    }
    @Override
    public DataTypeAstBuilder values(String values) {
      this.values = values;
      return this;
    }
    @Override
    public DataTypeAstBuilder ref(String ref, TypeDef dataType) {
      HdesAssert.isTrue(ref != null, () -> "ref can't be null!");
      HdesAssert.isTrue(dataType != null, () -> "dataType can't be null for ref: " + ref + "!");
      this.dataType = dataType;
      return this;
    }
    @Override
    public DataTypeAstBuilder property() {
      return new GenericDataTypeBuilder() {
        @Override
        public TypeDef build() {
          TypeDef property = super.build();
          properties.add(property);
          return property;
        }
      };
    }
    @Override
    public DataTypeAstBuilder valueSet(List<String> valueSet) {
      this.valueSet = valueSet;
      return this;
    }
    @Override
    public TypeDef build() {
      HdesAssert.notNull(name, () -> "name can't be null!");

      if(dataType != null) {
        valueType = dataType.getValueType();
        properties.addAll(dataType.getProperties());

        Deserializer deserializer = dataType.getDeserializer();
        Serializer serializer = dataType.getSerializer();
        return ImmutableTypeDef.builder()
            .id(id).script(script).order(order)
            .name(name).ref(ref).description(description)
            .direction(direction)
            .valueType(valueType)
            .beanType(beanType)
            .isRequired(Boolean.TRUE.equals(required))
            .values(values)
            .extRef(extRef)
            .properties(properties)
            .valueSet(valueSet)
            .deserializer(deserializer)
            .serializer(serializer)
            .build();
      }

      if(valueType == null) {
        HdesAssert.notNull(beanType, () -> "beanType can't be null!");
        valueType = valueTypeResolver.get(beanType);
      }

      Deserializer deserializer = deserializers.get(valueType);
      Serializer serializer = serializers.get(valueType);

      HdesAssert.notNull(valueType, () -> "valueType can't be null!");
      return ImmutableTypeDef.builder()
          .id(id).script(script).order(order)
          .name(name)
          .ref(ref)
          .extRef(extRef)
          .data(data)
          .description(description)
          .direction(direction)
          .valueType(valueType)
          .beanType(beanType)
          .isRequired(Boolean.TRUE.equals(required))
          .values(values)
          .properties(properties)
          .valueSet(valueSet)
          .deserializer(deserializer)
          .serializer(serializer)
          .build();
    }
  }
  
  @Override
  public String commandsString(List<AstCommand> commands) {
    try {
      return objectMapper.writeValueAsString(commands);
    } catch (IOException e) {
      throw new HdesJsonException(e.getMessage(), e);
    }
  }
  
  @Override
  public String toJson(Object anyObject) {
    try {
      return objectMapper.writeValueAsString(anyObject);
    } catch (IOException e) {
      throw new HdesJsonException(e.getMessage(), e);
    }
  }
  
  @Override
  public ArrayNode commandsJson(String commands) {
    try {
      return (ArrayNode) objectMapper.readTree(commands);
    } catch (IOException e) {
      throw new HdesJsonException(e.getMessage(), e);
    }
  }
  @Override
  public List<AstCommand> commandsList(String commands) {
    try {
      return objectMapper.readValue(commands, new TypeReference<List<AstCommand>>() {});
    } catch (IOException e) {
      throw new HdesJsonException(e.getMessage(), e);
    }
  }
  
  @SuppressWarnings({ "unchecked" })
  @Override
  public Map<String, Serializable> toMap(Object entity) {
    try {
      if(entity instanceof String) {
        return objectMapper.readValue((String) entity, Map.class);
      }
      
      return objectMapper.convertValue(entity, Map.class);
    } catch (Exception e) {
      throw new HdesJsonException(e.getMessage(), e);
    }
  }
  
  @SuppressWarnings({ "unchecked" })
  @Override
  public Map<String, Serializable> toMap(JsonNode entity) {
    try {
      return objectMapper.convertValue(entity, Map.class);
    } catch (Exception e) {
      throw new HdesJsonException(e.getMessage(), e);
    }
  }
  
  @Override
  public <T> T toType(Object value, Class<T> toType) {
    try {
      return objectMapper.convertValue(value, toType);
    } catch (Exception e) {
      throw new HdesJsonException(e.getMessage(), e);
    }
  }

  @Override
  public <T> T parseJson(String value, Class<T> toType) {
    try {
      return objectMapper.readValue(value, toType);
    } catch (Exception e) {
      throw new HdesJsonException(e.getMessage(), e);
    }
  }
     
  public HdesClientConfig config() {
    return config;
  }

}
