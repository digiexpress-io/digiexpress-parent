package io.resys.thena.fs.api.trees;

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



/**
 * Fluent builder for constructing complex filename filtering expressions.
 * Supports SQL-like operations with method chaining and logical operators.
 * Used in queries to filter files by their basename (without path).
 */
public interface NameExpressionBuilder {
  /**
   * Matches files with exactly the specified name.
   * Case-sensitive exact string comparison.
   * 
   * @param name the exact filename to match
   * @return builder for method chaining
   */
  NameExpressionBuilder equals(String name);
  
  /**
   * Matches files using SQL LIKE pattern with wildcards.
   * Supports '%' for any characters and '_' for single character.
   * 
   * @param pattern SQL LIKE pattern (e.g., "%.java", "test_*")
   * @return builder for method chaining
   */
  NameExpressionBuilder like(String pattern);
  
  /**
   * Matches files whose names begin with the specified prefix.
   * More readable alternative to like("prefix%").
   * 
   * @param prefix the required filename prefix
   * @return builder for method chaining
   */
  NameExpressionBuilder startsWith(String prefix);
  
  /**
   * Matches files whose names end with the specified suffix.
   * Commonly used for file extension filtering.
   * 
   * @param suffix the required filename suffix (e.g., ".java", "Test.js")
   * @return builder for method chaining
   */
  NameExpressionBuilder endsWith(String suffix);
  
  /**
   * Matches files whose names contain the specified substring anywhere.
   * Case-sensitive substring search.
   * 
   * @param substring the required substring within the filename
   * @return builder for method chaining
   */
  NameExpressionBuilder contains(String substring);
  
  /**
   * Matches files whose names match the specified regular expression.
   * Provides maximum flexibility for complex pattern matching.
   * 
   * @param regex the regular expression pattern
   * @return builder for method chaining
   */
  NameExpressionBuilder matches(String regex);
  
  /**
   * Matches files whose names are in the specified set of values.
   * Efficient alternative to multiple equals() conditions with OR.
   * 
   * @param names the set of acceptable filenames
   * @return builder for method chaining
   */
  NameExpressionBuilder in(String... names);
  
  /**
   * Combines the current expression with the next using logical OR.
   * The next method call will be ORed with preceding conditions.
   * 
   * @return builder for method chaining
   */
  NameExpressionBuilder or();
  
  /**
   * Combines the current expression with the next using logical AND.
   * The next method call will be ANDed with preceding conditions.
   * 
   * @return builder for method chaining
   */
  NameExpressionBuilder and();
  
  /**
   * @return convenient way to check if expression is empty
   */
  boolean isEmpty();
}
