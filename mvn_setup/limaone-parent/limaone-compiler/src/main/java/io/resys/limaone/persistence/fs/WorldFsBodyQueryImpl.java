package io.resys.limaone.persistence.fs;

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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.ast.FlowTask_AST;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.authoring.Authoring;
import io.resys.limaone.authoring.Authoring.WorldFsBodyQuery;
import io.resys.limaone.fs.ImmutableArticlePageBody;
import io.resys.limaone.fs.ImmutableWrenchAstBody;
import io.resys.limaone.fs.ImmutableWrenchBody;
import io.resys.limaone.fs.WorldFsBody;
import io.resys.limaone.fs.WorldFsBody.WrenchAstBody;
import io.resys.limaone.fs.WorldFsBody.WrenchAstBodyChange;
import io.resys.limaone.fs.WorldFsBody.WrenchBody;
import io.resys.limaone.model.ArticlePage;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.persistence.AuthoringImpl;
import io.resys.limaone.persistence.ImmutableAuthoringConfig;
import io.resys.limaone.program.Runtime.EnvironmentProperties;
import io.resys.limaone.spi.compiler.CompilerImpl;
import io.resys.thena.fs.api.FileSystem;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RequiredArgsConstructor
@Slf4j
public class WorldFsBodyQueryImpl implements WorldFsBodyQuery {
  private final FileSystem filesystem;
  private final String branchName;
  private final EnvironmentProperties envir;
  
  private WrenchAstBodyChange changes;
  private BodyType bodyType;
  private String id;
  
  @Override
  public WorldFsBodyQuery id(String id) {
    this.id = id;
    return this;
  }
  @Override
  public WorldFsBodyQuery bodyType(BodyType bodyType) {
    this.bodyType = bodyType;
    return this;
  }
  @Override
  public WorldFsBodyQuery withTransientChanges(WrenchAstBodyChange changes) {
    this.changes = changes;
    return this;
  }
  @Override
  public Uni<WorldFsBody> getOne() {
    Objects.requireNonNull(id, () -> "id must be defined");
    Objects.requireNonNull(bodyType, () -> "bodyType must be defined");
    
    // handle wrench assets
    if(bodyType == BodyType.FLOW || bodyType == BodyType.DECISION_TABLE || bodyType == BodyType.FLOW_TASK) {
      return envir.getModelDb().worldQuery()
          .docs(BodyType.FLOW_TASK, BodyType.FLOW, BodyType.DECISION_TABLE)
          .findAll().onItem().transformToUni(modelWorld -> {
            if(changes == null) {
              return getWrenchBody(modelWorld);
            }
            return withWrenchBodyChange(changes);
          });
    }
    
    // stencil assets
    final var tenant = filesystem.withTenant();
    return tenant
      .branchQuery()
      .branchName(name -> name.equals(branchName))
      .blobTypes(Arrays.asList(BodyType.without(BodyType.DEPLOYMENT))
          .stream().map(e -> e.name())
          .toList().toArray(new String[]{}))
      .getOne()
      .onItem().transform(ref -> {
        final var node = ref.getTransitives().findOneNode(id)
            .orElseThrow(() -> new IllegalArgumentException("Node not found " + id));
      
      switch (bodyType) {
        case ARTICLE_PAGE: {
          final var page = node.getTransitives().getBlob().getBlobValue().mapTo(ArticlePage.class);
          return ImmutableArticlePageBody.builder()
              .content(page.getContent())
              .build();
        }
        default: throw new IllegalArgumentException("Unsupported body type: " + bodyType);
       }
      });
    
  }
  
  @Override
  public WorldFsBody getOneSync() {
    return getOne()
        .runSubscriptionOn(envir.getWorkerPool())
        .await().atMost(envir.getWorkerPoolMaxTimeout());
  }
  
  private Uni<WrenchBody> getWrenchBody(ModelWorld world) {
    final var state = ImmutableWrenchBody.builder();
    final var bundle = new CompilerImpl(envir).compile(world).build().getBundle();
    
    bundle.queryFlows().forEach(program -> {
      state.putFlows(program.getId(), ImmutableWrenchAstBody.<Flow_AST>builder()
          .ast(program.getAst())
          .errors(program.getErrors())
          .associations(program.getAssociations())
          .id(program.getId())
          .status(program.getStatus())
          .build());
    });
    
    bundle.queryFlowTasks().forEach(program -> {
      state.putServices(program.getId(), ImmutableWrenchAstBody.<FlowTask_AST>builder()
          .ast(program.getAst())
          .errors(program.getErrors())
          .associations(program.getAssociations())
          .id(program.getId())
          .status(program.getStatus())
          .build());
    });
    
    
    bundle.queryDecisions().forEach(program -> {
      state.putDecisions(program.getId(), ImmutableWrenchAstBody.<DecisionTable_AST>builder()
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

  private Uni<WrenchAstBody<?>> withWrenchBodyChange(WrenchAstBodyChange entity) {
    final Authoring authoring = new AuthoringImpl(ImmutableAuthoringConfig.builder()
        .envir(envir)
        .persistence(envir.getModelDb())
        .build())
        .withBranchName(Optional.ofNullable(branchName));
    
    if(entity.getBodyType() == BodyType.FLOW) {
      return authoring
        .modifyModel().modifyFlow().props(props -> props.flowId(entity.getId()).flowValue(entity.getBodySyntax())).buildTransientWorld()
        .onItem().transformToUni(world -> getWrenchBody(world))
        .map(world -> world.getEntity(entity.getId()));
    } else if(entity.getBodyType() == BodyType.FLOW_TASK) {
      return authoring
        .modifyModel().modifyFlowTask().props(props -> props.flowTaskId(entity.getId()).flowTaskValue(entity.getBodySyntax())).buildTransientWorld()
        .onItem().transformToUni(world -> getWrenchBody(world))
        .map(world -> world.getEntity(entity.getId()));
    }    
    return authoring
        .modifyModel().modifyDecisionTable().props(props -> props.decisionTableId(entity.getId()).nodes(entity.getBodyStatment())).buildTransientWorld()
        .onItem().transformToUni(world -> getWrenchBody(world))
        .map(world -> world.getEntity(entity.getId()));
  }

}
