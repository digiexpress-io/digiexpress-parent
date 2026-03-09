package io.resys.limaone.authoring;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthorProps;
import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.Deployment;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewDeployment {

  NewDeployment author(AuthorProps author);
  
  NewDeployment props(NewDeploymentProps props);
  NewDeployment props(Consumer<ImmutableNewDeploymentProps.Builder> props);
  
  Uni<Model<Deployment>> build();
  Model<Deployment> buildSync();
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableNewDeploymentProps.class)
  @JsonDeserialize(as = ImmutableNewDeploymentProps.class)
  public interface NewDeploymentProps extends AuthoringModelProps {
    
    @Nullable String getName();  // autoname on null
    @Nullable String getDescription();
    @Nullable OffsetDateTime getLiveDate();
  }
}
