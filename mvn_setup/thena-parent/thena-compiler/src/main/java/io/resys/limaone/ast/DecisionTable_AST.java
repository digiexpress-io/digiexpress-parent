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
  
  // Legacy field - not used in current implementation
  List<String> getHeaderTypes();
  // Legacy field - not used in current implementation  
  Map<ValueType, List<String>> getHeaderExpressions();
  
  // Determines how multiple matching rules are handled (FIRST, ALL, etc.)
  HitPolicy getHitPolicy();
  
  // List of decision rules ordered by execution priority
  List<DecisionRowNode> getRows();

  // Represents a single decision rule with input conditions and output actions
  @Value.Immutable
  @JsonSerialize(as = ImmutableDecisionRowNode.class)
  @JsonDeserialize(as = ImmutableDecisionRowNode.class)
  interface DecisionRowNode extends Serializable {
    // Unique identifier for this rule
    String getId();
    // Execution order - lower numbers execute first
    int getOrder();
    // List of cells containing values for each parameter
    List<DecisionCellNode> getCells();
  }

  // Represents a single cell in a decision rule containing a value for a specific parameter
  @Value.Immutable
  @JsonSerialize(as = ImmutableDecisionCellNode.class)
  @JsonDeserialize(as = ImmutableDecisionCellNode.class)
  interface DecisionCellNode extends Serializable {
    // Unique identifier for this cell
    String getId();
    // References the Parameter.id this cell provides a value for
    String getHeader();
    // The actual rule value (e.g., "in [\"CAREFUL\"]", ">= 3", "GREEN")
    @Nullable    
    String getValue();
  }
}
