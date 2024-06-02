package io.resys.hdes.client.spi.composer;

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
import java.util.Arrays;
import java.util.List;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer.ComposerEntity;
import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesComposer.CopyAs;
import io.resys.hdes.client.api.HdesStore.CreateStoreEntity;
import io.resys.hdes.client.api.ImmutableCreateStoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.exceptions.ComposerException;
import io.resys.hdes.client.spi.changeset.AstCommandOptimiser;

public class CopyAsEntityVisitor {

  private final CopyAs asset;
  private final ComposerState state;
  private final AstCommandOptimiser optimise;
  
  public CopyAsEntityVisitor(ComposerState state, CopyAs asset, HdesClient client) {
    super();
    this.asset = asset;
    this.state = state;
    this.optimise = new AstCommandOptimiser(client);
  }
  public CreateStoreEntity visit() {
    visitValidations();

    final var original = getOriginal(asset);
    if(original.getAst() == null) {
      throw new ComposerException("Can't copy broken entity: '" + asset.getId() + "'!");
    }
    
    final var body = visitBody(original);
    
    return ImmutableCreateStoreEntity.builder()
        .bodyType(original.getSource().getBodyType())
        .body(body)
        .build();
  }

  private ComposerEntity<?> getOriginal(CopyAs entity) {
    if(state.getDecisions().containsKey(entity.getId())) {
      return state.getDecisions().get(entity.getId());
    } else if(state.getServices().containsKey(entity.getId())) {
      return state.getServices().get(entity.getId());
    } else if(state.getFlows().containsKey(entity.getId())) {
      return state.getFlows().get(entity.getId());
    } else {
      throw new ComposerException("Unknown entity: '" + entity.getId() + "'!");
    }
  }
  
  private List<AstCommand> visitBody(ComposerEntity<?> original) {
    switch (original.getSource().getBodyType()) {
    case DT: return initDecision(original);
    case FLOW: return initFlow(original);
    case FLOW_TASK: return initFlowTask(original);
    default: throw new ComposerException("Unknown asset: '" + original.getSource().getBodyType() + "'!"); 
    }
  }
  
  private void visitValidations() {
    
    // DT validations
    final var decision = state.getDecisions().values().stream()
      .filter(e -> e.getAst() != null)
      .filter(e -> e.getAst().getName().equals(asset.getName()))
      .findFirst();
    if(decision.isPresent()) {
      throw new ComposerException(decision.get().getSource().getBodyType() + " asset with name: '" + asset.getName() + "' exists already!");
    }
    
    // Flow validations
    final var flow = state.getFlows().values().stream()
        .filter(e -> e.getAst() != null)
        .filter(e -> e.getAst().getName().equals(asset.getName()))
        .findFirst();
    if(flow.isPresent()) {
      throw new ComposerException(flow.get().getSource().getBodyType() + " asset with name: '" + asset.getName() + "' exists already!");
    }
    
    // Flow task validations
    final var flowtask = state.getServices().values().stream()
        .filter(e -> e.getAst() != null)
        .filter(e -> e.getAst().getName().equals(asset.getName()))
        .findFirst();
    if(flowtask.isPresent()) {
      throw new ComposerException(flow.get().getSource().getBodyType() + " asset with name: '" + asset.getName() + "' exists already!");
    }
  }
  

  public List<AstCommand> initFlow(ComposerEntity<?> original) {
    final var commands = new ArrayList<>(original.getSource().getCommands());
    final var finalCommand = optimise.optimise(commands, AstBodyType.FLOW).get(0);
    
    final var body = finalCommand.getValue();
    final var oldName = original.getAst().getName();
    
    final var classIndex = body.indexOf("class");
    final var nameIndex = body.indexOf(oldName, classIndex);
    final var start = body.substring(0, nameIndex);
    final var end = body.substring(nameIndex + oldName.length());
    return Arrays.asList(ImmutableAstCommand.builder().type(AstCommandValue.SET_BODY).value(start + asset.getName() + end).build());
  }

  
  public List<AstCommand> initFlowTask(ComposerEntity<?> original) {
    final var commands = new ArrayList<>(original.getSource().getCommands());
    final var finalCommand = optimise.optimise(commands, AstBodyType.FLOW_TASK).get(0);
    
    final var body = finalCommand.getValue();
    final var oldName = original.getAst().getName();
    
    final var classIndex = body.indexOf("id:");
    final var nameIndex = body.indexOf(oldName, classIndex);
    final var start = body.substring(0, nameIndex);
    final var end = body.substring(nameIndex + oldName.length());
    return Arrays.asList(ImmutableAstCommand.builder().type(AstCommandValue.SET_BODY).value(start + asset.getName() + end).build());
  }

  private List<AstCommand> initDecision(ComposerEntity<?> original) {
    List<AstCommand> commands = new ArrayList<>(original.getSource().getCommands());
    commands.add(ImmutableAstCommand.builder().value(asset.getName()).type(AstCommandValue.SET_NAME).build());
    return optimise.optimise(commands, AstBodyType.DT);
  }
}
