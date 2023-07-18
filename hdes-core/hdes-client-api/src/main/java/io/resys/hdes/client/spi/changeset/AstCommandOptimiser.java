package io.resys.hdes.client.spi.changeset;

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

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesClient.HdesTypesMapper;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstDecision.AstDecisionRow;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.ast.TypeDef;
import io.resys.hdes.client.api.ast.TypeDef.Direction;
import io.resys.hdes.client.spi.decision.DecisionAstBuilderImpl;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AstCommandOptimiser {
  private final HdesTypesMapper defs;

  public AstCommandOptimiser(HdesClient client) {
    super();
    this.defs = client.mapper();
  }

  public List<AstCommand> optimise(List<AstCommand> commands, AstBodyType type) {
    switch (type) {
    case DT: return visitDt(commands);
    case FLOW_TASK: return visitSt(commands);
    case FLOW: return visitFl(commands);
    
    default: throw new IllegalArgumentException("unknown type: '" + type + "'");
    }
  }
  
  private List<AstCommand> visitDt(List<AstCommand> src) {
    final var dt = new DecisionAstBuilderImpl(defs).src(src).build();

    final List<TypeDef> headers = new ArrayList<>();
    headers.addAll(dt.getHeaders().getAcceptDefs());
    headers.addAll(dt.getHeaders().getReturnDefs());
    headers.sort((o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
    
    
    final List<AstCommand> commands = createHeaderCommands(headers);

    createRow(headers, dt.getRows(), commands);
    commands.add(ImmutableAstCommand.builder().value(dt.getHitPolicy().name()).type(AstCommandValue.SET_HIT_POLICY).build());
    commands.add(ImmutableAstCommand.builder().value(dt.getName()).type(AstCommandValue.SET_NAME).build());
    commands.add(ImmutableAstCommand.builder().value(dt.getDescription()).type(AstCommandValue.SET_DESCRIPTION).build());

    return commands;
  }

  private List<AstCommand> visitSt(List<AstCommand> original) {
    final var changes = AstChangesetFactory.src(original, null);
    final var value = new StringBuilder();
    final var iterator = changes.getSrc().iterator();

    while (iterator.hasNext()) {
      final var src = iterator.next();
      String lineContent = src.getCommands().get(src.getCommands().size() - 1).getValue();

      if (!StringUtils.isEmpty(lineContent)) {
        value.append(lineContent);
      }
      value.append(System.lineSeparator());
    }
    return Arrays.asList(ImmutableAstCommand.builder()
        .value(value.toString())
        .type(AstCommandValue.SET_BODY).build());
  }

  private List<AstCommand> visitFl(List<AstCommand> original) {
    final var changes = AstChangesetFactory.src(original, null);
    final var value = new StringBuilder();
    final var iterator = changes.getSrc().iterator();

    while (iterator.hasNext()) {
      final var src = iterator.next();
      String lineContent = src.getCommands().get(src.getCommands().size() - 1).getValue();

      if (!StringUtils.isEmpty(lineContent)) {
        value.append(lineContent);
      }
      value.append(System.lineSeparator());
    }
    return Arrays.asList(ImmutableAstCommand.builder()
        .value(value.toString())
        .type(AstCommandValue.SET_BODY).build());
  }

  
  private void createRow(List<TypeDef> headers, List<AstDecisionRow> nodes, List<AstCommand> result) {  
    
    int rows = 1;
    for(final var node : nodes) {
    
      int nextId = headers.size() * rows + rows;
      result.add(ImmutableAstCommand.builder().type(AstCommandValue.ADD_ROW).build());
  
      Map<String, String> entries = new HashMap<>();
      node.getCells().forEach(e -> entries.put(e.getHeader(), e.getValue()));
  
      for(final var header : headers) {
        String value = entries.get(header.getId());
        result.add(ImmutableAstCommand.builder()
            .id(String.valueOf(nextId++))
            .value(value)
            .type(AstCommandValue.SET_CELL_VALUE)
            .build());
      }
      
      rows++;
    }
  }

  private List<AstCommand> createHeaderCommands(List<TypeDef> headers) {
    final List<AstCommand> result = new ArrayList<>();
    
    int index = 0;
    for(TypeDef dataType : headers) {
      String id = String.valueOf(index);
      result.add(ImmutableAstCommand.builder().type(dataType.getDirection() == Direction.IN ? AstCommandValue.ADD_HEADER_IN : AstCommandValue.ADD_HEADER_OUT).build());
      result.add(ImmutableAstCommand.builder().id(id).value(dataType.getName()).type(AstCommandValue.SET_HEADER_REF).build());
      result.add(ImmutableAstCommand.builder().id(id).value(dataType.getScript()).type(AstCommandValue.SET_HEADER_SCRIPT).build());
      result.add(ImmutableAstCommand.builder().id(id).value(dataType.getValueType() == null ? null : dataType.getValueType().name()).type(AstCommandValue.SET_HEADER_TYPE).build());
      if(dataType.getExtRef() != null) {
        result.add(ImmutableAstCommand.builder().id(id).value(dataType.getExtRef()).type(AstCommandValue.SET_HEADER_EXTERNAL_REF).build());
      }
      if(dataType.getValueSet() != null) {
        result.add(ImmutableAstCommand.builder().id(id).value(String.join(", ", dataType.getValueSet())).type(AstCommandValue.SET_VALUE_SET).build());
      }
      index++;
    }
    return result;
  }

}
