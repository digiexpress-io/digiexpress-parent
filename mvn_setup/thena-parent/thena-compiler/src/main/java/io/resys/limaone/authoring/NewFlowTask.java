package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewFlowTask {

  NewArticleLink props(NewFlowTaskProps props);
  NewArticleLink props(Consumer<ImmutableNewFlowTaskProps.Builder> props);
  
  Uni<Model<FlowTask>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewFlowTaskProps.class) @JsonDeserialize(as = ImmutableNewFlowTaskProps.class)
  interface NewFlowTaskProps {
    @Nullable String getName();
    @Nullable String getDesc();
  }
}