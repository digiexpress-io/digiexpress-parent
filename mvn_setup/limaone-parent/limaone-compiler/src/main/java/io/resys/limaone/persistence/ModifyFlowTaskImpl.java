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

import io.resys.limaone.authoring.ImmutableModifyFlowTaskProps;
import io.resys.limaone.authoring.ImmutableModifyFlowTaskProps.Builder;
import io.resys.limaone.authoring.ModifyFlowTask;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableFlowTask;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class ModifyFlowTaskImpl extends AuthoringTemplate<ModifyFlowTaskImpl, Model<FlowTask>> implements ModifyFlowTask {

  private ModifyFlowTaskProps props;

  public ModifyFlowTaskImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyFlowTask props(ModifyFlowTaskProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyFlowTask props(Consumer<Builder> props) {
    final var builder = ImmutableModifyFlowTaskProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<FlowTask>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.FLOW_TASK)
      .build(nextWorld -> {
        final ModelWorld world = nextWorld.getCurrentWorld();
        final var body = internalBuild(world);
        return nextWorld.mergeModel(props.getFlowTaskId(), body.getTaskName(), body, props.getAssetDescription());
      });
  }
  
  @Override
  public Uni<ModelWorld> buildTransientWorld() {
    return config.getPersistence().worldQuery()
      .docs(BodyType.DECISION_TABLE, BodyType.FLOW, BodyType.FLOW_TASK)
      .findAll()
      .onItem().transform(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.withAny(props.getFlowTaskId(), body);
      });
  }
  
  private FlowTask internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");
    
    final var start = world.getFlowTasks().get(props.getFlowTaskId());
    if(start == null) {
      throw new AuthoringException(props, "Flow task with id: '" + props.getFlowTaskId() + "' not found!");
    }
    
    final var flow = config.getEnvir().getAstParser().parseFlowTask().syntax(props.getFlowTaskValue()).parse();
    
    // Check for duplicate name only if the name is actually being changed
    if(!start.getBody().getTaskName().equals(flow.getName())) {
      final var duplicate = world.getFlowTasks().values().stream()
          .filter(p -> !p.getId().equals(props.getFlowTaskId()))
          .filter(p -> p.getBody().getTaskName().equalsIgnoreCase(flow.getName()))
          .findFirst();

      if(duplicate.isPresent()) {
        throw new AuthoringException(props, "Flow task with name: '" + flow.getName() + "' already exists!");
      }
    }

    return ImmutableFlowTask.builder()
      .from(start.getBody())
      .taskName(flow.getName())
      .taskValue(props.getFlowTaskValue())
      .build();
  }
}
