package io.resys.limaone.spi.expression;

import java.util.Objects;

import io.resys.limaone.ast.attribute.Attribute_AST.ValueType;

public class CacheKey {
  final String src;
  final ValueType valueType;

  CacheKey(String src, ValueType valueType) {
    this.src = src;
    this.valueType = valueType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof CacheKey)) return false;
    CacheKey cacheKey = (CacheKey) o;
    return Objects.equals(src, cacheKey.src) && Objects.equals(valueType, cacheKey.valueType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(src, valueType);
  }
}