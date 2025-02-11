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

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.dialob.api.form.Form;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.CreateOneDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.api.ImmutableEveliSources;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.thena.api.actions.DocCommitActions.CreateOneDoc;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.envelope.DocContainer.DocTenantObjects;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.api.envelope.QueryEnvelope.QueryEnvelopeStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
import io.resys.thena.spi.ThenaDocConfig.OneDocCreateVisitor;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class CreateOneDeploymentImpl implements CreateOneDeployment, OneDocCreateVisitor<EveliDeployment> {
  private final EveliEnvirStore ctx;

  private String userId;
  private String name;
  private String description;
  private OffsetDateTime startsAt;
  private SiteState stencil;
  private AstTag wrench;
  private List<Form> dialob;
  
  @Override
  public Uni<EveliDeployment> build() {
    RepoAssert.notEmpty(userId, () -> "userId must be defined!");
    RepoAssert.notEmpty(name, () -> "name must be defined!");
    RepoAssert.notNull(startsAt, () -> "startsAt must be defined!");
    RepoAssert.notNull(stencil, () -> "stencil must be defined!");
    RepoAssert.notNull(wrench, () -> "wrench must be defined!");
    RepoAssert.notNull(dialob, () -> "dialob must be defined!");
    
    final var config = ctx.getConfig();
    return config.getClient().doc(config.getRepoId())
        .find().docQuery()
        .docType(EveliEnvirStore.DOC_TYPE_DEPLOYMENT)
        .emptyBranchBody()
        .findAll(Arrays.asList(name))
        .onItem().transformToUni(e -> {
          visitExistingDocs(config, e);
          return config.accept(this);
        });
  }
  
  private void visitExistingDocs(ThenaDocConfig config, QueryEnvelope<DocTenantObjects> envelope) {
    if(envelope.getStatus() != QueryEnvelopeStatus.OK) {
      throw DocStoreException.builder("CREATE_DEPLOYMENT_FAILED_TO_QUERY_EXISTING_DOCS").add(config, envelope).build();
    }
    final var existing = envelope.getObjects().getDocs().values();
    if(!existing.isEmpty()) {
      final var existingDocs = JsonObject.of("existing-docs", existing.stream().map(e -> JsonObject.mapFrom(e)).toList()).encodePrettily();
      throw DocStoreException.builder("CREATE_DEPLOYMENT_FAILED_BECAUSE_DOC_ALREADY_EXISTS")
        .add(config, envelope)
        .add((msg) -> msg.addArgs(existingDocs))
        .build();
    }
  }

  @Override
  public CreateOneDoc start(ThenaDocConfig config, CreateOneDoc builder) {

    
    
    return builder
        .commitAuthor(userId)
        .commitMessage("Creating new deployment from: " + CreateOneDeploymentImpl.class)
        .commitLogExcludesBranchBody()
        .branchContent(JsonObject.mapFrom(ImmutableEveliSources.builder()
            .stencil(ImmutableSiteState.builder().from(stencil).releases(Collections.emptyMap()).build())
            .wrench(wrench)
            .dialob(dialob)
            .build()))
        .ownerId(userId)
        .docName(name)
        .docSubStatus(EveliDeploymentStatus.BUILDING.name())
        .docType(EveliEnvirStore.DOC_TYPE_DEPLOYMENT)
        .docDescription(EveliEnvirStore.formatDescription(description, stencil, wrench))
        .docStartsAt(startsAt.withSecond(0).withNano(0));
  }
  


  @Override
  public OneDocEnvelope visitEnvelope(ThenaDocConfig config, OneDocEnvelope envelope) {
    if(envelope.getStatus() == CommitResultStatus.OK) {
      return envelope;
    }
    throw new DocStoreException("CREATE_DEPLOYMENT_FAILED", DocStoreException.convertMessages(envelope));
  }

  @Override
  public EveliDeployment end(ThenaDocConfig config, OneDocEnvelope commit) {
    return EveliEnvirStore.map(commit.getDoc(), Optional.ofNullable(commit.getBranch()));
  }
}
