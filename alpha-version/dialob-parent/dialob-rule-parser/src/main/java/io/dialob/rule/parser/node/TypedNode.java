package io.dialob.rule.parser.node;

import io.dialob.rule.parser.api.ValueType;
import org.jetbrains.annotations.Nullable;


public interface TypedNode {
  @Nullable
  ValueType getValueType();
}
