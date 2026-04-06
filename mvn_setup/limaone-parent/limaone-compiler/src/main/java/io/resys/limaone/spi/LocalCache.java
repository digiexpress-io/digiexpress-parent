package io.resys.limaone.spi;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import io.resys.limaone.ast.ArticleWorkflow_AST;
import io.resys.limaone.ast.DecisionTable_AST;
import io.resys.limaone.ast.DialobForm_AST;
import io.resys.limaone.ast.FlowTask_AST;
import io.resys.limaone.ast.Flow_AST;
import io.resys.limaone.model.Parameter.ValueType;
import io.resys.limaone.program.Compiler.Bundle;
import io.resys.limaone.program.ExpressionProgram;
import io.resys.limaone.spi.ast.MarkdownVisitor.Markdown_AST;
import lombok.Value;

public class LocalCache {
  private static final ConcurrentHashMap<ExpressionCacheKey, ExpressionProgram> EXPRESSION_CACHE = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<DecisionTable_AST_CacheKey, DecisionTable_AST> DT_CACHE = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<Flow_AST_CacheKey, Flow_AST> FLOW_CACHE = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<FlowTask_AST_CacheKey, FlowTask_AST> FLOW_TASK_CACHE = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<Bundle_CacheKey, Bundle> BUNDLE_CACHE = new ConcurrentHashMap<>();  
  private static final ConcurrentHashMap<ArticleWorkflow_AST_CacheKey, ArticleWorkflow_AST> WK_CACHE = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<DialobForm_AST_CacheKey, DialobForm_AST> FORM_CACHE = new ConcurrentHashMap<>();
  
  private static final ConcurrentHashMap<Markdown_AST_CacheKey, Markdown_AST> MARKDOWN_CACHE = new ConcurrentHashMap<>();
  
  public static Markdown_AST computeIfAbsent(
      Markdown_AST_CacheKey key, 
      Function<Markdown_AST_CacheKey, Markdown_AST> mappingFunction) {
    
    return MARKDOWN_CACHE.computeIfAbsent(key, mappingFunction);
  }

  
  public static DialobForm_AST computeIfAbsent(
      DialobForm_AST_CacheKey key, 
      Function<DialobForm_AST_CacheKey, DialobForm_AST> mappingFunction) {
    
    return FORM_CACHE.computeIfAbsent(key, mappingFunction);
  }

  public static ArticleWorkflow_AST computeIfAbsent(
      ArticleWorkflow_AST_CacheKey key, 
      Function<ArticleWorkflow_AST_CacheKey, ArticleWorkflow_AST> mappingFunction) {
    
    return WK_CACHE.computeIfAbsent(key, mappingFunction);
  }
  
  public static Bundle computeIfAbsent( 
      Bundle_CacheKey key, 
      Function<Bundle_CacheKey, Bundle> mappingFunction) {

    return BUNDLE_CACHE.computeIfAbsent(key, mappingFunction);
  }
  

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
      if (!(o instanceof Flow_AST_CacheKey)) return false;
      return key.equals(((Flow_AST_CacheKey) o).key);
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
      if (!(o instanceof DecisionTable_AST_CacheKey)) return false;
      return key.equals(((DecisionTable_AST_CacheKey) o).key);
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
  
  
  public static class Bundle_CacheKey {
    private final String key;
    private final int hashCode;

    public Bundle_CacheKey(String input) {
      this.key = input != null ? input : "";
      this.hashCode = this.key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Bundle_CacheKey)) return false;
      return key.equals(((Bundle_CacheKey) o).key);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public String toString() {
      return "Bundle_CacheKey{" + key + "}";
    }
  }
  
  public static class ArticleWorkflow_AST_CacheKey {
    private final String key;
    private final int hashCode;

    public ArticleWorkflow_AST_CacheKey(String input) {
      this.key = input != null ? input : "";
      this.hashCode = this.key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof ArticleWorkflow_AST_CacheKey)) return false;
      return key.equals(((ArticleWorkflow_AST_CacheKey) o).key);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public String toString() {
      return "ArticleWorkflow_AST_CacheKey{" + key + "}";
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
  
  
  
  
  public static class DialobForm_AST_CacheKey {
    private final String key;
    private final int hashCode;

    public DialobForm_AST_CacheKey(String input) {
      this.key = input != null ? input : "";
      this.hashCode = this.key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof DialobForm_AST_CacheKey)) return false;
      return key.equals(((DialobForm_AST_CacheKey) o).key);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public String toString() {
      return "DialobForm_AST_CacheKey{" + key + "}";
    }
  }
  
  
  public static class Markdown_AST_CacheKey {
    private final String key;
    private final int hashCode;

    public Markdown_AST_CacheKey(String input) {
      this.key = input != null ? input : "";
      this.hashCode = this.key.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Markdown_AST_CacheKey)) return false;
      return key.equals(((Markdown_AST_CacheKey) o).key);
    }

    @Override
    public int hashCode() {
      return hashCode;
    }

    @Override
    public String toString() {
      return "Markdown_AST_CacheKey{" + key + "}";
    }
  }
  
  public static void flushAll() {
    EXPRESSION_CACHE.clear();
  }
}
