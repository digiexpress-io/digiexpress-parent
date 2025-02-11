package io.digiexpress.eveli.envir.spi.actions;

/*-
 * #%L
 * eveli-envir
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.Optional;

import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentCompiler;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.hdes.client.api.programs.ProgramEnvir;
import io.resys.hdes.client.spi.HdesClientEnvirBuilder;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.resys.hdes.client.spi.config.HdesClientConfig;
import io.resys.hdes.client.spi.envir.ProgramEnvirFactory;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.spi.MarkdownBuilderImpl;
import io.thestencil.client.spi.SitesBuilderImpl;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class EveliDeploymentCompilerImpl implements EveliDeploymentCompiler {
  private final EveliEnvirStore ctx;
  private final HdesClientConfig hdesClientConfig;
  private final DialobClient dialobClient;
  
  private String userId;
  private String deploymentId;

  @Override
  public Uni<EveliDeployment> compile() {
    RepoAssert.notEmpty(userId, () -> "userId must be defined!");
    RepoAssert.notEmpty(deploymentId, () -> "deploymentId must be defined!");
    
    return new DeploymentQueryImpl(ctx).emptyBranchBody(false).getOneById(deploymentId)
        .onItem().transformToUni(this::visitMerge);
  }
  
  private Uni<EveliDeployment> visitMerge(EveliDeployment deployment) {
    final var result = visitEnvir(deployment);
    log.info(new StringBuilder("Compiled deployment\r\n")
        .append("  - deployment\r\n")
        .append("    id: {}\r\n")
        .append("    name: {}\r\n")
        .append("    status: {} -> {}\r\n")
        .toString(),
        
        deploymentId, deployment.getName(), deployment.getStatus(), result.getItem1());
    
    final var config = ctx.getConfig();
    return config.getClient().doc(config.getRepoId()).commit()
        .modifyOneDoc()
        .docId(deploymentId)
        .commitAuthor(userId)
        .commitMessage("Update deployment by: " + EveliDeploymentCompilerImpl.class)
        .docSubStatus(result.getItem1().name())
        .meta(result.getItem2())
        .build().onItem().transform(env -> visitEnvelope(config, env));
  }
  
  public EveliDeployment visitEnvelope(ThenaDocConfig config, OneDocEnvelope envelope) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      throw DocStoreException.builder("GET_DEPLOYMENT_BY_ID_FOR_COMPILING_FAILED")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(JsonObject.of("id", deploymentId).encode()))
        .build();
    }
    return EveliEnvirStore.map(envelope.getDoc(), Optional.ofNullable(envelope.getBranch()));
  }

  private Tuple2<EveliDeploymentStatus, JsonObject> visitEnvir(EveliDeployment deployment) {
    final var wrench = visitWrench(deployment);
    final var stencil = visitStencil(deployment);
    
    final var errors1 = new DeploymentEnvirValidator(deployment, stencil, wrench).accept();
    if(errors1.isPresent()) {
      return Tuple2.of(EveliDeploymentStatus.ERROR, errors1.get());
    }
    final var errors2 = new DeploymentEnvirDialobUploader(dialobClient, deployment, stencil).accept();
    if(errors2.isPresent()) {
      return Tuple2.of(EveliDeploymentStatus.ERROR, errors2.get());      
    }
    
    return Tuple2.of(EveliDeploymentStatus.READY, null);       
  }
  
  private ProgramEnvir visitWrench(EveliDeployment deployment) {
    final var envir = new HdesClientEnvirBuilder(new ProgramEnvirFactory(hdesClientConfig), hdesClientConfig.getTypes())
        .tagName(deployment.getName())
        .callback(builder -> ComposerEntityMapper.toEnvir(builder, deployment.getSources().getWrench()).build())
        .build();
    return envir;
  }
  
  private Sites visitStencil(EveliDeployment deployment) {
    final var state = deployment.getSources().getStencil();
    final var markdowns = new MarkdownBuilderImpl()
      .targetDate(null)
      .json(state, true)
      .build();
    
    final var envir = new SitesBuilderImpl()
      .imagePath("images")
      .created(System.currentTimeMillis())
      .source(markdowns)
      .tagName(deployment.getName())
      .build();
    return envir;
  }
}
