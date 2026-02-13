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

import java.time.OffsetDateTime;
import java.util.function.Consumer;

import io.resys.thena.fs.api.branches.BranchBuilder.BeforeBranchCompletion;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

/**
 * Builder for creating new branches in the file system.
 * Branches provide isolated development environments that can diverge from and merge back to other branches.
 * Supports both simple branch creation and complex workflow integration with metadata and callbacks.
 */
public interface CreateBranch {

  /**
   * Sets the source commit or branch that this new branch will be created from.
   * The new branch will start at the same state as the specified commit or branch head.
   * 
   * @param commitIdOrBranchName the hash of the commit OR the name of an existing branch
   * @return builder for method chaining
   */
  CreateBranch commitIdOrBranchName(String commitIdOrBranchName);
  
  /**
   * Configures the properties of the new branch using a builder pattern.
   * The consumer receives a BranchBuilder that allows setting branch name, description,
   * metadata, permissions, and other branch-specific configuration.
   * 
   * @param branchBuilder lambda that receives a BranchBuilder and configures branch properties
   * @return builder for method chaining
   */
  CreateBranch newBranch(Consumer<BranchBuilder> branchBuilder);
  
  /**
   * Sets the creation timestamp for this branch.
   * Used for audit trails, chronological ordering, and temporal queries.
   * When null, the current system time will be used automatically.
   * 
   * @param branchCreatedAt the branch creation timestamp, null for current time
   * @return builder for method chaining
   */
  CreateBranch branchCreatedAt(@Nullable OffsetDateTime branchCreatedAt);
  
  /**
   * Registers a callback that executes before branch completion.
   * Allows for dynamic validation, error handling, default value setting,
   * and integration with external systems based on the loaded branch context.
   * 
   * @param callback lambda that receives loaded context and can modify the builder
   * @return builder for method chaining
   */
  CreateBranch beforeBranchCompletion(BeforeBranchCompletion callback);
  
  /**
   * Sets the identifier of who created this branch.
   * Used for accountability, notifications, and ownership tracking.
   * 
   * @param branchAuthor the creator's identifier, null if unknown or system-created
   * @return builder for method chaining
   */
  CreateBranch branchAuthor(@Nullable String branchAuthor);
  
  /**
   * Executes the branch creation operation.
   * Creates the new branch reference pointing to the specified commit and
   * initializes it with the configured properties and metadata.
   * 
   * @return reactive stream containing the branch creation result
   */
  Uni<BranchResult> build();
}
