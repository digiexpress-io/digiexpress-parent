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

import io.resys.limaone.authoring.ImmutableNewDecisionTableProps;
import io.resys.limaone.authoring.ImmutableNewDecisionTableProps.Builder;
import io.resys.limaone.authoring.NewDecisionTable;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.ImmutableDecisionTable;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.smallrye.mutiny.Uni;


public class NewDecisionTableImpl extends AuthoringTemplate<NewDecisionTableImpl, Model<DecisionTable>> implements NewDecisionTable {

  private NewDecisionTableProps props;

  public NewDecisionTableImpl(AuthoringConfig config) {
    super(config);
  }
  
  @Override
  public NewDecisionTable props(NewDecisionTableProps props) {
    this.props = props;
    return this;
  }

  @Override
  public NewDecisionTable props(Consumer<Builder> props) {
    final var builder = ImmutableNewDecisionTableProps.builder();
    props.accept(builder);
    return props(builder.build());
  }

  @Override
  public Uni<Model<DecisionTable>> build() {
    return config.getPersistence().worldBuilder()
      .docs(BodyType.DECISION_TABLE)
      .createdAt(getCreatedAt())
      .author(getAuthor())
      .build(nextWorld -> {
        final var body = internalBuild(nextWorld.getCurrentWorld());
        return nextWorld.newModel(body.getName(), body, props.getAssetDescription(), props.getAssetLabels());
      });
  }
  
  private DecisionTable internalBuild(ModelWorld world) {
    Objects.requireNonNull(props, () -> "props must be defined");

    final var syntax = """
name: {name}
description: {desc}
hitPolicy: FIRST
valueSets:
  riskLevel: low, medium, high
table: |
  | age:INTEGER    | income: INTEGER | -> | riskLevel:STRING   |
  |----------------|-----------------|----|--------------------|
  | < 25           | < 30000         |    | high               |
  | >= 25          | >= 50000        |    | low                |
"""
    .replace("{name}", Optional.ofNullable(props.getName()).orElse("first_dt"))
    .replace("{desc}", Optional.ofNullable(props.getDesc()).orElse("my dt"));
    
    final var decision = config.getEnvir().getAstParser().parseDecisionTable().syntax(syntax).parse();
    final var nodes = config.getEnvir().getAstParser().parseDecisionTable().syntax(syntax).parseNodes();
    
    
    return ImmutableDecisionTable.builder()
        .name(decision.getName())
        .nodes(nodes)
        .build();
  }
}
