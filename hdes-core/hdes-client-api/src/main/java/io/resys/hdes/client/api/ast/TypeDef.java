package io.resys.hdes.client.api.ast;

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
import org.immutables.value.Value;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;


@Value.Immutable
public abstract class TypeDef implements Serializable, Comparable<TypeDef> {
  private static final long serialVersionUID = -1945170579949676929L;
  
  public abstract String getId(); // GID
  public abstract String getName();
  public abstract Integer getOrder();
  public abstract Boolean getData();
  
  public abstract Direction getDirection();
  public abstract ValueType getValueType();
  public abstract boolean isRequired();
  public abstract Collection<TypeDef> getProperties();
  @Nullable
  public abstract String getExtRef();
  @Nullable
  public abstract String getScript();
  @Nullable
  public abstract Class<?> getBeanType();
  @Nullable
  public abstract String getDescription();
  @Nullable
  public abstract String getValues();
  @Nullable
  public abstract String getRef();
  @Nullable
  public abstract List<String> getValueSet();
  
  @JsonIgnore
  public abstract Deserializer getDeserializer();
  @JsonIgnore
  public abstract Serializer getSerializer();
  @JsonIgnore
  public Serializable toValue(Object value) {
    return getDeserializer().deserialize(this, value);
  }
  @JsonIgnore
  public String toString(Object value) {
    return getSerializer().serialize(this, value);
  }
  @JsonIgnore
  @Override
  public int compareTo(TypeDef o) {
    return Integer.compare(getOrder(), o.getOrder());
  }
    
  public interface Deserializer {
    Serializable deserialize(TypeDef dataType, Object value);
  }

  public interface Serializer {
    String serialize(TypeDef dataType, Object value);
  }

  @FunctionalInterface
  public interface ValueTypeResolver {
    ValueType get(Class<?> src);
  }

  public enum AssociationType { ONE_TO_ONE, ONE_TO_MANY }
  public enum Direction { IN, OUT }
  public enum ValueType {
    TIME, DATE, DATE_TIME, INSTANT, PERIOD, DURATION, 
    STRING, INTEGER, LONG, DECIMAL, BOOLEAN, PERCENT, OBJECT, ARRAY,
    MAP, FLOW_CONTEXT;
  }
}
