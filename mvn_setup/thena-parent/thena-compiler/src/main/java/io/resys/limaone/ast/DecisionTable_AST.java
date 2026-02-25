package io.resys.limaone.ast;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.model.DecisionTable.HitPolicy;
import jakarta.annotation.Nullable;


@Value.Immutable
@JsonSerialize(as = ImmutableDecisionTable_AST.class)
@JsonDeserialize(as = ImmutableDecisionTable_AST.class)
public interface DecisionTable_AST extends Simple_AST {
  
  List<String> getHeaderTypes();
  Map<ValueType, List<String>> getHeaderExpressions();
  HitPolicy getHitPolicy();
  List<DecisionRow> getRows();

  @Value.Immutable
  @JsonSerialize(as = ImmutableDecisionRow.class)
  @JsonDeserialize(as = ImmutableDecisionRow.class)
  interface DecisionRow extends Serializable {
    String getId();
    int getOrder();
    List<DecisionCell> getCells();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableDecisionCell.class)
  @JsonDeserialize(as = ImmutableDecisionCell.class)
  interface DecisionCell extends Serializable {
    String getId();
    String getHeader();
    @Nullable    
    String getValue();
  }
}
