package io.resys.limaone.spi;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.resys.limaone.ast.Attribute_AST.ValueType;
import io.resys.limaone.program.ExpressionProgram;
import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.ast.FlowTask_AST;
import io.resys.limaone.ast.Flow_AST;
import lombok.Value;

public class LocalCache {
  private static final ConcurrentHashMap<ExpressionCacheKey, ExpressionProgram> EXPRESSION_CACHE = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<DecisionTable_AST_CacheKey, DecisionTable_AST> DT_CACHE = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<Flow_AST_CacheKey, Flow_AST> FLOW_CACHE = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<FlowTask_AST_CacheKey, FlowTask_AST> FLOW_TASK_CACHE = new ConcurrentHashMap<>();


  public static FlowTask_AST computeIfAbsent(
      FlowTask_AST_CacheKey key, 
      Function<FlowTask_AST_CacheKey, FlowTask_AST> mappingFunction) {
    
    return FLOW_TASK_CACHE.computeIfAbsent(key, mappingFunction);
  }
  
  public static Flow_AST computeIfAbsent(
      Flow_AST_CacheKey key, 
      Function<Flow_AST_CacheKey, Flow_AST> mappingFunction) {
    
    return FLOW_CACHE.computeIfAbsent(key, mappingFunction);
  }

  public static DecisionTable_AST computeIfAbsent(
      DecisionTable_AST_CacheKey key, 
      Function<DecisionTable_AST_CacheKey, DecisionTable_AST> mappingFunction) {
    
    return DT_CACHE.computeIfAbsent(key, mappingFunction);
  }  
  
  public static ExpressionProgram computeIfAbsent(
      ExpressionCacheKey key, 
      Function<ExpressionCacheKey, ExpressionProgram> mappingFunction) {
    
    return EXPRESSION_CACHE.computeIfAbsent(key, mappingFunction);
  }
  
  public static class Flow_AST_CacheKey {
    private final String key;
    private final int hashCode;

    public Flow_AST_CacheKey(String input) {
      this.key = input != null ? input : "";
      this.hashCode = this.key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof FlowTask_AST_CacheKey)) return false;
      return key.equals(((FlowTask_AST_CacheKey) o).key);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public String toString() {
      return "Flow_AST_CacheKey{" + key + "}";
    }
  }
  
  public static class DecisionTable_AST_CacheKey {
    private final String key;
    private final int hashCode;

    public DecisionTable_AST_CacheKey(String input) {
      this.key = input != null ? input : "";
      this.hashCode = this.key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof FlowTask_AST_CacheKey)) return false;
      return key.equals(((FlowTask_AST_CacheKey) o).key);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public String toString() {
      return "DecisionTable_AST_CacheKey{" + key + "}";
    }
  }
  
  public static class FlowTask_AST_CacheKey {
    private final String key;
    private final int hashCode;

    public FlowTask_AST_CacheKey(String input) {
      this.key = input != null ? input : "";
      this.hashCode = this.key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof FlowTask_AST_CacheKey)) return false;
      return key.equals(((FlowTask_AST_CacheKey) o).key);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public String toString() {
      return "FlowTask_AST_CacheKey{" + key + "}";
    }
  }
  
  @Value
  public static class ExpressionCacheKey {
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
  
  public static void flushAll() {
    EXPRESSION_CACHE.clear();
  }
}
