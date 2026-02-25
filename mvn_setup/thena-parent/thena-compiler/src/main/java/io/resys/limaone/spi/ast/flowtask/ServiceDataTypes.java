package io.resys.limaone.spi.ast.flowtask;

import org.immutables.value.Value;

import io.resys.limaone.ast.AST.Headers_AST;
import io.resys.limaone.ast.Attribute_AST;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface ServiceDataTypes {
  Headers_AST getHeaders();
  
  @Nullable Attribute_AST getAcceptType0();
  @Nullable Attribute_AST getAcceptType1();
  @Nullable Attribute_AST getReturnType();
}