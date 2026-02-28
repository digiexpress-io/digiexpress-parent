package io.resys.limaone.spi.compiler.decisiontable;

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
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.hdes.client.api.programs.ImmutableDecisionProgram;
import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.DecisionProgram;
import io.resys.limaone.program.ImmutableDecisionRow;
import io.resys.limaone.program.ImmutableDecisionRowAccepts;
import io.resys.limaone.program.ImmutableDecisionRowReturns;
import io.resys.limaone.spi.program.ProgramException;
import io.resys.limaone.spi.program.expression.ExpressionProgramFactory;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class DecisionProgram_Compiler {

  private final DecisionTable_AST ast;
  

  public DecisionProgram compile() {
    try {
      final var program = ImmutableDecisionProgram.builder().hitPolicy(ast.getHitPolicy());
      
      final var accepts = ast.getHeaders().getAcceptDefs().stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
      
      final var returns = new HashMap<>(ast.getHeaders()
          .getReturnDefs().stream()
          .collect(Collectors.toMap(e -> e.getId(), e -> e)));
      
      accepts.values().forEach(e -> {
        returns.put("_" + e.getId(), e);
      });
      
      
      final List<DecisionTable_AST.DecisionRowNode> rows = new ArrayList<>(ast.getRows());
      Collections.sort(rows, (o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
      

      for(var row : rows) {

        final var programRow = ImmutableDecisionRow.builder().order(row.getOrder());
        for(final var value : row.getCells()) {
          
          if(accepts.containsKey(value.getHeader())) {
            if(value.getValue() == null || value.getValue().isBlank()) {
              continue;
            }
            final var typeDef = accepts.get(value.getHeader());
            programRow.addAccepts(ImmutableDecisionRowAccepts.builder()
                .key(typeDef)
                .expression(ExpressionProgramFactory.build(value.getValue(), typeDef.getValueType()))
                .build());
          } else {
            final var typeDef = returns.get(value.getHeader());
            if(value.getValue() == null && typeDef.getValueType() != ValueType.INTL) {
              continue;
            }
            
            try {
              programRow.addReturns(ImmutableDecisionRowReturns.builder()
                  .key(typeDef)
                  .value(typeDef.toValue(value.getValue()))
                  .build());
            } catch(Exception e) {

              throw new DecisionRowException(
                  row.getOrder(), typeDef.getOrder(),
                  "Failed to create expression: '" + value.getValue() + "'!" +
                  System.lineSeparator() + e.getMessage(), e);
              
            }
          }
        }
        program.addRows(programRow.build());
      }
      return program.build();
    } catch(ProgramException | DecisionRowException ex) {
      throw ex;
      
    } catch(Exception e) {
      throw new ProgramException(
          "Failed to create decision program from ast: '" + ast.getName() + "'!" +
          System.lineSeparator() + e.getMessage(), e);
    }

  }
}
