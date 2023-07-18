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

import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstDecision.AstDecisionCell;
import io.resys.hdes.client.api.ast.AstDecision.AstDecisionRow;
import io.resys.hdes.client.api.exceptions.DecisionProgramException;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.api.programs.ImmutableDecisionProgram;
import io.resys.hdes.client.api.programs.ImmutableDecisionRow;
import io.resys.hdes.client.api.programs.ImmutableDecisionRowAccepts;
import io.resys.hdes.client.api.programs.ImmutableDecisionRowReturns;

public class DecisionProgramBuilder {

  private final HdesTypesMapper typesFactory;
  
  public DecisionProgramBuilder(HdesTypesMapper typesFactory) {
    super();
    this.typesFactory = typesFactory;
  }

  public DecisionProgram build(AstDecision ast) {
    try {
      final var program = ImmutableDecisionProgram.builder().hitPolicy(ast.getHitPolicy());
      
      final var accepts = ast.getHeaders().getAcceptDefs().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
      final var returns = ast.getHeaders().getReturnDefs().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
      final List<AstDecisionRow> rows = new ArrayList<>(ast.getRows());
      Collections.sort(rows, (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
      
      for(var row : rows) {
        final var programRow = ImmutableDecisionRow.builder().order(row.getOrder());
        for(AstDecisionCell value : row.getCells()) {
          
          if(accepts.containsKey(value.getHeader())) {
            if(value.getValue() == null || value.getValue().isBlank()) {
              continue;
            }
            final var typeDef = accepts.get(value.getHeader());
            programRow.addAccepts(ImmutableDecisionRowAccepts.builder()
                .key(typeDef)
                .expression(typesFactory.expression(typeDef.getValueType(), value.getValue()))
                .build());
          } else {
            if(value.getValue() == null) {
              continue;
            }
            
            final var typeDef = returns.get(value.getHeader());
            try {
              programRow.addReturns(ImmutableDecisionRowReturns.builder()
                  .key(typeDef)
                  .value(typeDef.toValue(value.getValue()))
                  .build());
            } catch(Exception e) {

              throw new DecisionProgramException(
                  "Failed to create expression: '" + value.getValue() + "'!" +
                  System.lineSeparator() + e.getMessage(), e);
              
            }
          }
        }
        program.addRows(programRow.build());
      }
      return program.build();
    } catch(Exception e) {
      throw new DecisionProgramException(
          "Failed to create decision program from ast: '" + ast.getName() + "'!" +
          System.lineSeparator() + e.getMessage(), e);
    }

  }
}
