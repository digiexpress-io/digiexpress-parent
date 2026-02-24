package io.resys.limaone.authoring.flowtask;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface ModifyFlowTask {
  
  ModifyFlowTask props(ModifyFlowTaskProps props);
  ModifyFlowTask props(Consumer<ImmutableModifyFlowTaskProps.Builder> props);
  
  Uni<Model<FlowTask>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyFlowTaskProps.class) @JsonDeserialize(as = ImmutableModifyFlowTaskProps.class)
  interface ModifyFlowTaskProps {
    String getFlowTaskId();
    String getTaskName();
    String getTaskValue();
  }
}