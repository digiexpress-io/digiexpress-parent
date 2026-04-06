package io.digiexpress.eveli.client.web.resources.assets;

/*-
 * #%L
 * eveli-client
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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.ast.FlowTask_AST;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.ast.Simple_AST;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.authoring.DebugAny.DebugAnyProps;
import io.resys.limaone.authoring.DebugAny.DebugResult;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Model.ModelWorldDiff;
import io.resys.limaone.model.Model.ModelWorldIndex;
import io.resys.limaone.model.Model.ModelWorldSummary;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.program.Compiler;
import io.resys.limaone.program.Program.ProgramAssociation;
import io.resys.limaone.program.Program.ProgramStatus;
import io.resys.thena.support.TableUtils;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/worker/rest/api/assets/wrench")
@RequiredArgsConstructor
public class AssetsWrenchController {
  private final Authoring composer;
  private final Compiler compiler;
  
  private Authoring getComposer(String branchName) {
    return composer.withBranchName(Optional.ofNullable(branchName));
  } 
  
  private Uni<ComposerState> getComposerState(String branchName) {
    return getComposer(branchName)
        .withBranchName(Optional.ofNullable(branchName))
        .worldQuery()
        .docs(BodyType.FLOW, BodyType.FLOW_TASK, BodyType.DECISION_TABLE)
        .findAll()
        .onItem().transformToUni(world -> getComposerState(branchName, world));
  }
  
  private Uni<ComposerState> getComposerState(String branchName, ModelWorld world) {
    final var state = ImmutableComposerState.builder();
    final var bundle = compiler.compile(world).build().getBundle();
    
    bundle.queryFlows().forEach(program -> {
      state.putFlows(program.getId(), ImmutableComposerEntity.<Flow_AST>builder()
          .ast(program.getAst())
          .errors(program.getErrors())
          .associations(program.getAssociations())
          .id(program.getId())
          .status(program.getStatus())
          .build());
    });
    
    bundle.queryFlowTasks().forEach(program -> {
      state.putServices(program.getId(), ImmutableComposerEntity.<FlowTask_AST>builder()
          .ast(program.getAst())
          .errors(program.getErrors())
          .associations(program.getAssociations())
          .id(program.getId())
          .status(program.getStatus())
          .build());
    });
    
    
    bundle.queryDecisions().forEach(program -> {
      state.putDecisions(program.getId(), ImmutableComposerEntity.<DecisionTable_AST>builder()
          .ast(program.getAst())
          .errors(program.getErrors())
          .associations(program.getAssociations())
          .id(program.getId())
          .status(program.getStatus())
          .commands(world.getDecisionTables().get(program.getId()).getBody().getNodes())
          .build());
    });
    
    return Uni.createFrom().item(state.build());
  }
  
  @PostMapping(path = "/commands", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerEntity<?>> commands(
      @RequestBody UpdateEntity entity, 
      @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    
    if(entity.getBodyType() == BodyType.FLOW) {
      return getComposer(branchName)
        .modifyModel().modifyFlow().props(props -> props.flowId(entity.getId()).flowValue(entity.getBodySyntax())).buildTransientWorld()
        .onItem().transformToUni(world -> getComposerState(branchName, world))
        .map(world -> world.getEntity(entity.getId()));
    } else if(entity.getBodyType() == BodyType.FLOW_TASK) {
      return getComposer(branchName)
        .modifyModel().modifyFlowTask().props(props -> props.flowTaskId(entity.getId()).flowTaskValue(entity.getBodySyntax())).buildTransientWorld()
        .onItem().transformToUni(world -> getComposerState(branchName, world))
        .map(world -> world.getEntity(entity.getId()));
    }    
    return getComposer(branchName)
        .modifyModel().modifyDecisionTable().props(props -> props.decisionTableId(entity.getId()).nodes(entity.getBodyStatment())).buildTransientWorld()
        .onItem().transformToUni(world -> getComposerState(branchName, world))
        .map(world -> world.getEntity(entity.getId()));
  }

  @GetMapping(path = "/defaultAssets", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> defaultAssets() {
    
    return getComposer(null).defaultModel().buildDefaultModel()
        .onItem().transformToUni(ignore -> getComposerState(null));
  }
    
  @GetMapping(path = "/commitlogs", produces = MediaType.APPLICATION_JSON_VALUE)
  public Multi<ModelWorldIndex> commitlogs() {
    return getComposer(null).worldIndexQuery().findAll();
  }

  @GetMapping(path = "/dataModels", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> dataModels(@RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return getComposerState(branchName);
  }

  @PostMapping(path = "/debugs", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<DebugResult> debug(
      @RequestBody DebugAnyProps debug, 
      @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    
    return getComposer(branchName).debugModel().debugAny().props(debug).build();
  }

  @PostMapping(path = "/resources", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> create(
      @RequestBody CreateEntity entity, 
      @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    
    if(entity.getBodyType() == BodyType.FLOW) {
      return getComposer(branchName)
        .newModel().newFlow().props(props -> props.name(entity.getName()).desc(entity.getDesc())).build()
        .onItem().transformToUni(ignore -> getComposerState(branchName));
      
    } else if(entity.getBodyType() == BodyType.FLOW_TASK) {
      return getComposer(branchName)
          .newModel().newFlowTask().props(props -> props.name(entity.getName()).desc(entity.getDesc())).build()
          .onItem().transformToUni(ignore -> getComposerState(branchName));
    }
    
    return getComposer(branchName)
        .newModel().newDecisionTable().props(props -> props.name(entity.getName()).desc(entity.getDesc()).nodes(entity.getBodyStatment())).build()
        .onItem().transformToUni(ignore -> getComposerState(branchName));
  }

  @PutMapping(path = "/resources", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> update(
      @RequestBody UpdateEntity entity, 
      @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    
    if(entity.getBodyType() == BodyType.FLOW) {
      return getComposer(branchName)
        .modifyModel().modifyFlow().props(props -> props.flowId(entity.getId()).flowValue(entity.getBodySyntax())).build()
        .onItem().transformToUni(ignore -> getComposerState(branchName));
    } else if(entity.getBodyType() == BodyType.FLOW_TASK) {
      return getComposer(branchName)
          .modifyModel().modifyFlowTask().props(props -> props.flowTaskId(entity.getId()).flowTaskValue(entity.getBodySyntax())).build()
          .onItem().transformToUni(ignore -> getComposerState(branchName));
    }
    
    return getComposer(branchName)
        .modifyModel().modifyDecisionTable().props(props -> props.decisionTableId(entity.getId()).nodes(entity.getBodyStatment())).build()
        .onItem().transformToUni(ignore -> getComposerState(branchName));
  }

  @DeleteMapping(path = "/resources/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> delete(@PathVariable String id, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return getComposer(branchName).deleteModel().deleteAny().props(props -> props.id(id)).build()
        .onItem().transformToUni(ignore -> getComposerState(branchName));
  }

  @GetMapping(path = "/resources/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerEntity<?>> get(@PathVariable String id, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return getComposerState(branchName).onItem().transform(world -> {
      if(world.getDecisions().containsKey(id)) {
        return world.getDecisions().get(id); 
      } else if(world.getFlows().containsKey(id)) {
        return world.getFlows().get(id); 
      }
      return world.getServices().get(id);
    });
  }

  @PostMapping(path = "/copyas", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ComposerState> copyAs(@RequestBody CopyAs entity, @RequestHeader(value = "Branch-Name", required = false) String branchName) {
    return getComposer(branchName).copyAsModel().copyAny()
        .props(props -> props.idOfObjectToCopy(entity.getId()).newObjectName(entity.getName()))
        .build().onItem().transformToUni(ignore -> getComposerState(branchName));
  }

  @GetMapping(path = "/diff", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ModelWorldDiff> diff(@RequestParam("baseId") String baseId, @RequestParam("targetId") String targetId) {
    return getComposer(null).worldDiffQuery().baseId(TableUtils.toUuid(baseId)).targetId(TableUtils.toUuid(targetId)).findAll();
  }

  @GetMapping(path = "/summary/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Uni<ModelWorldSummary> summary(@PathVariable("tagId") String tagId) {
    return getComposer(null).worldSummaryQuery().tagId(tagId).findAll();
  }

  @GetMapping(path="/flow-names")
  public Uni<List<String>> flowNames() {
    return getComposer(null).worldQuery()
      .docs(BodyType.FLOW).findAll()
      .onItem().transform(world -> world.getFlows().values().stream()
          .map(e -> e.getBody().getFlowName()).toList());
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableUpdateEntity.class)
  @JsonDeserialize(as = ImmutableUpdateEntity.class)
  public interface UpdateEntity {
    String getId();
    Model.BodyType getBodyType();
    @Nullable String getBodySyntax();
    List<DecisionStatement> getBodyStatment();
  }
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableCreateEntity.class)
  @JsonDeserialize(as = ImmutableCreateEntity.class)
  public interface CreateEntity {
    @Nullable String getName();
    @Nullable String getDesc();
    Model.BodyType getBodyType();
    List<DecisionStatement> getBodyStatment();
  }

  @JsonSerialize(as = ImmutableCopyAs.class)
  @JsonDeserialize(as = ImmutableCopyAs.class)
  @Value.Immutable
  public interface CopyAs extends Serializable {
    String getId(); // id of the entity
    String getName(); // new name of the entity
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerState.class)
  @JsonDeserialize(as = ImmutableComposerState.class)
  public interface ComposerState {
    Map<String, ComposerEntity<Flow_AST>> getFlows();
    Map<String, ComposerEntity<FlowTask_AST>> getServices();
    Map<String, ComposerEntity<DecisionTable_AST>> getDecisions();
    
    default ComposerEntity<?> getEntity(String id) {
      if(this.getDecisions().containsKey(id)) {
        return this.getDecisions().get(id); 
      } else if(this.getFlows().containsKey(id)) {
        return this.getFlows().get(id); 
      }
      return this.getServices().get(id);
    }

  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableComposerEntity.class)
  @JsonDeserialize(as = ImmutableComposerEntity.class)
  public interface ComposerEntity<A extends Simple_AST> {
    String getId();
    @Nullable A getAst();
    List<DecisionStatement> getCommands();
    List<ModelError> getErrors();
    List<ProgramAssociation> getAssociations();
    ProgramStatus getStatus();
  }

}
