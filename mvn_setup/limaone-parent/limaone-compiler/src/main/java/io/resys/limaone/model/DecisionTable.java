package io.resys.limaone.model;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import jakarta.annotation.Nullable;


@Value.Immutable
@JsonSerialize(as = ImmutableDecisionTable.class)
@JsonDeserialize(as = ImmutableDecisionTable.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public interface DecisionTable extends Body {

  String getName();
  List<DecisionStatement> getNodes();
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Value.Immutable @JsonSerialize(as = ImmutableDecisionStatement.class) @JsonDeserialize(as = ImmutableDecisionStatement.class)
  interface DecisionStatement extends Serializable {
    @Nullable String getId();
    @Nullable String getValue();
    StatementType getType();
  }
  
  enum StatementType {
    SET_NAME, SET_DESCRIPTION, IMPORT_CSV, IMPORT_ORDERED_CSV,
    MOVE_ROW, MOVE_HEADER, INSERT_ROW, COPY_ROW,
    SET_HEADER_TYPE, SET_HEADER_REF, SET_HEADER_NAME, SET_HEADER_EXTERNAL_REF,
    SET_HEADER_SCRIPT, SET_HEADER_DIRECTION, SET_HEADER_EXPRESSION, SET_HIT_POLICY, SET_CELL_VALUE,
    DELETE_CELL, DELETE_HEADER, DELETE_ROW,
    ADD_HEADER_IN, ADD_HEADER_OUT, ADD_ROW, SET_VALUE_SET,
  }
  
  enum HitPolicy { FIRST, ALL }
  enum ColumnExpressionType { 
    IN, EQUALS, 
    // pattern matching for special symbols
    // "." - word separator   
    // "#" - match one or more word 
    // "*" - match one word
    QIN  
  }
  
  
  default BodyType getBodyType() {
    return BodyType.DECISION_TABLE;
  }
}
