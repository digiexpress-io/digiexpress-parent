package io.resys.limaone.program;

/*-
 * #%L
 * wrench-assets-dt
 * %%
 * Copyright (C) 2016 - 2018 Copyright 2016 ReSys OÜ
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

import io.resys.limaone.ast.Attribute_AST;
import io.resys.limaone.ast.DecisionTable_AST.HitPolicy;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface DecisionProgram extends Program {
  List<DecisionRow> getRows();
  HitPolicy getHitPolicy();
  
  @Value.Immutable
  interface DecisionRow extends Serializable {
    int getOrder();
    
    List<DecisionRowAccepts> getAccepts();
    List<DecisionRowReturns> getReturns();
  }
  @Value.Immutable
  interface DecisionRowAccepts extends Serializable {
    Attribute_AST getKey();
    ExpressionProgram getExpression();
  }
  @Value.Immutable
  interface DecisionRowReturns extends Serializable {
    Attribute_AST getKey();
    Serializable getValue();
  }
  
  
  @Value.Immutable
  interface DecisionResult extends ProgramResult {
    List<DecisionLog> getRejections();
    List<DecisionLog> getMatches();
  }
  @Value.Immutable
  interface DecisionLog extends Serializable {
    Boolean getMatch();
    Integer getOrder();
    List<DecisionLogEntry> getAccepts();
    List<DecisionLogEntry> getReturns();
  }
  @Value.Immutable
  interface DecisionLogEntry extends Serializable {
    Boolean getMatch();
    Attribute_AST getHeaderType();
    String getExpression();
    @Nullable
    Serializable getUsedValue();
  }
}
