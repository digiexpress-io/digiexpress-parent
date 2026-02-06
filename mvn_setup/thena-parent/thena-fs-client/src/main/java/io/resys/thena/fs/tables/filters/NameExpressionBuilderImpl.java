package io.resys.thena.fs.tables.filters;

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
  
  private boolean hasConditions = false;
  
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
    if (hasConditions) {
      sqlBuilder.append(" OR ");
    }
    return this;
  }
  
  @Override
  public NameExpressionBuilder and() {
    if (hasConditions) {
      sqlBuilder.append(" AND ");
    }
    return this;
  }
  
  private void appendCondition(final String condition) {
    if (hasConditions && !endsWithLogicalOperator()) {
      sqlBuilder.append(" AND ");
    }
    sqlBuilder.append(condition);
    hasConditions = true;
  }
  
  private boolean endsWithLogicalOperator() {
    final String current = sqlBuilder.toString();
    return current.endsWith(" OR ") || current.endsWith(" AND ");
  }
  
  public void close() {
    // Wrap in parentheses if we have conditions
    if (hasConditions) {
      final String content = sqlBuilder.toString();
      sqlBuilder.setLength(0);
      sqlBuilder.append("(").append(content).append(")");
    }
  }
}
