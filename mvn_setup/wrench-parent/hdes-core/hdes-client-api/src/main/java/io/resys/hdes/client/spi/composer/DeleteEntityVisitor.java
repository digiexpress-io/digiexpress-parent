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

import io.resys.hdes.client.api.HdesComposer.ComposerEntity;
import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesStore.DeleteAstType;
import io.resys.hdes.client.api.ImmutableDeleteAstType;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstService;
import io.resys.hdes.client.api.exceptions.ComposerException;

public class DeleteEntityVisitor {

  private final String assetId;
  private final ComposerState state;
  
  public DeleteEntityVisitor(ComposerState state, String assetId) {
    super();
    this.assetId = assetId;
    this.state = state;
  }
  public DeleteAstType visit() {
    final var bodyType = visitBody();
    return ImmutableDeleteAstType.builder().id(assetId).bodyType(bodyType).build();
  }
  
  private AstBodyType visitBody() {
    final var asset = visitId();
    final var bodyType = asset.getSource().getBodyType();
    
    switch (bodyType) {
    case DT: return visitDecision(asset);
    case FLOW: return visitFlow(asset);
    case FLOW_TASK: return visitFlowTask(asset);
    default: throw new ComposerException("Unknown asset of type: '" + bodyType + "'!"); 
    }
  }
  
  private ComposerEntity<?> visitId() {
    if(state.getDecisions().containsKey(assetId)) {
      return state.getDecisions().get(assetId);
      
    } else if(state.getFlows().containsKey(assetId)) {
      return state.getFlows().get(assetId);
      
    } else if(state.getServices().containsKey(assetId)) {
      return state.getServices().get(assetId);
      
    }
    throw new ComposerException("No entity with id: '" + assetId + "'");
  }
  
  private AstBodyType visitFlow(ComposerEntity<?> flow) {
    // find programmatic usage
    final var link2 = state.getServices().values().stream().filter(service -> isUsed(service, flow.getAst().getName())).findFirst();
    if(link2.isPresent()) {
      throw new ComposerException("Can't delete FLOW with id: '" + assetId + "', because it's connected to SERVICE: '" + link2.get().getAst().getName() + "'");
    }
    return flow.getSource().getBodyType();
  }

  private AstBodyType visitFlowTask(ComposerEntity<?> flowTask) {
    final var link = flowTask.getAssociations().stream().filter(a -> !a.getOwner()).findFirst();
    if(link.isPresent()) {
      throw new ComposerException("Can't delete SERVICE with id: '" + assetId + "', because it's connected to: '" + link.get().getRef() + "'");
    }
    
    // find programmatic usage
    final var link2 = state.getServices().values().stream().filter(service -> isUsed(service, flowTask.getAst().getName())).findFirst();
    if(link2.isPresent()) {
      throw new ComposerException("Can't delete SERVICE with id: '" + assetId + "', because it's connected to SERVICE: '" + link2.get().getAst().getName() + "'");
    }
    return flowTask.getSource().getBodyType();
  }
  
  private AstBodyType visitDecision(ComposerEntity<?> decision) {
    final var link = decision.getAssociations().stream().filter(a -> !a.getOwner()).findFirst();
    if(link.isPresent()) {
      throw new ComposerException("Can't delete DECISION with id: '" + assetId + "', because it's connected to: '" + link.get().getRef() + "'");
    }
    
    // find programmatic usage
    final var link2 = state.getServices().values().stream().filter(service -> isUsed(service, decision.getAst().getName())).findFirst();
    if(link2.isPresent()) {
      throw new ComposerException("Can't delete DECISION with id: '" + assetId + "', because it's connected to SERVICE: '" + link2.get().getAst().getName() + "'");
    }
    return decision.getSource().getBodyType();
  }
  
  private boolean isUsed(ComposerEntity<AstService> service, String ref) {
    final var target = new StringBuilder().append("\"").append(ref).append("\"").toString();
    return service.getAst().getValue().contains(target);
  }
}
