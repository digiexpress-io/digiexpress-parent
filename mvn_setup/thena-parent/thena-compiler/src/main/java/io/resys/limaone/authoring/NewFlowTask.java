package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewFlowTask {

  NewFlowTask props(NewFlowTaskProps props);
  NewFlowTask props(Consumer<ImmutableNewFlowTaskProps.Builder> props);
  
  Uni<Model<FlowTask>> build();
  Model<FlowTask> buildSync();
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewFlowTaskProps.class) @JsonDeserialize(as = ImmutableNewFlowTaskProps.class)
  interface NewFlowTaskProps extends AuthoringModelProps {
    @Nullable String getName();
    @Nullable String getDesc();
  }
}