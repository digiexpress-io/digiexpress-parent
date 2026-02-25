package io.resys.limaone.ast;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.BodyType;
import jakarta.annotation.Nullable;

public interface Simple_AST {
  String getId();
  String getName();
  @Nullable String getDescription();
  BodyType getBodyType();
  
  Headers_AST getHeaders();
  List<Message_AST> getMessages();

  
  @Value.Immutable @JsonSerialize(as = ImmutableHeaders_AST.class) @JsonDeserialize(as = ImmutableHeaders_AST.class)
  interface Headers_AST extends Serializable {
    List<Attribute_AST> getAcceptDefs();
    List<Attribute_AST> getReturnDefs();
  }
  @Value.Immutable @JsonSerialize(as = ImmutableMessage_AST.class) @JsonDeserialize(as = ImmutableMessage_AST.class)
  interface Message_AST extends Serializable {
    int getLine();
    String getValue();
    MessageType getType();
    @Nullable MessageRange_AST getRange();
  }

  @Value.Immutable @JsonSerialize(as = ImmutableMessageRange_AST.class) @JsonDeserialize(as = ImmutableMessageRange_AST.class)
  interface MessageRange_AST extends Serializable {
    int getStart();
    int getEnd();
    @Nullable
    Integer getColumn();
    @Nullable
    Boolean getInsert();
  }
  
  enum MessageType { ERROR, WARNING }
}