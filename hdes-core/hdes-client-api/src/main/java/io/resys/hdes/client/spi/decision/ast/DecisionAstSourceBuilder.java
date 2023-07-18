package io.resys.hdes.client.spi.decision.ast;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstDecision.AstDecisionCell;
import io.resys.hdes.client.api.ast.AstDecision.AstDecisionRow;
import io.resys.hdes.client.api.ast.AstDecision.HitPolicy;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Direction;

public class DecisionAstSourceBuilder {

  
  
  public static List<AstCommand> build(List<TypeDef> headers, List<AstDecisionRow> rows, String name, String description, HitPolicy hitPolicy) {
    List<AstCommand> result = createHeaderCommands(headers);
    createRow(headers, 1, rows.iterator(), result);
    result.add(ImmutableAstCommand.builder().value(name).type(AstCommandValue.SET_NAME).build());
    result.add(ImmutableAstCommand.builder().value(description).type(AstCommandValue.SET_DESCRIPTION).build());
    result.add(ImmutableAstCommand.builder().value(hitPolicy.name()).type(AstCommandValue.SET_HIT_POLICY).build());
    return result;
  }

  private static void createRow(List<TypeDef> headers, int rows, Iterator<AstDecisionRow> it, List<AstCommand> result) {
    if(!it.hasNext()) {
      return;
    }
    int nextId = headers.size() * rows + rows;
    result.add(ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build());

    final var node = it.next();
    Map<String, AstDecisionCell> entries = node.getCells().stream().collect(Collectors.toMap(e -> e.getHeader(), e -> e));

    for(TypeDef header : headers) {
      AstDecisionCell value = entries.get(header.getName());
      result.add(ImmutableAstCommand.builder()
          .id(String.valueOf(nextId++))
          .value(value == null ? null : value.getValue())
          .type(AstCommandValue.SET_CELL_VALUE)
          .build());
    }
    createRow(headers, ++rows, it, result);
  }

  private static List<AstCommand> createHeaderCommands(List<TypeDef> headers) {
    List<AstCommand> result = new ArrayList<>();
    int index = 0;
    for(TypeDef dataType : headers) {
      String id = String.valueOf(index);
      result.add(ImmutableAstCommand.builder().type(dataType.getDirection() == Direction.IN ? AstCommandValue.ADD_HEADER_IN : AstCommandValue.ADD_HEADER_OUT).build());
      result.add(ImmutableAstCommand.builder().id(id).value(dataType.getName()).type(AstCommandValue.SET_HEADER_REF).build());
      result.add(ImmutableAstCommand.builder().id(id).value(dataType.getScript()).type(AstCommandValue.SET_HEADER_SCRIPT).build());
      result.add(ImmutableAstCommand.builder().id(id).value(dataType.getValueType() == null ? null : dataType.getValueType().name()).type(AstCommandValue.SET_HEADER_TYPE).build());
      index++;
    }
    return result;
  }
}
