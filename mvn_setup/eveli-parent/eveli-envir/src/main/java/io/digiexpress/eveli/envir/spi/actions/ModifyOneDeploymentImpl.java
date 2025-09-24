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
import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.ModifyOneDeployment;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.doc.api.ThenaDocConfig;
import io.resys.thena.doc.api.DocCommitActions.OneDocEnvelope;
import io.resys.thena.doc.spi.support.DocStoreException;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class ModifyOneDeploymentImpl implements ModifyOneDeployment {
  private final EveliEnvirStore ctx;
  private String userId;
  
  private String id;
  private OffsetDateTime startsAt;
  private EveliDeploymentStatus status;

  @Override
  public Uni<EveliDeployment> build() {
    RepoAssert.notEmpty(userId, () -> "userId must be defined!");
    RepoAssert.notEmpty(id, () -> "id must be defined!");
    RepoAssert.isTrue(startsAt != null || status != null, () -> "startsAt or status must be defined!");
    
    final var config = ctx.getConfig();
    final var builder = config.getClient().doc(config.getRepoId()).commit()
        .modifyOneDoc()
        .docId(id)
        .commitAuthor(userId)
        .commitMessage("Update deployment by: " + ModifyOneDeploymentImpl.class);
    
    if(startsAt != null) {
      builder.docStartsAt(startsAt);
    }
    if(status != null) {
      builder.docSubStatus(status.name());
    }
    return builder.build().onItem().transform(env -> visitEnvelope(config, env));
  }


  public EveliDeployment visitEnvelope(ThenaDocConfig config, OneDocEnvelope envelope) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      throw DocStoreException.builder("GET_DEPLOYMENT_BY_ID_FOR_UPDATE_FAILED")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(JsonObject.of("id", id).encode()))
        .build();
    }
    return EveliEnvirStore.map(envelope.getDoc(), Optional.ofNullable(envelope.getBranch()));
  }
}
