package io.resys.thena.fs.api.tags;

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
import io.resys.thena.fs.entities.Tag;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

/**
 * Query interface for retrieving tags with configurable content loading.
 * Supports filtering by tag properties and controlling the depth of related data loading
 * to optimize performance based on use case requirements.
 */
public interface TagQuery {
  /**
   * Filters to a specific tag by its unique identifier.
   * Used for retrieving exact tag instances or checking tag existence.
   * 
   * @param id the unique tag identifier
   * @return query builder for method chaining
   */
  TagQuery tagId(String id);
  
  /**
   * Excludes blob content from the result set to reduce payload size.
   * When true, only tag, commit, tree, and node metadata is returned without file content.
   * 
   * @param excludeBlobs true to exclude blobs, false to include them
   * @return query builder for method chaining
   */
  TagQuery excludeBlobs(boolean excludeBlobs);
  
  /**
   * Excludes node information from the result set to reduce payload size.
   * When true, also automatically excludes blobs since nodes reference blob content.
   * Only tag, commit, and tree structure information is returned.
   * 
   * @param excludeNodes true to exclude nodes (and blobs), false to include them
   * @return query builder for method chaining
   */
  TagQuery excludeNodes(boolean excludeNodes);
  
  /**
   * Excludes tree structure from the result set to reduce payload size.
   * When true, also automatically excludes nodes and blobs since they depend on tree structure.
   * Only tag and commit information is returned.
   * 
   * @param excludeTrees true to exclude trees (nodes, and blobs), false to include them
   * @return query builder for method chaining
   */
  TagQuery excludeTrees(boolean excludeTrees);
  
  /**
   * Applies complex tag name filtering using expression builder patterns.
   * Supports operations like startsWith("v"), endsWith(".0"), contains("release"), etc.
   * 
   * @param nameExpr lambda that configures the name expression builder
   * @return query builder for method chaining
   */
  TagQuery tagName(Consumer<NameExpressionBuilder> nameExpr);
  
  /**
   * Executes query that may return zero or one tag result.
   * Safe when unsure if any tags match the specified criteria.
   * 
   * @return reactive stream containing optional tag result
   */
  Uni<Optional<Tag>> findOne();
  
  /**
   * Executes query expecting exactly one tag result.
   * Throws exception if zero or multiple tags match the criteria.
   * 
   * @return reactive stream containing the single matching tag
   */
  Uni<Tag> getOne();
  
  /**
   * Executes query returning all matching tags as a reactive stream.
   * Efficient for processing multiple tags or when multiple results are expected.
   * 
   * @return reactive stream of all matching tags
   */
  Multi<Tag> findAll();
}
