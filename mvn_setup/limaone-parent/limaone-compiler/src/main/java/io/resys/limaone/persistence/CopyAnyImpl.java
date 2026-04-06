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

import java.util.Objects;
import java.util.function.Consumer;

import io.resys.limaone.authoring.CopyAny;
import io.resys.limaone.authoring.ImmutableCopyAnyProps;
import io.resys.limaone.authoring.ImmutableCopyAnyProps.Builder;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.DecisionTable.StatementType;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableDecisionStatement;
import io.resys.limaone.model.ImmutableDecisionTable;
import io.resys.limaone.model.ImmutableFlow;
import io.resys.limaone.model.ImmutableFlowTask;
import io.resys.limaone.model.Model;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.persistence.ModelWorldDb.NextWorld;
import io.smallrye.mutiny.Uni;

public class CopyAnyImpl extends AuthoringTemplate<CopyAnyImpl, Model<?>> implements CopyAny {
  
  private CopyAnyProps props;

  public CopyAnyImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public CopyAny props(CopyAnyProps props) {
    this.props = Objects.requireNonNull(props, () -> "props must be defined");
    return this;
  }
  @Override
  public CopyAny props(Consumer<Builder> props) {
    Objects.requireNonNull(props, () -> "props must be defined");
    final var builder = ImmutableCopyAnyProps.builder();
    props.accept(builder);
    return props(builder.build());
  }
  @Override
  public Uni<Model<?>> build() {
    Objects.requireNonNull(props, () -> "props must be defined");
  
    return config.getPersistence().worldBuilder()
        .createdAt(getCreatedAt())
        .author(getAuthor())
        .docsId(props.getIdOfObjectToCopy())
        .build(nextWorld -> {
          final var body = internalBuild(nextWorld);
          return nextWorld.newModel(props.getNewObjectName(), body);
        });
  }
  
  private Model.Body internalBuild(NextWorld nextWorld) {
    
    final var src = nextWorld.getCurrentWorld()
      .findAnyObject(props.getIdOfObjectToCopy());
    
    if(src.isEmpty()) {
      throw new CopyAsNotSupportedForObject("Copy object can't be found!");  
    }
    switch (src.get().getBodyType()) {
      case FLOW: return copyFlow(src.get(), nextWorld);
      case FLOW_TASK: return copyFlowTask(src.get(), nextWorld);
      case DECISION_TABLE: return copyDecisionTable(src.get(), nextWorld);
      default: throw new CopyAsNotSupportedForObject("Copy as not support for object: " + src.get().getBodyType());
    }
  }
  
  private Model.Body copyFlow(Model<?> src, NextWorld nextWorld) {
    final Flow flow = (Flow) src.getBody();
    
    
    return ImmutableFlow.builder()
      .flowName(props.getNewObjectName())
      .flowValue(flow.getFlowValue().replace(flow.getFlowName(), props.getNewObjectName()))
      .build();
  }
  
  private Model.Body copyFlowTask(Model<?> src, NextWorld nextWorld) {
    final FlowTask flowTask = (FlowTask) src.getBody();
    
    return ImmutableFlowTask.builder()
      .taskName(props.getNewObjectName())
      .taskValue(flowTask.getTaskValue().replace(flowTask.getTaskName(), props.getNewObjectName()))
      .build();
  }
  
  private Model.Body copyDecisionTable(Model<?> src, NextWorld nextWorld) {
    final DecisionTable decisionTable = (DecisionTable) src.getBody();
    final var next = ImmutableDecisionTable.builder().name(props.getNewObjectName());
    for(final var node : decisionTable.getNodes()) {
      if(node.getType() == StatementType.SET_NAME) {
        next.addNodes(ImmutableDecisionStatement.builder()
            .from(node)
            .value(props.getNewObjectName())
            .build());
      } else {
        next.addNodes(node);        
      }
    }
    return next.build();
  }
  public static class CopyAsNotSupportedForObject extends RuntimeException {
    private static final long serialVersionUID = -1398646745215966745L;

    public CopyAsNotSupportedForObject(String message) {
      super(message);
    }
  }
}
