package io.resys.limaone.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import jakarta.annotation.Nullable;

import org.immutables.value.Value;

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

import com.fasterxml.jackson.annotation.JsonIgnore;


@Value.Immutable
public interface Parameter extends Serializable, Comparable<Parameter> {
  String getId(); // GID
  String getName();
  Integer getOrder();
  Boolean getData();
  
  Direction getDirection();
  ValueType getValueType();
  boolean isRequired();
  
  Collection<Parameter> getProperties();
  
  @Nullable String getExtRef();
  @Nullable String getScript();
  @Nullable Class<?> getBeanType();
  @Nullable String getDescription();
  @Nullable String getValues();
  @Nullable String getRef();
  @Nullable List<String> getValueSet();
  
  @JsonIgnore Deserializer getDeserializer();
  @JsonIgnore Serializer getSerializer();
  

  public default Serializable toValue(Object value) {
    return getDeserializer().deserialize(this, value);
  }

  public default String toString(Object value) {
    return getSerializer().serialize(this, value);
  }
  @Override
  public default int compareTo(Parameter o) {
    return Integer.compare(getOrder(), o.getOrder());
  }
    
  interface Deserializer {
    Serializable deserialize(Parameter dataType, Object value);
  }

  interface Serializer {
    String serialize(Parameter dataType, Object value);
  }

  @FunctionalInterface
  interface ValueTypeResolver {
    ValueType get(Class<?> src);
  }

  enum AssociationType { ONE_TO_ONE, ONE_TO_MANY }
  enum Direction { IN, OUT }
  enum ValueType {
    TIME, 
    DATE, 
    DATE_TIME, 
    INSTANT, 
    PERIOD, 
    DURATION, 
    STRING,
    INTL,
    INTEGER, 
    LONG, 
    DECIMAL, 
    BOOLEAN, 
    PERCENT, 
    OBJECT, 
    ARRAY,
    MAP, 
    FLOW_CONTEXT;
  }
}
