package io.resys.limaone.spi.program.expression;

import java.util.Objects;

import io.resys.limaone.model.Parameter.ValueType;
import lombok.Value;


@Value
public class ExpressionCacheKey {
  String src;
  ValueType valueType;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ExpressionCacheKey)) return false;
    ExpressionCacheKey cacheKey = (ExpressionCacheKey) o;
    return Objects.equals(src, cacheKey.src) && Objects.equals(valueType, cacheKey.valueType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(src, valueType);
  }
}