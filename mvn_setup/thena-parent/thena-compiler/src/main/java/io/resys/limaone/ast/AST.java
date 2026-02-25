package io.resys.limaone.ast;

import io.resys.limaone.model.Model.BodyType;
import jakarta.annotation.Nullable;

public interface AST {
  String getId();
  String getName();
  @Nullable String getDescription();
  BodyType getBodyType();
  
  Headers_AST getHeaders();
}