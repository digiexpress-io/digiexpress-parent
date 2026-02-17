package io.resys.thena.fs.api.commits;

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

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.api.trees.PathExpressionBuilder;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface CommitQuery {
  /**
   * Constrains query to a specific branch's commit history.
   * Searches through all commits that are reachable from the branch HEAD.
   * 
   * @param branchName the target branch to search within
   * @return query builder for method chaining
   */
  CommitQuery branchName(String branchName);
  
  /**
   * Filters commits that affected a specific file or folder.
   * Useful for tracking the modification history of particular filesystem nodes.
   * 
   * @param fileOrFolderId the unique identifier of the file or folder to track
   * @return query builder for method chaining
   */
  CommitQuery fileOrFolderId(String fileOrFolderId);
  
  /**
   * Applies complex path-based filtering to find commits affecting specific paths.
   * Supports operations like under("/src"), startsWith("/config"), etc.
   * 
   * @param pathExpr lambda that configures the path expression builder
   * @return query builder for method chaining
   */
  CommitQuery path(Consumer<PathExpressionBuilder> pathExpr);
  
  /**
   * Applies complex filename filtering to find commits affecting files with specific names.
   * Supports operations like endsWith(".java"), matches(regex), etc.
   * 
   * @param nameExpr lambda that configures the name expression builder
   * @return query builder for method chaining
   */
  CommitQuery name(Consumer<NameExpressionBuilder> nameExpr);
  
  /**
   * Excludes blob content from the result set to reduce payload size.
   * When true, only metadata is returned without actual file content.
   * 
   * @param excludeBlobs true to exclude blobs, false to include them
   * @return query builder for method chaining
   */
  CommitQuery excludeBlobs(boolean excludeBlobs);

  /**
   * Executes query that may return zero or one commit result.
   * Safe when unsure if any commits match the specified criteria.
   * 
   * @return reactive stream containing optional commit result with associated data
   */
  Uni<Optional<CommitsByObject>> findOne();
  
  /**
   * Executes query expecting exactly one commit result.
   * Throws exception if zero or multiple commits match the criteria.
   * 
   * @return reactive stream containing the single matching commit with associated data
   */
  Uni<CommitsByObject> getOne();
  
  /**
   * Executes query returning all matching commits as a reactive stream.
   * Efficient for processing commit history or when multiple results are expected.
   * 
   * @return reactive stream of all matching commits with their associated data
   */
  Multi<CommitsByObject> findAll();
  
  /**
   * Aggregated result containing a filesystem node and all commits that modified it,
   * along with the file content from each commit. Provides a complete view of how
   * a file or folder evolved over time.
   */
  
  interface CommitsByObject {
    // target object that was changed
    String getObjectId();
    
    /**
     * @return the filesystem node (file or folder) that was tracked
     */
    Map<String, Node> getNodesById();
    
    /**
     * @return map of commit ID to commit object for all commits that modified this node
     */
    Map<String, Commit> getCommitsById();
    
    /**
     * @return map of commit ID to file content for each commit (empty for folders)
     */
    Map<String, Blob> getBlobsByCommitId();
    
    /**
     * @return map of commit ID to node properties for each commit (may be empty if excluded)
     */
    Map<String, Props> getPropsByCommitId();
  }
}
