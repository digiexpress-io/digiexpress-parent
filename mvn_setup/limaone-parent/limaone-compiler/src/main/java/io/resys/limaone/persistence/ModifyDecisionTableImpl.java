package io.resys.limaone.persistence;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.ast.DecisionTable_AST.DecisionRowNode;
import io.resys.limaone.authoring.ImmutableModifyDecisionTableProps;
import io.resys.limaone.authoring.ImmutableModifyDecisionTableProps.Builder;
import io.resys.limaone.authoring.ModifyDecisionTable;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.ImmutableDecisionStatement;
import io.resys.limaone.model.ImmutableDecisionTable;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Parameter;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class ModifyDecisionTableImpl extends AuthoringTemplate<ModifyDecisionTableImpl, Model<DecisionTable>> implements ModifyDecisionTable {

  private ModifyDecisionTableProps props;

  public ModifyDecisionTableImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyDecisionTable props(ModifyDecisionTableProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyDecisionTable props(Consumer<Builder> props) {
    final var builder = ImmutableModifyDecisionTableProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<DecisionTable>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.DECISION_TABLE)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.mergeModel(props.getDecisionTableId(), body.getName(), body);
      });
  }
  
  @Override
  public Uni<ModelWorld> buildTransientWorld() {
    return config.getPersistence().worldQuery()
        .docs(BodyType.DECISION_TABLE, BodyType.FLOW, BodyType.FLOW_TASK)
        .findAll()
        .onItem().transform(nextWorld -> {
          final var body = internalBuild(nextWorld);
          return nextWorld.withAny(props.getDecisionTableId(), body);
        });
  }
  
  private DecisionTable internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");
    
    final var start = world.getDecisionTables().get(props.getDecisionTableId());
    if(start == null) {
      throw new AuthoringException(props, "Decision table with id: '" + props.getDecisionTableId() + "' not found!");
    }
    
    final var decision = config.getEnvir().getAstParser().parseDecisionTable().nodes(props.getNodes()).parse();
    
    // Check for duplicate name only if the name is actually being changed
    if(!start.getBody().getName().equals(decision.getName())) {
      final var duplicate = world.getDecisionTables().values().stream()
          .filter(p -> !p.getId().equals(props.getDecisionTableId()))
          .filter(p -> p.getBody().getName().equalsIgnoreCase(decision.getName()))
          .findFirst();

      if(duplicate.isPresent()) {
        throw new AuthoringException(props, "Decision table with name: '" + decision.getName() + "' already exists!");
      }
    }
    
    final List<Parameter> headers = new ArrayList<>();
    headers.addAll(decision.getHeaders().getAcceptDefs());
    headers.addAll(decision.getHeaders().getReturnDefs());
    headers.sort((o1, o2) -> Integer.compare(o1.getOrder(), o2.getOrder()));
    
    final List<DecisionStatement> commands = createHeaderCommands(headers);
    createRow(headers, decision.getRows(), commands);
    commands.add(ImmutableDecisionStatement.builder().value(decision.getHitPolicy().name()).type(DecisionTable.StatementType.SET_HIT_POLICY).build());
    commands.add(ImmutableDecisionStatement.builder().value(decision.getName()).type(DecisionTable.StatementType.SET_NAME).build());
    commands.add(ImmutableDecisionStatement.builder().value(decision.getDescription()).type(DecisionTable.StatementType.SET_DESCRIPTION).build());


    return ImmutableDecisionTable.builder()
      .from(start.getBody())
      .name(decision.getName())
      .nodes(commands)
      .build();
  }
  
  private List<DecisionStatement> createHeaderCommands(List<Parameter> headers) {
    final List<DecisionStatement> result = new ArrayList<>();
    
    int index = 0;
    for(final var dataType : headers) {
      String id = String.valueOf(index);
      result.add(ImmutableDecisionStatement.builder().type(dataType.getDirection() == Parameter.Direction.IN ? DecisionTable.StatementType.ADD_HEADER_IN : DecisionTable.StatementType.ADD_HEADER_OUT).build());
      result.add(ImmutableDecisionStatement.builder().id(id).value(dataType.getName()).type(DecisionTable.StatementType.SET_HEADER_REF).build());
      result.add(ImmutableDecisionStatement.builder().id(id).value(dataType.getScript()).type(DecisionTable.StatementType.SET_HEADER_SCRIPT).build());
      result.add(ImmutableDecisionStatement.builder().id(id).value(dataType.getValueType() == null ? null : dataType.getValueType().name()).type(DecisionTable.StatementType.SET_HEADER_TYPE).build());
      if(dataType.getExtRef() != null) {
        result.add(ImmutableDecisionStatement.builder().id(id).value(dataType.getExtRef()).type(DecisionTable.StatementType.SET_HEADER_EXTERNAL_REF).build());
      }
      if(dataType.getValueSet() != null) {
        result.add(ImmutableDecisionStatement.builder().id(id).value(String.join(", ", dataType.getValueSet())).type(DecisionTable.StatementType.SET_VALUE_SET).build());
      }
      index++;
    }
    return result;
  }
  
  private void createRow(List<Parameter> headers, List<DecisionRowNode> nodes, List<DecisionStatement> result) {  
    
    int rows = 1;
    for(final var node : nodes) {
    
      int nextId = headers.size() * rows + rows;
      result.add(ImmutableDecisionStatement.builder().type(DecisionTable.StatementType.ADD_ROW).build());
  
      Map<String, String> entries = new HashMap<>();
      node.getCells().forEach(e -> entries.put(e.getHeader(), e.getValue()));
  
      for(final var header : headers) {
        String value = entries.get(header.getId());
        result.add(ImmutableDecisionStatement.builder()
            .id(String.valueOf(nextId++))
            .value(value)
            .type(DecisionTable.StatementType.SET_CELL_VALUE)
            .build());
      }
      
      rows++;
    }
  }
}
