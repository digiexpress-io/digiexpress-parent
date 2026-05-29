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

import io.resys.limaone.authoring.ImmutableModifyFlowProps;
import io.resys.limaone.authoring.ImmutableModifyFlowProps.Builder;
import io.resys.limaone.authoring.ModifyFlow;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.ImmutableFlow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class ModifyFlowImpl extends AuthoringTemplate<ModifyFlowImpl, Model<Flow>> implements ModifyFlow {

  private ModifyFlowProps props;

  public ModifyFlowImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public ModifyFlow props(ModifyFlowProps props) {
    this.props = props;
    return this;
  }

  @Override
  public ModifyFlow props(Consumer<Builder> props) {
    final var builder = ImmutableModifyFlowProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Flow>> build() {
    return config.getPersistence().worldBuilder()
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .docs(BodyType.FLOW)
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.mergeModel(props.getFlowId(), body.getFlowName(), body);
      });
  }
  
  @Override
  public Uni<ModelWorld> buildTransientWorld() {
    return config.getPersistence().worldQuery()
      .docs(BodyType.DECISION_TABLE, BodyType.FLOW, BodyType.FLOW_TASK)
      .findAll()
      .onItem().transform(nextWorld -> {
        final var body = internalBuild(nextWorld);
        return nextWorld.withAny(props.getFlowId(), body);
      });
  }
  
  
  private Flow internalBuild(final ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");
    
    final var start = world.getFlows().get(props.getFlowId());
    if(start == null) {
      throw new AuthoringException(props, "Flow with id: '" + props.getFlowId() + "' not found!");
    }
    
    final var flow = config.getEnvir().getAstParser().parseFlow().syntax(props.getFlowValue()).parse();

    // Check for duplicate name only if the name is actually being changed
    if(!start.getBody().getFlowName().equals(flow.getName())) {
      final var duplicate = world.getFlows().values().stream()
          .filter(p -> !p.getId().equals(props.getFlowId()))
          .filter(p -> p.getBody().getFlowName().equalsIgnoreCase(flow.getName()))
          .findFirst();

      if(duplicate.isPresent()) {
        throw new AuthoringException(props, "Flow with name: '" + flow.getName() + "' already exists!");
      }
    }

    return ImmutableFlow.builder()
      .from(start.getBody())
      .flowName(flow.getName())
      .flowValue(props.getFlowValue())
      .description(props.getDescription())
      .tagLabels(props.getTagLabels())
      .devMode(props.getDevMode())
      .disabledMode(props.getDisabledMode())
      .build();
  }
}
