package io.resys.limaone.authoring.flow;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.link.NewLink;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewFlow {

  NewLink props(NewFlowProps props);
  NewLink props(Consumer<ImmutableNewFlowProps.Builder> props);
  
  Uni<Model<Flow>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewFlowProps.class) @JsonDeserialize(as = ImmutableNewFlowProps.class)
  interface NewFlowProps {
    @Nullable String getName();
    @Nullable String getDesc();
  }
}