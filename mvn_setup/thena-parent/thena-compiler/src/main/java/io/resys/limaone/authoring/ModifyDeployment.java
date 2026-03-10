package io.resys.limaone.authoring;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Deployment.BundleStatus;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface ModifyDeployment {

  ModifyDeployment props(ModifyDeploymentProps props);
  ModifyDeployment props(Consumer<ImmutableModifyDeploymentProps.Builder> props);
  Uni<Model<Deployment>> build();
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableModifyDeploymentProps.class)
  @JsonDeserialize(as = ImmutableModifyDeploymentProps.class)
  interface ModifyDeploymentProps extends AuthoringModelProps {
    String getId();
    @Nullable String getName();
    @Nullable String getDescription();
    @Nullable OffsetDateTime getStartsAt();
    BundleStatus getStatus();
  }
}
