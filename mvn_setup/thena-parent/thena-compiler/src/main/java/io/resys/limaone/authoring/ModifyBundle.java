package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Bundle.BundleStatus;
import io.smallrye.mutiny.Uni;

public interface ModifyBundle {

  ModifyBundle props(NewBundleProps props);
  ModifyBundle props(Consumer<ImmutableNewBundleProps.Builder> props);
  Uni<NewBundleProps> build();
  
  @Value.Immutable
  @JsonSerialize(as = ImmutableNewBundleProps.class)
  @JsonDeserialize(as = ImmutableNewBundleProps.class)
  interface NewBundleProps {
    String getId();
    BundleStatus getStatus();
  }
}
