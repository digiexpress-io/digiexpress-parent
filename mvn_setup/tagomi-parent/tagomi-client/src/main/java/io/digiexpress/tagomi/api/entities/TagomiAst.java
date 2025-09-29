package io.digiexpress.tagomi.api.entities;

import java.io.Serializable;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

public interface TagomiAst {
  
  @JsonSerialize(as = ImmutableTypeDef.class)
  @JsonDeserialize(as = ImmutableTypeDef.class)
  @Value.Immutable
  interface TypeDef {
    String getModuleName();
    String getValueName();
  }
  
  @JsonSerialize(as = ImmutableAstMessage.class)
  @JsonDeserialize(as = ImmutableAstMessage.class)
  @Value.Immutable
  interface AstMessage extends Serializable {
    int getLine();
    String getValue();
    CommandMessageType getType();
    @Nullable
    AstRange getRange();
  }
  
  @JsonSerialize(as = ImmutableAstRange.class)
  @JsonDeserialize(as = ImmutableAstRange.class)
  @Value.Immutable
  interface AstRange extends Serializable {
    int getStart();
    int getEnd();
    @Nullable
    Integer getColumn();
    @Nullable
    Boolean getInsert();
  }

  enum CommandMessageType { ERROR, WARNING }
}
