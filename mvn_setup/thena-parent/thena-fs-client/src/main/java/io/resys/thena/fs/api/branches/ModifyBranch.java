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

import java.util.function.BiConsumer;

import io.resys.thena.fs.api.branches.BranchBuilder.BeforeBranchCompletion;
import io.resys.thena.fs.entities.Ref;
import io.smallrye.mutiny.Uni;

/**
 * Builder for modifying existing branches in the file system.
 * Allows updating branch metadata, properties, permissions, and other configuration
 * while preserving the branch's commit history and references.
 * Supports conditional updates based on current branch state.
 */
public interface ModifyBranch {
  
  /**
   * Sets the branch that will be modified.
   * Can specify either the branch ID (hash) or the branch name for lookup.
   * 
   * @param branchId the identifier of the target branch (ID or name)
   * @return builder for method chaining
   */
  ModifyBranch branchId(String branchId);
  
  /**
   * Configures modifications to the existing branch using current state.
   * The system will load the current branch state within the transaction
   * and provide it to the consumer for conditional updates and validation.
   * 
   * @param branchBuilder lambda that receives current branch state and configures changes
   * @return builder for method chaining
   */
  ModifyBranch modifyBranch(BiConsumer<Ref, BranchBuilder> branchBuilder);
  
  /**
   * Registers a callback that executes before branch modification completion.
   * Allows for dynamic validation, error handling, default value setting,
   * and integration with external systems based on the loaded branch context.
   * 
   * @param callback lambda that receives loaded context and can modify the builder
   * @return builder for method chaining
   */
  ModifyBranch beforeBranchCompletion(BeforeBranchCompletion callback);
  
  /**
   * Executes the branch modification operation.
   * Updates the existing branch with the configured changes while
   * preserving its commit history and maintaining referential integrity.
   * 
   * @return reactive stream containing the branch modification result
   */
  Uni<BranchResult> build();
}
