package io.resys.limaone.ast.decisiontable;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.ast.attribute.Attribute_AST.ValueType;
import jakarta.annotation.Nullable;

public interface DecisionTable_AST {
  
  List<String> getHeaderTypes();
  Map<ValueType, List<String>> getHeaderExpressions();
  HitPolicy getHitPolicy();
  List<AstDecisionRow> getRows();

  @Value.Immutable
  @JsonSerialize(as = ImmutableAstDecisionRow.class)
  @JsonDeserialize(as = ImmutableAstDecisionRow.class)
  interface AstDecisionRow extends Serializable {
    String getId();
    int getOrder();
    List<AstDecisionCell> getCells();
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableAstDecisionCell.class)
  @JsonDeserialize(as = ImmutableAstDecisionCell.class)
  interface AstDecisionCell extends Serializable {
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
