package io.resys.limaone.persistence.world;

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

import io.resys.limaone.authoring.Authoring.WorldSummaryQuery;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.ImmutableModelWorldSummary;
import io.resys.limaone.model.ImmutableModelWorldSummaryItem;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Model.ModelWorldSummary;
import io.resys.limaone.model.Model.ModelWorldSummaryItem;
import io.resys.limaone.persistence.AuthoringImpl.AuthoringConfig;
import io.resys.limaone.spi.program.DecisionProgramPrettyEncoder;
import io.resys.thena.support.TableUtils;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorldSummaryQueryImpl implements WorldSummaryQuery {
  private final AuthoringConfig config;
  
  private String tagId;
  
  @Override
  public WorldSummaryQuery tagId(String tagId) {
    this.tagId = Objects.requireNonNull(tagId, () -> "tagId must be defined");
    return this;
  }

  @Override
  public Uni<ModelWorldSummary> findAll() {
    Objects.requireNonNull(tagId, () -> "tagId must be defined");
    
    return config.getPersistence().worldQuery()
      .docs(BodyType.DEPLOYMENT)
      .findAll()
      .onItem().transform(e -> e.getDeployments().values().stream()
          .filter(this::isMatch)
          .findFirst().orElseThrow(() -> new WorldSummaryQueryException("Can't find tag by id: '" + tagId + "'")))
      .onItem().transformToUni(model -> config.getPersistence().worldQuery().commitId(model.getBody().getFromCommitId()).findAll())
      .onItem().transform(this::build);
  }
  @Override
  public ModelWorldSummary findAllSync() {
    return findAll()
        .runSubscriptionOn(config.getEnvir().getWorkerPool())
        .await().atMost(config.getEnvir().getWorkerPoolMaxTimeout());
  }

  
  private boolean isMatch(Model<Deployment> dep) {
    if(dep.getId().equals(tagId)) {
      return true;
    }
    
    if(dep.getBody().getName().equals(tagId)) {
      return true;
    }
    try {
      return dep.getBody().getFromCommitId().equals(TableUtils.toUuid(tagId));  
    } catch(Exception e) {
      return false;
    }
  }
  
  
  private static class WorldSummaryQueryException extends RuntimeException {

    private static final long serialVersionUID = 7949103397916586300L;

    public WorldSummaryQueryException(String message) {
      super(message);
    }
  }
  
  public ModelWorldSummary build(ModelWorld world) {
    final var flows = world.getFlows().values().stream().map(this::fromCommands).toList();
    final var decisions = world.getDecisionTables().values().stream().map(this::fromCommands).toList();
    final var services = world.getFlowTasks().values().stream().map(this::fromCommands).toList();

    return ImmutableModelWorldSummary.builder()
        .tagName(world.getName())
        .flows(flows)
        .decisions(decisions)
        .services(services)
        .build();
  }

  private ModelWorldSummaryItem fromCommands(Model<?> model) {
    return ImmutableModelWorldSummaryItem.builder()
        .id(model.getId())
        .name(getAssetName(model))
        .body(getAssetBody(model))
        .build();
  }

  private String getAssetBody(Model<?> value) {
    switch (value.getBodyType()) {
      case FLOW: return "flows/" + ((Flow) value.getBody()).getFlowValue();
      case FLOW_TASK: return "services/" + ((FlowTask) value.getBody()).getTaskValue();
      case DECISION_TABLE: {
        
        final DecisionTable table = (DecisionTable) value.getBody();
        final var ast = config.getEnvir().getAstParser().parseDecisionTable().nodes(table.getNodes()).parse();
        final var pretty = DecisionProgramPrettyEncoder.encodePrettily(ast);
        return "decisions/" + pretty;
      }
      default: throw new IllegalArgumentException("unknown model: " + value.getBodyType()); 
    }
  }

  private String getAssetName(Model<?> value) {
    switch (value.getBodyType()) {
      case FLOW: return "flows/" + ((Flow) value.getBody()).getFlowName();
      case FLOW_TASK: return "services/" + ((FlowTask) value.getBody()).getTaskName();
      case DECISION_TABLE: return "decisions/" + ((DecisionTable) value.getBody()).getName();
      default: throw new IllegalArgumentException("unknown model: " + value.getBodyType()); 
    }
  }
}
