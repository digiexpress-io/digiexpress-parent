package io.resys.limaone.model;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.Body;
import jakarta.annotation.Nullable;


@Value.Immutable
@JsonSerialize(as = ImmutableDecisionTable.class)
@JsonDeserialize(as = ImmutableDecisionTable.class)
public interface DecisionTable extends Body {

  String getName();
  List<DecisionStatement> getNodes();
  
  @JsonIgnoreProperties(ignoreUnknown = true)
  @Value.Immutable @JsonSerialize(as = ImmutableDecisionStatement.class) @JsonDeserialize(as = ImmutableDecisionStatement.class)
  interface DecisionStatement extends Serializable {
    @Nullable String getId();
    @Nullable String getValue();
    StatementType getType();
  }
  
  enum StatementType {
    SET_NAME, SET_DESCRIPTION, IMPORT_CSV, IMPORT_ORDERED_CSV,
    MOVE_ROW, MOVE_HEADER, INSERT_ROW, COPY_ROW,
    SET_HEADER_TYPE, SET_HEADER_REF, SET_HEADER_NAME, SET_HEADER_EXTERNAL_REF,
    SET_HEADER_SCRIPT, SET_HEADER_DIRECTION, SET_HEADER_EXPRESSION, SET_HIT_POLICY, SET_CELL_VALUE,
    DELETE_CELL, DELETE_HEADER, DELETE_ROW,
    ADD_HEADER_IN, ADD_HEADER_OUT, ADD_ROW, SET_VALUE_SET,
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
