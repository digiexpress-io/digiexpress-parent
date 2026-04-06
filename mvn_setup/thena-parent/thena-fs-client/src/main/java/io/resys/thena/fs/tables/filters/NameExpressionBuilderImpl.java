package io.resys.thena.fs.tables.filters;

/*-
 * #%L
 * thena-fs-client
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

import java.util.function.Function;

import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NameExpressionBuilderImpl implements NameExpressionBuilder {

  // sql column name that will be used for all the where sections
  private final String sqlColumnName;
  
  // allocate number by giving sql parameter
  private final Function<Object, Integer> sqlProps;
  
  // target stream into what to append the sql fragment
  private final StringBuilder sqlBuilder;
  
  private boolean isAnyConditionDefined = false;
  
  @Override
  public NameExpressionBuilder equals(String name) {
    final int paramPos = sqlProps.apply(name);
    appendCondition(sqlColumnName + " = $" + paramPos);
    return this;
  }
  
  @Override
  public NameExpressionBuilder like(String pattern) {
    final int paramPos = sqlProps.apply(pattern);
    appendCondition(sqlColumnName + " LIKE $" + paramPos);
    return this;
  }
  
  @Override
  public NameExpressionBuilder startsWith(String prefix) {
    final int paramPos = sqlProps.apply(prefix + "%");
    appendCondition(sqlColumnName + " LIKE $" + paramPos);
    return this;
  }
  
  @Override
  public NameExpressionBuilder endsWith(String suffix) {
    final int paramPos = sqlProps.apply("%" + suffix);
    appendCondition(sqlColumnName + " LIKE $" + paramPos);
    return this;
  }
  
  @Override
  public NameExpressionBuilder contains(String substring) {
    final int paramPos = sqlProps.apply("%" + substring + "%");
    appendCondition(sqlColumnName + " LIKE $" + paramPos);
    return this;
  }
  
  @Override
  public NameExpressionBuilder matches(String regex) {
    final int paramPos = sqlProps.apply(regex);
    appendCondition(sqlColumnName + " ~ $" + paramPos);
    return this;
  }
  
  @Override
  public NameExpressionBuilder in(String... names) {
    if (names.length == 0) return this;
    
    final StringBuilder inClause = new StringBuilder(sqlColumnName + " IN (");
    for (int i = 0; i < names.length; i++) {
      if (i > 0) inClause.append(", ");
      final int paramPos = sqlProps.apply(names[i]);
      inClause.append("$").append(paramPos);
    }
    inClause.append(")");
    
    appendCondition(inClause.toString());
    return this;
  }
  
  @Override
  public NameExpressionBuilder or() {
    if (isAnyConditionDefined) {
      sqlBuilder.append(" OR ");
    }
    return this;
  }
  
  @Override
  public NameExpressionBuilder and() {
    if (isAnyConditionDefined) {
      sqlBuilder.append(" AND ");
    }
    return this;
  }
  
  private void appendCondition(final String condition) {
    if (isAnyConditionDefined && !endsWithLogicalOperator()) {
      sqlBuilder.append(" AND ");
    }
    sqlBuilder.append(condition);
    isAnyConditionDefined = true;
  }
  
  private boolean endsWithLogicalOperator() {
    final String current = sqlBuilder.toString();
    return current.endsWith(" OR ") || current.endsWith(" AND ");
  }
  
  public void close() {
    // Wrap in parentheses if we have conditions
    if (isAnyConditionDefined) {
      final String content = sqlBuilder.toString();
      sqlBuilder.setLength(0);
      sqlBuilder.append("(").append(content).append(")");
    }
  }

  @Override
  public boolean isEmpty() {
    return !isAnyConditionDefined;
  }
}
