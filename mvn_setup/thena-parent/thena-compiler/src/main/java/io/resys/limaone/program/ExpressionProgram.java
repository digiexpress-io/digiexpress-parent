package io.resys.limaone.program;

import java.util.List;

import org.immutables.value.Value;

import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.Program.ProgramResult;
import jakarta.annotation.Nullable;

public interface ExpressionProgram {
  String getSrc();
  ValueType getType();
  List<String> getConstants();
  ExpressionResult run(Object context);

  @Value.Immutable
  interface ExpressionResult extends ProgramResult {
    ValueType getType();
    List<String> getConstants();
    
    @Nullable Object getValue();
  }
}
