package io.digiexpress.eveli.envir.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.dialob.api.form.Form;
import io.resys.hdes.client.api.HdesClient.ExecutorBuilder;
import io.resys.hdes.client.api.ast.AstTag;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;


public interface EveliEnvirClient {
  CreateOneDeployment createOneDeployment();
  ModifyOneDeployment modifyOneDeployment();
  EveliRuntimeQuery runtimeQuery();
  DeploymentQuery deploymentQuery();
  
  
  interface DeploymentQuery {
    Uni<List<EveliDeployment>> findAll();  
  }
  
  interface EveliRuntimeQuery {
    Uni<EveliRuntime> getOne();
    Uni<Optional<EveliRuntime>> findOne();
  }
  
  interface CreateOneDeployment {
    CreateOneDeployment name(String name);    
    CreateOneDeployment startsAt(OffsetDateTime startsAt);
    CreateOneDeployment stencil(SiteState stencil);
    CreateOneDeployment wrench(AstTag wrench);
    CreateOneDeployment dialob(List<Form> dialob);
    Uni<EveliDeployment> build();
  }
  
  
  interface ModifyOneDeployment {
    ModifyOneDeployment id(String idOrName);
    ModifyOneDeployment startsAt(OffsetDateTime startsAt);
    ModifyOneDeployment status(EveliDeploymentStatus status);
    Uni<EveliDeployment> build();
  }
  
  @JsonSerialize(as = ImmutableEveliDeployment.class)
  @JsonDeserialize(as = ImmutableEveliDeployment.class)
  @Value.Immutable
  interface EveliDeployment {
    String getId();
    String getName();
    String getExternalId();

    EveliDeploymentStatus getStatus();
    @Nullable JsonObject getErrors();

    OffsetDateTime getCreatedAt();
    OffsetDateTime getStartsAt();

    SiteState getStencil();
    AstTag getWrench();
    List<Form> getDialob();
  }
  
  enum EveliDeploymentStatus {
    BUILDING, READY, ERROR, DEPLOYED
  }

  interface EveliRuntime {
    String getName();
    String getSrcId();
    ExecutorBuilder getWrench();
    Sites getStencil();
    List<Form> getForms();
  }
}
