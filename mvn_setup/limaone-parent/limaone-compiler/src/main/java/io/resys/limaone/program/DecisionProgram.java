package io.resys.limaone.program;

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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.immutables.value.Value;

import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.model.DecisionTable.HitPolicy;
import io.resys.limaone.model.Parameter;
import jakarta.annotation.Nullable;




public interface DecisionProgram extends Program {
  List<DecisionRow> getRows();
  HitPolicy getHitPolicy();
  String encodePrettily();
  
  DecisionTable_AST getAst();
  
  DecisionExecutor run(Map<String, Serializable> input);
  DecisionExecutor run(ProgramInput input);
  
  interface DecisionExecutor {
    DecisionExecutor callback(Consumer<DecisionTable_AST> callback);
    Map<String, Serializable> andGet();
    List<Map<String, Serializable>> andFind();
    DecisionResult andGetBody();
  }
  
  @Value.Immutable
  interface DecisionRow extends Serializable {
    int getOrder();
    
    List<DecisionRowAccepts> getAccepts();
    List<DecisionRowReturns> getReturns();
  }
  @Value.Immutable
  interface DecisionRowAccepts extends Serializable {
    Parameter getKey();
    ExpressionProgram getExpression();
  }
  @Value.Immutable
  interface DecisionRowReturns extends Serializable {
    Parameter getKey();
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
    default Map<String, Serializable> getReturnsMap() {
      return getReturns().stream().collect(Collectors.toMap(
          e -> e.getHeaderType().getName(), 
          e -> e.getUsedValue()));
    }
  }
  @Value.Immutable
  interface DecisionLogEntry extends Serializable {
    Boolean getMatch();
    Parameter getHeaderType();
    String getExpression();
    @Nullable Serializable getUsedValue();
  }
}
