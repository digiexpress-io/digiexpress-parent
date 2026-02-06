package io.resys.thena.fs.api.trees;


/**
 * Fluent builder for constructing complex path filtering expressions.
 * Supports filesystem-aware operations with method chaining and logical operators.
 * Used in queries to filter files by their full path within the repository.
 */
public interface PathExpressionBuilder {
  /**
   * Matches files at exactly the specified path.
   * Case-sensitive exact path comparison.
   * 
   * @param path the exact file path to match
   * @return builder for method chaining
   */
  PathExpressionBuilder equals(String path);
  
  /**
   * Matches files using SQL LIKE pattern with wildcards on paths.
   * Supports '%' for any characters and '_' for single character.
   * 
   * @param pattern SQL LIKE pattern (e.g., "/src/%.java", "/config/_test")
   * @return builder for method chaining
   */
  PathExpressionBuilder like(String pattern);
  
  /**
   * Matches files whose paths begin with the specified prefix.
   * More readable alternative to like("prefix%").
   * 
   * @param prefix the required path prefix (e.g., "/src", "/config")
   * @return builder for method chaining
   */
  PathExpressionBuilder startsWith(String prefix);
  
  /**
   * Matches files whose paths end with the specified suffix.
   * Useful for filtering by path patterns or nested structures.
   * 
   * @param suffix the required path suffix
   * @return builder for method chaining
   */
  PathExpressionBuilder endsWith(String suffix);
  
  /**
   * Matches files whose paths contain the specified segment anywhere.
   * Case-sensitive substring search within the full path.
   * 
   * @param segment the required path segment (e.g., "test", "config")
   * @return builder for method chaining
   */
  PathExpressionBuilder contains(String segment);
  
  /**
   * Matches files that are located under the specified directory.
   * Ensures the path starts with the directory followed by a path separator.
   * More semantically correct than startsWith() for directory containment.
   * 
   * @param directory the parent directory path (e.g., "/src", "/config")
   * @return builder for method chaining
   */
  PathExpressionBuilder under(String directory);
  
  /**
   * Matches files at a specific depth level in the filesystem hierarchy.
   * Depth is counted by the number of path separators from root.
   * 
   * @param levels the exact depth level (0 for root, 1 for immediate children, etc.)
   * @return builder for method chaining
   */
  PathExpressionBuilder depth(int levels);
  
  /**
   * Matches files whose paths match the specified regular expression.
   * Provides maximum flexibility for complex path pattern matching.
   * 
   * @param regex the regular expression pattern
   * @return builder for method chaining
   */
  PathExpressionBuilder matches(String regex);
  
  /**
   * Matches files whose paths are in the specified set of values.
   * Efficient alternative to multiple equals() conditions with OR.
   * 
   * @param paths the set of acceptable file paths
   * @return builder for method chaining
   */
  PathExpressionBuilder in(String... paths);
  
  /**
   * Combines the current expression with the next using logical OR.
   * The next method call will be ORed with preceding conditions.
   * 
   * @return builder for method chaining
   */
  PathExpressionBuilder or();
  
  /**
   * Combines the current expression with the next using logical AND.
   * The next method call will be ANDed with preceding conditions.
   * 
   * @return builder for method chaining
   */
  PathExpressionBuilder and();
}