package io.resys.limaone.authoring;

import java.util.List;
import java.util.function.Consumer;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.DecisionTable;
import io.resys.limaone.model.DecisionTable.DecisionTableNode;
import io.resys.limaone.model.Model;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface NewDecisionTable {

  NewArticleLink props(NewDecisionTableProps props);
  NewArticleLink props(Consumer<ImmutableNewDecisionTableProps.Builder> props);
  
  Uni<Model<DecisionTable>> build();
  
  
  @Value.Immutable @JsonSerialize(as = ImmutableNewDecisionTableProps.class) @JsonDeserialize(as = ImmutableNewDecisionTableProps.class)
  interface NewDecisionTableProps {
    @Nullable String getName();
    @Nullable String getDesc();
    List<DecisionTableNode> getNodes();
  }
}