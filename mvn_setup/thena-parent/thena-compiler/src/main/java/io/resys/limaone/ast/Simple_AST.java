package io.resys.limaone.ast;

import java.io.Serializable;
import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.ModelError;
import io.resys.limaone.model.Parameter;
import jakarta.annotation.Nullable;

public interface Simple_AST {
  String getId();
  String getName();
  String getHash();
  
  @Nullable String getDescription();
  BodyType getBodyType();
  
  Headers_AST getHeaders();
  List<ModelError> getErrors();
  
  @Value.Immutable @JsonSerialize(as = ImmutableHeaders_AST.class) @JsonDeserialize(as = ImmutableHeaders_AST.class)
  interface Headers_AST extends Serializable {
    List<Parameter> getAcceptDefs();
    List<Parameter> getReturnDefs();
  }
}