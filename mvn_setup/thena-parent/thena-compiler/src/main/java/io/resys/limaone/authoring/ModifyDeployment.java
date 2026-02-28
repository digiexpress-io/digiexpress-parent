package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Deployment.BundleStatus;
import io.smallrye.mutiny.Uni;

public interface ModifyDeployment {

  ModifyDeployment props(ModifyDeploymentProps props);
  ModifyDeployment props(Consumer<ImmutableModifyDeploymentProps.Builder> props);
  Uni<ModifyDeploymentProps> build();
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableModifyDeploymentProps.class)
  @JsonDeserialize(as = ImmutableModifyDeploymentProps.class)
  interface ModifyDeploymentProps {
    String getId();
    BundleStatus getStatus();
  }
}
