package io.resys.thena.fs.tables.filters;

import java.util.function.Function;

import io.resys.thena.fs.api.trees.PathExpressionBuilder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PathExpressionBuilderImpl implements PathExpressionBuilder {

  // sql column name that will be used for all the where sections
  private final String sqlColumnName;
  
  // allocate number by giving sql parameter
  private final Function<Object, Integer> sqlProps;
  
  // target stream into what to append the sql fragment
  private final StringBuilder sqlBuilder;
  
  private boolean hasConditions = false;
  
  @Override
  public PathExpressionBuilder equals(String path) {
    final int paramPos = sqlProps.apply(path);
    appendCondition(sqlColumnName + " = $" + paramPos);
    return this;
  }
  
  @Override
  public PathExpressionBuilder like(String pattern) {
    final int paramPos = sqlProps.apply(pattern);
    appendCondition(sqlColumnName + " LIKE $" + paramPos);
    return this;
  }
  
  @Override
  public PathExpressionBuilder startsWith(String prefix) {
    final int paramPos = sqlProps.apply(prefix + "%");
    appendCondition(sqlColumnName + " LIKE $" + paramPos);
    return this;
  }
  
  @Override
  public PathExpressionBuilder endsWith(String suffix) {
    final int paramPos = sqlProps.apply("%" + suffix);
    appendCondition(sqlColumnName + " LIKE $" + paramPos);
    return this;
  }
  
  @Override
  public PathExpressionBuilder contains(String segment) {
    final int paramPos = sqlProps.apply("%" + segment + "%");
    appendCondition(sqlColumnName + " LIKE $" + paramPos);
    return this;
  }
  
  @Override
  public PathExpressionBuilder under(String directory) {
    // Ensure directory path ends with / for proper containment check
    final String dirPath = directory.endsWith("/") ? directory : directory + "/";
    final int paramPos = sqlProps.apply(dirPath + "%");
    appendCondition(sqlColumnName + " LIKE $" + paramPos);
    return this;
  }
  
  @Override
  public PathExpressionBuilder depth(int levels) {
    // Count forward slashes to determine depth
    // depth 0 = no slashes, depth 1 = one slash, etc.
    final StringBuilder depthCondition = new StringBuilder();
    depthCondition.append("(LENGTH(").append(sqlColumnName).append(") - LENGTH(REPLACE(").append(sqlColumnName).append(", '/', ''))) = ");
    final int paramPos = sqlProps.apply(levels);
    depthCondition.append("$").append(paramPos);
    
    appendCondition(depthCondition.toString());
    return this;
  }
  
  @Override
  public PathExpressionBuilder matches(String regex) {
    final int paramPos = sqlProps.apply(regex);
    appendCondition(sqlColumnName + " ~ $" + paramPos);
    return this;
  }
  
  @Override
  public PathExpressionBuilder in(String... paths) {
    if (paths.length == 0) return this;
    
    final StringBuilder inClause = new StringBuilder(sqlColumnName + " IN (");
    for (int i = 0; i < paths.length; i++) {
      if (i > 0) inClause.append(", ");
      final int paramPos = sqlProps.apply(paths[i]);
      inClause.append("$").append(paramPos);
    }
    inClause.append(")");
    
    appendCondition(inClause.toString());
    return this;
  }
  
  @Override
  public PathExpressionBuilder or() {
    if (hasConditions) {
      sqlBuilder.append(" OR ");
    }
    return this;
  }
  
  @Override
  public PathExpressionBuilder and() {
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