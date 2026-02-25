package io.resys.limaone.ast.decisiontable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.ast.attribute.AST;
import io.resys.limaone.ast.attribute.Attribute_AST.ValueType;
import jakarta.annotation.Nullable;


@Value.Immutable
@JsonSerialize(as = ImmutableDecisionTable_AST.class)
@JsonDeserialize(as = ImmutableDecisionTable_AST.class)
public interface DecisionTable_AST extends AST {
  
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

  enum HitPolicy { FIRST, ALL }
  enum ColumnExpressionType { 
    IN, EQUALS, 
    // pattern matching for special symbols
    // "." - word separator   
    // "#" - match one or more word 
    // "*" - match one word
    QIN  
  }
}
