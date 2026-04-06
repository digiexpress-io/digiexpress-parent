package io.resys.limaone.ast;

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
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.model.DecisionTable.HitPolicy;
import jakarta.annotation.Nullable;


@Value.Immutable
@JsonSerialize(as = ImmutableDecisionTable_AST.class)
@JsonDeserialize(as = ImmutableDecisionTable_AST.class)
public interface DecisionTable_AST extends Simple_AST {
  
  // Legacy field - not used in current implementation
  List<String> getHeaderTypes();
  
  // Legacy field - not used in current implementation  
  Map<ValueType, List<String>> getHeaderExpressions();
  
  // Determines how multiple matching rules are handled (FIRST, ALL, etc.)
  HitPolicy getHitPolicy();
  
  // List of decision rules ordered by execution priority
  List<DecisionRowNode> getRows();

  // Represents a single decision rule with input conditions and output actions
  @Value.Immutable
  @JsonSerialize(as = ImmutableDecisionRowNode.class)
  @JsonDeserialize(as = ImmutableDecisionRowNode.class)
  interface DecisionRowNode extends Serializable {
    // Unique identifier for this rule
    String getId();
    // Execution order - lower numbers execute first
    int getOrder();
    // List of cells containing values for each parameter
    List<DecisionCellNode> getCells();
  }

  // Represents a single cell in a decision rule containing a value for a specific parameter
  @Value.Immutable
  @JsonSerialize(as = ImmutableDecisionCellNode.class)
  @JsonDeserialize(as = ImmutableDecisionCellNode.class)
  interface DecisionCellNode extends Serializable {
    // Unique identifier for this cell
    String getId();
    // References the Parameter.id this cell provides a value for
    String getHeader();
    // The actual rule value (e.g., "in [\"CAREFUL\"]", ">= 3", "GREEN")
    @Nullable    
    String getValue();
  }
}
