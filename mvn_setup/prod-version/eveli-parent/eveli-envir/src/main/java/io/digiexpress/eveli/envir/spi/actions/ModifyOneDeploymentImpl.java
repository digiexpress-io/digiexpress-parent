package io.digiexpress.eveli.envir.spi.actions;

import java.time.OffsetDateTime;
import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.ModifyOneDeployment;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.thena.api.actions.DocCommitActions.OneDocEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.spi.ThenaDocConfig;
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
