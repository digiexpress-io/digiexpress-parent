package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.FlowTask;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface ModifyFlowTask {
  
  ModifyFlowTask props(ModifyFlowTaskProps props);
  ModifyFlowTask props(Consumer<ImmutableModifyFlowTaskProps.Builder> props);
  
  Uni<Model<FlowTask>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyFlowTaskProps.class) @JsonDeserialize(as = ImmutableModifyFlowTaskProps.class)
  interface ModifyFlowTaskProps extends AuthoringModelProps {
    String getFlowTaskId();
    String getTaskName();
    String getTaskValue();
  }
}