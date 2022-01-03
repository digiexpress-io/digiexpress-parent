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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesComposer.CreateEntity;
import io.resys.hdes.client.api.HdesStore.CreateStoreEntity;
import io.resys.hdes.client.api.ImmutableCreateStoreEntity;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.AstDecision;
import io.resys.hdes.client.api.ast.AstFlow;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.api.exceptions.ComposerException;
import io.resys.hdes.client.api.programs.DecisionProgram;
import io.resys.hdes.client.api.programs.DecisionProgram.DecisionLog;
import io.resys.hdes.client.api.programs.DecisionProgram.DecisionResult;
import io.resys.hdes.client.api.programs.FlowProgram;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResult;
import io.resys.hdes.client.api.programs.FlowProgram.FlowResultLog;
import io.resys.hdes.client.api.programs.Program.ProgramContext;
import io.resys.hdes.client.api.programs.ServiceData;
import io.resys.hdes.client.api.programs.ServiceProgram;
import io.resys.hdes.client.api.programs.ServiceProgram.ServiceResult;

public class CreateEntityVisitor {

  private final CreateEntity asset;
  private final ComposerState state;
  
  public CreateEntityVisitor(ComposerState state, CreateEntity asset) {
    super();
    this.asset = asset;
    this.state = state;
  }
  public CreateStoreEntity visit() {
    if(asset.getName() == null && asset.getBody().isEmpty()) {
      throw new ComposerException("Name can't be null if body is empty!");
    }
    visitValidations();
    final var body = visitBody();
    return ImmutableCreateStoreEntity.builder()
        .bodyType(asset.getType())
        .body(body)
        .build();
  }
  
  private List<AstCommand> visitBody() {
    switch (asset.getType()) {
    case DT: return initDecision(asset);
    case FLOW: return initFlow(asset);
    case FLOW_TASK: return initFlowTask(asset);
    case TAG: return initTag(asset);
    default: throw new ComposerException("Unknown asset: '" + asset.getType() + "'!"); 
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
    
    // Tag validations
    final var tag = state.getTags().values().stream()
        .filter(e -> e.getAst() != null)
        .filter(e -> e.getAst().getName().equals(asset.getName()))
        .findFirst();
    if(tag.isPresent()) {
      throw new ComposerException(flow.get().getSource().getBodyType() + " asset with name: '" + asset.getName() + "' exists already!");
    }
  }
  

  public List<AstCommand> initFlow(CreateEntity entity) {
    if(!entity.getBody().isEmpty()) {
      return entity.getBody();
    }
    
    final var lnr = System.lineSeparator();
    final var body = new StringBuilder()
        .append("id: ").append(entity.getName()).append(lnr)
        .toString();
    
    return Arrays.asList(ImmutableAstCommand.builder().type(AstCommandValue.SET_BODY).value(body).build());
  }

  public List<AstCommand> initTag(CreateEntity entity) {
    return Collections.emptyList();
  }
  
  public List<AstCommand> initFlowTask(CreateEntity entity) {
    if(!entity.getBody().isEmpty()) {
      return entity.getBody();
    }
    
    final var lnr = System.lineSeparator();
    final var body = new StringBuilder()
        .append("package io.resys.wrench.assets.bundle.groovy;").append(lnr)
        .append("import java.io.Serializable;").append(lnr)
        
        .append("import ").append(DecisionProgram.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(FlowProgram.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(ServiceProgram.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(AstFlow.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(AstDecision.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(AstService.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(DecisionResult.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(DecisionLog.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(FlowResult.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(FlowResultLog.class.getCanonicalName()).append(";").append(lnr)
        
        .append("import ").append(ServiceResult.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(ProgramContext.class.getCanonicalName()).append(";").append(lnr)
        .append("import ").append(ServiceData.class.getCanonicalName()).append(";").append(lnr)
        
        .append("public class ").append(entity.getName()).append(" {").append(lnr)
        .append("  public Output execute(Input input, ProgramContext ctx) {").append(lnr)
        .append("    return new Output();").append(lnr)
        .append("  }").append(lnr)

        .append("  @").append(ServiceData.class.getSimpleName()).append(lnr)
        .append("  public static class Input implements Serializable {").append(lnr)
        .append("  }").append(lnr)
        .append("  @").append(ServiceData.class.getSimpleName()).append(lnr)
        .append("  public static class Output implements Serializable {").append(lnr)
        .append("  }").append(lnr)
        .append("}").append(lnr)
        .toString();
    
    return Arrays.asList(ImmutableAstCommand.builder().type(AstCommandValue.SET_BODY).value(body).build());
  }

  private List<AstCommand> initDecision(CreateEntity entity) {
    if(!entity.getBody().isEmpty()) {
      return entity.getBody();
    }
    
    return Arrays.asList(
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_IN).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("0").value("inputColumn").build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("0").value("STRING").build(),
      ImmutableAstCommand.builder().type(AstCommandValue.ADD_HEADER_OUT).build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_REF).id("1").value("outputColumn").build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_HEADER_TYPE).id("1").value("STRING").build(),
      ImmutableAstCommand.builder().type(AstCommandValue.SET_NAME).value(entity.getName()).build()
    );
  }
}
