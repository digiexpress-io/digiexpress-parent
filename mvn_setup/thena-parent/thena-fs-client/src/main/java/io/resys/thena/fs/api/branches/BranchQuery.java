package io.resys.thena.fs.api.branches;

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

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.entities.Ref;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Query interface for retrieving branch references with filtering capabilities.
 * Supports searching branches by name patterns and retrieving branch metadata.
 */
public interface BranchQuery {

  /**
   * Applies complex branch name filtering using expression builder patterns.
   * Supports operations like startsWith("feature/"), endsWith("-dev"), contains("hotfix"), etc.
   * 
   * @param nameExpr lambda that configures the name expression builder
   * @return query builder for method chaining
   */
  BranchQuery branchName(Consumer<NameExpressionBuilder> nameExpr);
  
  /**
   * Filters to a specific branch by its unique identifier.
   * Used for retrieving exact branch instances or checking branch existence.
   * 
   * @param branchId the unique branch identifier (same as branch name)
   * @return query builder for method chaining
   */
  BranchQuery branchId(String branchId);
  
  /**
   * Executes query that may return zero or one branch result.
   * Safe when unsure if any branches match the specified criteria.
   * 
   * @return reactive stream containing optional branch result
   */
  Uni<Optional<Ref>> findOne();
  
  /**
   * Executes query returning all matching branches as a reactive stream.
   * Efficient for processing multiple branches or when multiple results are expected.
   * 
   * @return reactive stream of all matching branch references
   */
  Multi<Ref> findAll();
  
  /**
   * Executes query expecting exactly one branch result.
   * Throws exception if zero or multiple branches match the criteria.
   * 
   * @return reactive stream containing the single matching branch
   */
  Uni<Ref> getOne();
}
