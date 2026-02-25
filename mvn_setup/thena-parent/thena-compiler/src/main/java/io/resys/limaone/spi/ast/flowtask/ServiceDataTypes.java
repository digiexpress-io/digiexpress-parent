package io.resys.limaone.spi.ast.flowtask;

import org.immutables.value.Value;

import io.resys.limaone.ast.Simple_AST.Headers_AST;
import io.resys.limaone.model.Parameter;
import jakarta.annotation.Nullable;

@Value.Immutable
public interface ServiceDataTypes {
  Headers_AST getHeaders();
  
  @Nullable Parameter getAcceptType0();
  @Nullable Parameter getAcceptType1();
  @Nullable Parameter getReturnType();
}