package io.resys.limaone.authoring;

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.authoring.Authoring.AuthoringModelProps;
import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.DecisionTable.DecisionStatement;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;

public interface ModifyDecisionTable {
  
  ModifyDecisionTable props(ModifyDecisionTableProps props);
  ModifyDecisionTable props(Consumer<ImmutableModifyDecisionTableProps.Builder> props);
  
  Uni<Model<DecisionTable>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableModifyDecisionTableProps.class) @JsonDeserialize(as = ImmutableModifyDecisionTableProps.class)
  interface ModifyDecisionTableProps extends AuthoringModelProps {
    String getDecisionTableId();
    String getName();
    List<DecisionStatement> getNodes();
  }
}