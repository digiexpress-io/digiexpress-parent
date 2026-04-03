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
import java.util.List;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.HdesStore.ImportStoreEntity;
import io.resys.hdes.client.api.ImmutableCreateStoreEntity;
import io.resys.hdes.client.api.ImmutableImportStoreEntity;
import io.resys.hdes.client.api.ImmutableUpdateStoreEntityWithBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTag.AstTagValue;
import io.resys.hdes.client.api.exceptions.ComposerException;

public class ImportEntityVisitor {

  private final HdesClient client;
  private final AstTag asset;
  private final ComposerState state;
  private final List<HdesStore.CreateStoreEntity> create = new ArrayList<>();
  private final List<HdesStore.UpdateStoreEntityWithBodyType> update = new ArrayList<>();


  public ImportEntityVisitor(ComposerState state, AstTag asset, HdesClient client) {
    super();
    this.asset = asset;
    this.state = state;
    this.client = client;
  }

  
  public ImportStoreEntity visit() {
    for(final var entry : asset.getValues()) {
      visitBody(entry);
    }
    return ImmutableImportStoreEntity.builder()
        .create(create)
        .update(update)
        .build();
  }
  
  private List<AstCommand> visitBody(AstTagValue original) {
    switch (original.getBodyType()) {
    case DT: return initDecision(original);
    case FLOW: return initFlow(original);
    case FLOW_TASK: return initFlowTask(original);
    default: throw new ComposerException("Unknown asset: '" + original.getBodyType() + "'!"); 
    }
  }

  public List<AstCommand> initFlow(AstTagValue original) {
    final var flow = client.ast().commands(original.getCommands()).flow();
    final var old = state.getFlows().values().stream()
        .filter(v -> v.getAst() != null && v.getAst().getName().equals(flow.getName()))
        .findFirst();
    
    if(old.isPresent()) {
      update.add(ImmutableUpdateStoreEntityWithBodyType.builder()
          .id(old.get().getId())
          .bodyType(old.get().getSource().getBodyType())
          .body(original.getCommands())
          .build());
    } else {
      create.add(ImmutableCreateStoreEntity.builder()
          .bodyType(original.getBodyType())
          .body(original.getCommands())
          .build());
    }
    return original.getCommands();
  }

  
  public List<AstCommand> initFlowTask(AstTagValue original) {
    final var service = client.ast().commands(original.getCommands()).service();
    final var old = state.getServices().values().stream()
        .filter(v -> v.getAst() != null && v.getAst().getName().equals(service.getName()))
        .findFirst();
    
    if(old.isPresent()) {
      update.add(ImmutableUpdateStoreEntityWithBodyType.builder()
          .id(old.get().getId())
          .bodyType(old.get().getSource().getBodyType())
          .body(original.getCommands())
          .build());
    } else {
      create.add(ImmutableCreateStoreEntity.builder()
          .bodyType(original.getBodyType())
          .body(original.getCommands())
          .build());
    }    
    return original.getCommands();
  }

  private List<AstCommand> initDecision(AstTagValue original) {
    final var decision = client.ast().commands(original.getCommands()).decision();
    final var old = state.getDecisions().values().stream()
        .filter(v -> v.getAst() != null && v.getAst().getName().equals(decision.getName()))
        .findFirst();
    
    if(old.isPresent()) {
      update.add(ImmutableUpdateStoreEntityWithBodyType.builder()
          .id(old.get().getId())
          .bodyType(old.get().getSource().getBodyType())
          .body(original.getCommands())
          .build());
    } else {
      create.add(ImmutableCreateStoreEntity.builder()
          .bodyType(original.getBodyType())
          .body(original.getCommands())
          .build());
    }
    return original.getCommands();
  }
}
