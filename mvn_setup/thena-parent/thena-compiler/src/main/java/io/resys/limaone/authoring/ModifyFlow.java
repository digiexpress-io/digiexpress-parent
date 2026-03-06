package io.resys.limaone.authoring;

import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.Flow;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface ModifyFlow {
  
  ModifyFlow props(ModifyFlowProps props);
  ModifyFlow props(Consumer<ImmutableModifyFlowProps.Builder> props);
  
  Uni<Model<Flow>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyFlowProps.class) @JsonDeserialize(as = ImmutableModifyFlowProps.class)
  interface ModifyFlowProps extends AuthoringModelProps {
    String getFlowId();
    String getFlowName();
    String getFlowValue();
  }
}