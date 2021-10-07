package io.resys.hdes.client.api.ast;

import java.io.Serializable;
import java.util.List;

/*-
 * #%L
 * wrench-assets-datatype
 * %%
 * Copyright (C) 2016 - 2021 Copyright 2020 ReSys OÜ
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

import javax.annotation.Nullable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public interface AstType {
  
  String getName();
  @Nullable
  String getDescription();
  int getRev();
  List<AstCommandType> getCommands();
  
  
  
  enum Direction { IN, OUT }
  enum ValueType {
    TIME, DATE, DATE_TIME, INSTANT, STRING, INTEGER, LONG, DECIMAL, BOOLEAN, PERIOD, DURATION, PERCENT, OBJECT, ARRAY;
  }
  
  @JsonSerialize(as = ImmutableAstCommandType.class)
  @JsonDeserialize(as = ImmutableAstCommandType.class)
  @Value.Immutable
  public interface AstCommandType extends Serializable {
    @Nullable
    String getId();
    @Nullable
    String getValue();
    AstCommandValue getType();
    
    enum AstCommandValue {
      // flow and service related
      SET, ADD, DELETE, SET_BODY,
      
      
      // DT related
      SET_NAME,
      SET_DESCRIPTION,
      IMPORT_CSV,    
      IMPORT_ORDERED_CSV,

      MOVE_ROW,
      MOVE_HEADER,
      INSERT_ROW,
      COPY_ROW,
      
      SET_HEADER_TYPE,
      SET_HEADER_REF,
      
      SET_HEADER_SCRIPT,
      SET_HEADER_DIRECTION,
      SET_HEADER_EXPRESSION,
      SET_HIT_POLICY,
      SET_CELL_VALUE,

      DELETE_CELL,
      DELETE_HEADER,
      DELETE_ROW,

      ADD_LOG,
      ADD_HEADER_IN,
      ADD_HEADER_OUT,
      ADD_ROW,
    }
  }

}
