package io.resys.limaone.spi.ast.decisiontable;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.resys.limaone.ast.DecisionTable_CST.YamlDecision;
import io.resys.limaone.ast.DecisionTable_CST.YamlTable;
import io.resys.limaone.ast.DecisionTable_CST.YamlTableHeader;
import io.resys.limaone.ast.DecisionTable_CST.YamlTableRow;
import io.resys.limaone.ast.DecisionTable_CST.YamlValueSet;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.DecisionTable.StatementType;
import io.resys.limaone.model.ImmutableDecisionStatement;
import io.resys.limaone.model.Parameter.ValueType;
import io.smallrye.mutiny.tuples.Tuple2;

public class DecisionCSTToCommands {
  
  private final AtomicInteger SEQUENCE = new AtomicInteger(-1);

  public List<DecisionStatement> convert(YamlDecision parseTree) {
    final List<DecisionStatement> commands = new ArrayList<>();
    
    // Set basic properties
    if (parseTree.getName() != null && parseTree.getName().getValue() != null) {
      commands.add(ImmutableDecisionStatement.builder()
          .type(StatementType.SET_NAME)
          .value(parseTree.getName().getValue())
          .build());
    }
    
    if (parseTree.getDescription() != null && parseTree.getDescription().getValue() != null) {
      commands.add(ImmutableDecisionStatement.builder()
          .type(StatementType.SET_DESCRIPTION)
          .value(parseTree.getDescription().getValue())
          .build());
    }
    
    if (parseTree.getHitPolicy() != null && parseTree.getHitPolicy().getValue() != null) {
      commands.add(ImmutableDecisionStatement.builder()
          .type(StatementType.SET_HIT_POLICY)
          .value(parseTree.getHitPolicy().getValue())
          .build());
    }
    
    // Add value sets
    for (YamlValueSet valueSet : parseTree.getValueSetNodes().values()) {
      final String valueSetDefinition = String.join(",", valueSet.getValues());
      commands.add(ImmutableDecisionStatement.builder()
          .type(StatementType.SET_VALUE_SET)
          .id(valueSet.getName())
          .value(valueSetDefinition)
          .build());
    }
    
    // Process table if present
    final YamlTable table = parseTree.getTable();
    if (table != null) {
      convertTable(table, commands);
    }
    
    return commands;
  }
  
  private void convertTable(YamlTable table, List<DecisionStatement> commands) {
    // Add headers in order (input headers first, then output headers)
    final var headers = new ArrayList<Tuple2<String, YamlTableHeader>>();
    
    for (final YamlTableHeader header : table.getHeaders()) {
      final var headerId = allocateId();
      final var headerType = header.isOutput() ? StatementType.ADD_HEADER_OUT : StatementType.ADD_HEADER_IN;
      final var headerValue = ValueType.valueOf(header.getType().toUpperCase()).name();
      headers.add(Tuple2.of(headerId, header));
      
      commands.add(ImmutableDecisionStatement.builder().type(headerType).build());
      commands.add(ImmutableDecisionStatement.builder().type(DecisionTable.StatementType.SET_HEADER_REF).id(headerId).value(header.getName()).build());
      commands.add(ImmutableDecisionStatement.builder().type(DecisionTable.StatementType.SET_HEADER_TYPE).id(headerId).value(headerValue).build());
    }
    
    // Add rows
    for (YamlTableRow row : table.getRows()) {
      
      // id must be incremented
      allocateId();
      commands.add(ImmutableDecisionStatement.builder().type(StatementType.ADD_ROW).build());
      
      final var cells = row.getCellsByHeader();
      for(final var header : headers) {
        
        final var cell = cells.get(header.getItem2().getName());
        final var cellId = allocateId();
        
        commands.add(ImmutableDecisionStatement.builder()
            .type(StatementType.SET_CELL_VALUE)
            .id(cellId)
            .value(cell.getExpression())
            .build());
      }
      

    }
  }
  
  private String allocateId() {
    return String.valueOf(SEQUENCE.incrementAndGet());
  }
  
  
}
