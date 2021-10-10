package io.resys.hdes.client.spi.decision;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstDecision.Cell;
import io.resys.hdes.client.api.ast.AstDecision.Row;
import io.resys.hdes.client.api.execution.ImmutableDecisionProgram;
import io.resys.hdes.client.api.execution.ImmutableRow;
import io.resys.hdes.client.api.execution.ImmutableRowAccepts;
import io.resys.hdes.client.api.execution.ImmutableRowReturns;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.spi.HdesTypeDefsFactory;

public class DecisionProgramBuilder {

  private final HdesTypeDefsFactory typesFactory;
  
  public DecisionProgramBuilder(HdesTypeDefsFactory typesFactory) {
    super();
    this.typesFactory = typesFactory;
  }

  public DecisionProgram build(AstDecision ast) {
    final var program = ImmutableDecisionProgram.builder()
        .id(ast.getName())
        .ast(ast)
        .hitPolicy(ast.getHitPolicy());
    
    final var accepts = ast.getHeaders().getAcceptDefs().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var returns = ast.getHeaders().getReturnDefs().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final List<Row> rows = new ArrayList<>(ast.getRows());
    Collections.sort(rows, (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
    
    for(var row : rows) {
      final var programRow = ImmutableRow.builder().order(row.getOrder());
      for(Cell value : row.getCells()) {
        
        if(accepts.containsKey(value.getHeader())) {
          if(value.getValue() == null || value.getValue().isBlank()) {
            continue;
          }
          final var typeDef = accepts.get(value.getHeader());
          programRow.addAccepts(ImmutableRowAccepts.builder()
              .key(typeDef)
              .expression(typesFactory.expression(typeDef.getValueType(), value.getValue()))
              .build());
        } else {
          if(value.getValue() == null) {
            continue;
          }
          
          final var typeDef = returns.get(value.getHeader());
          programRow.addReturns(ImmutableRowReturns.builder()
              .key(typeDef)
              .value(typeDef.toValue(value.getValue()))
              .build());
        }
      }
      program.addRows(programRow.build());
    }
    return program.build();
  }
}
