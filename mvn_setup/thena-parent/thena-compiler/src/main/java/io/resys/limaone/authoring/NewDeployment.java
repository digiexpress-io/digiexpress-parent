package io.resys.limaone.authoring;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewDeployment {

  
  NewDeployment props(NewDeploymentProps props);
  NewDeployment props(Consumer<ImmutableNewDeploymentProps.Builder> props);
  
  Uni<NewDeploymentProps> build();
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableNewDeploymentProps.class)
  @JsonDeserialize(as = ImmutableNewDeploymentProps.class)
  public interface NewDeploymentProps extends AuthoringModelProps {
    @Nullable String getStencilTag(); // auto-create tag on null
    @Nullable String getWrenchTag();  // auto-create tag on null
    
    @Nullable String getName();  // autoname on null
    @Nullable String getDescription();
    @Nullable LocalDateTime getLiveDate();
    
  }
}
