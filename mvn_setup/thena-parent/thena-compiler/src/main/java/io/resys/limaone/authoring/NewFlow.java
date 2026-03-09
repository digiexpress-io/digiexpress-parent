package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewFlow {

  NewFlow props(NewFlowProps props);
  NewFlow props(Consumer<ImmutableNewFlowProps.Builder> props);
  
  Uni<Model<Flow>> build();
  Model<Flow> buildSync();
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewFlowProps.class) @JsonDeserialize(as = ImmutableNewFlowProps.class)
  interface NewFlowProps extends AuthoringModelProps {
    @Nullable String getName();
    @Nullable String getDesc();
  }
}