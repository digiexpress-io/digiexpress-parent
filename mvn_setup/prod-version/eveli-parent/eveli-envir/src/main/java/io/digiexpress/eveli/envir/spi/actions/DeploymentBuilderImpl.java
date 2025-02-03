package io.digiexpress.eveli.envir.spi.actions;

import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.envir.api.EveliEnvirClient.DeploymentBuilder;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeployment;
import io.digiexpress.eveli.envir.api.EveliEnvirClient.EveliDeploymentStatus;
import io.digiexpress.eveli.envir.spi.EveliEnvirStore;
import io.resys.thena.api.actions.DocCommitActions.ManyDocsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.spi.DocStoreException;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;


@RequiredArgsConstructor
@Setter @Accessors(fluent = true)
public class DeploymentBuilderImpl implements DeploymentBuilder {
  private final EveliEnvirStore ctx;
  private String userId;
  private String deploymentId;
  
  @Override
  public Uni<EveliDeployment> build() {
    RepoAssert.notEmpty(userId, () -> "userId must be defined!");
    RepoAssert.notEmpty(deploymentId, () -> "deploymentId must be defined!");
    
    return Uni.combine().all().unis(
      new DeploymentQueryImpl(ctx).emptyBranchBody(true).getOneById(deploymentId),
      new DeploymentQueryImpl(ctx).emptyBranchBody(true).status(EveliDeploymentStatus.DEPLOYED).findAll()
    )
    .asTuple().onItem().transformToUni(tuple -> applyUpdate(tuple.getItem1(), tuple.getItem2()))
    .onItem().transform(this::validateUpdateResponse)
    .onItem().transform(this::createResult);
  }
  
  private Uni<ManyDocsEnvelope> applyUpdate(EveliDeployment target, List<EveliDeployment> deployed) {
    final var config = ctx.getConfig();
    
    final var builder = config.getClient().doc(config.getRepoId()).commit().modifyManyDocs()
      .commitMessage("Activating singular deployment")
      .commitAuthor(DeploymentBuilderImpl.class.getName());
    
    for(final var dep : deployed) {
      builder.item()
        .docId(dep.getId())
        .docSubStatus(EveliDeploymentStatus.READY.name())
        .next();
    }
    
    return builder.item()
      .docId(target.getId())
      .docSubStatus(EveliDeploymentStatus.DEPLOYED.name())
      .next()
      .build();
  }
  
  public ManyDocsEnvelope validateUpdateResponse(ManyDocsEnvelope envelope) {
    if(envelope.getStatus() != CommitResultStatus.OK) {
      final var config = ctx.getConfig();
      throw DocStoreException.builder("DEPLOYMENT_UPDATE_FAILED")
        .add(config, envelope)
        .add((callback) -> callback.addArgs(JsonObject.of("id", deploymentId).encode()))
        .build();
    }
    return envelope;
  }

  private EveliDeployment createResult(ManyDocsEnvelope envelope) {
    final var dep = envelope.getDoc().stream()
        .filter(doc -> doc.getId().equals(deploymentId)).findFirst()
        .orElseThrow(() -> {
          final var config = ctx.getConfig();
          throw DocStoreException.builder("DEPLOYMENT_UPDATED_BUT_NOT_IN_RESULT")
          .add(config, envelope)
          .add((callback) -> callback.addArgs(JsonObject.of("id", deploymentId).encode()))
          .build();
        });
    return EveliEnvirStore.map(dep, Optional.empty());
  }
}
