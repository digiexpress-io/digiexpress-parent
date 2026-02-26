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

import java.time.OffsetDateTime;
import java.util.function.Consumer;

import io.resys.thena.fs.api.tags.TagBuilder.BeforeTagCompletion;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

/**
 * Builder for creating tags that mark specific commits with metadata and scheduling information.
 * Supports both simple tagging and complex workflow integration with external systems.
 */
public interface CreateTag {
  
  /**
   * Sets the commit that this tag points to.
   * The tag will mark this specific commit in the repository history.
   * 
   * @param commitIdOrBranchName the hash identifier of the target commit OR branch id OR branch name
   * @return builder for method chaining
   */
  CreateTag commitId(String commitIdOrBranchName);
  
  /**
   * Create new tag based on commitId or branchName.
   * The system will query the current commit and branch state within the transaction
   * and provide it to the consumer for conditional updates.
   * 
   * @param tagBuilder lambda that receives current commit state and configures changes
   * @return builder for method chaining
   */
  CreateTag newTag(Consumer<TagBuilder> tagBuilder);
  
  /**
   * Sets the creation timestamp for this tag.
   * Used for audit trails and chronological ordering.
   * When null, the current system time will be used automatically.
   * 
   * @param tagCreatedAt the tag creation timestamp, null for current time
   * @return builder for method chaining
   */
  CreateTag tagCreatedAt(@Nullable OffsetDateTime tagCreatedAt);
  
  /**
   * Sets the author who created this tag.
   * Used for accountability and audit purposes.
   * 
   * @param tagAuthor the tag creator identifier
   * @return builder for method chaining
   */
  CreateTag tagAuthor(String tagAuthor);
  
  /**
   * Registers a callback that executes before tag completion.
   * Allows for dynamic validation, error handling, and default value setting
   * based on the loaded tag context and related entities.
   * 
   * @param callback lambda that receives loaded context and can modify the builder
   * @return builder for method chaining
   */
  CreateTag beforeTagCompletion(BeforeTagCompletion callback);
  
  /**
   * Executes the tag creation operation.
   * Creates the tag and associates it with the specified commit.
   * 
   * @return reactive stream containing the tag creation result
   */
  Uni<TagResult> build();
}
