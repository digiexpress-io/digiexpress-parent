package io.resys.limaone.authoring.flow;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Flow;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface ModifyFlow {
  
  ModifyFlow props(ModifyFlowProps props);
  ModifyFlow props(Consumer<ImmutableModifyFlowProps.Builder> props);
  
  Uni<Model<Flow>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyFlowProps.class) @JsonDeserialize(as = ImmutableModifyFlowProps.class)
  interface ModifyFlowProps {
    String getFlowId();
    String getFlowName();
    String getFlowValue();
  }
}