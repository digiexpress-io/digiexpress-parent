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

import io.resys.thena.fs.entities.Ref.RefTransitives;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

/**
 * Builder for creating and managing branch references with metadata and configuration.
 * Supports branch creation with descriptive information, access controls, and UI customization.
 */
public interface BranchBuilder {

  /**
   * Sets the name identifier for this branch.
   * Must be unique within the repository to avoid conflicts.
   * 
   * @param branchName the unique branch name
   * @return builder for method chaining
   */
  BranchBuilder branchName(String branchName);
  
  /**
   * Sets a descriptive explanation for the branch purpose.
   * Helps team members understand what this branch is for and its intended lifecycle.
   * 
   * @param branchDescription human-readable description, null for no description
   * @return builder for method chaining
   */
  BranchBuilder branchDescription(@Nullable String branchDescription);
  
  /**
   * Sets extension properties for UI configuration and custom behavior.
   * Can include display settings, workflow configurations, or application-specific metadata.
   * Examples: UI colors, default merge strategies, custom validation rules.
   * 
   * @param branchProps JSON object containing extension properties, null for no custom props
   * @return builder for method chaining
   */
  BranchBuilder branchProps(@Nullable JsonObject branchProps);
  
  /**
   * Sets access control and permission rules for branch operations.
   * Defines who can push, merge, delete, or perform other operations on this branch.
   * Enables fine-grained security management for sensitive branches.
   * 
   * @param branchPermissions JSON object containing permission settings, null for default permissions
   * @return builder for method chaining
   */
  BranchBuilder branchPermissions(@Nullable JsonObject branchPermissions);
  
  /**
   * Sets behavioral flags and feature toggles for branch-specific behavior.
   * Controls branch protection rules, automation settings, or special handling.
   * Examples: require reviews, auto-delete on merge, disable force push.
   * 
   * @param branchFlags JSON object containing behavioral flags, null for default behavior
   * @return builder for method chaining
   */
  BranchBuilder branchFlags(@Nullable JsonObject branchFlags);

  /**
   * Executes the branch creation or update operation.
   * Creates the branch reference with all configured metadata and settings.
   */
  void build();
  
  /**
   * Callback interface for pre-completion branch processing.
   * Provides access to loaded branch context and allows builder modification.
   */
  @FunctionalInterface
  interface BeforeBranchCompletion {
    /**
     * Applies custom logic before tag completion.
     * Can modify the builder based on the loaded tag context and related entities.
     * 
     * @param loaded the loaded branch context with related entities
     * @param builder the branch builder that can be modified
     */
    void apply(RefTransitives loaded, BranchBuilder builder);
  }
  
}
