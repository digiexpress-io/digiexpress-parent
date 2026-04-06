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
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.limaone.authoring.ImmutableNewFlowProps;
import io.resys.limaone.authoring.ImmutableNewFlowProps.Builder;
import io.resys.limaone.authoring.NewFlow;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.ImmutableFlow;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class NewFlowImpl extends AuthoringTemplate<NewFlowImpl, Model<Flow>> implements NewFlow {

  private NewFlowProps props;

  public NewFlowImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public NewFlow props(NewFlowProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewFlow props(Consumer<Builder> props) {
    final var builder = ImmutableNewFlowProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<Flow>> build() {
    return config.getPersistence().worldBuilder()
      .docs(BodyType.FLOW)
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getFlowName(), body);
      });
  }
  
  private Flow internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final String syntax;
    if(props.getBody() == null) {
      syntax = """
id: {id}
description: {desc}

inputs:
  id:
    required: true
    type: STRING
    debugValue: "1"
tasks:
  - First task:
    id: "first_task"
    then: end
"""
    .replace("{id}", Optional.ofNullable(props.getName()).orElse("first_flow"))
    .replace("{desc}", Optional.ofNullable(props.getDesc()).orElse("my first flow"));
    } else {
      syntax = props.getBody();
    }
    
    // Check for duplicate name only if the name is actually being changed
    final var flow = config.getEnvir().getAstParser().parseFlow().syntax(syntax).parse();    
    final var duplicate = world.getFlows().values().stream()
        .filter(p -> p.getBody().getFlowName().equalsIgnoreCase(flow.getName()))
        .findFirst();

    if(duplicate.isPresent()) {
      throw new AuthoringException(props, "Flow with name: '" + flow.getName() + "' already exists!");
    }
    return ImmutableFlow.builder()
        .flowName(flow.getName())
        .flowValue(syntax)
        .build();
  }
}
