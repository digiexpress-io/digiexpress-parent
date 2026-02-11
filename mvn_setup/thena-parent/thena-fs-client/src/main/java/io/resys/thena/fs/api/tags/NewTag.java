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
import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.resys.thena.fs.entities.Tag;
import io.resys.thena.fs.entities.Tag.TagTransitives;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

/**
 * Builder for creating tags that mark specific commits with metadata and scheduling information.
 * Supports both simple tagging and complex workflow integration with external systems.
 */
public interface NewTag {
  
  /**
   * Sets the name identifier for this tag.
   * Must be unique within the repository to avoid conflicts.
   * 
   * @param tagName the unique tag name
   * @return builder for method chaining
   */
  NewTag tagName(String tagName);
  
  /**
   * Sets an optional description for the tag.
   * Provides human-readable context about the purpose or significance of this tag.
   * 
   * @param tagDescription descriptive text for the tag, null for no description
   * @return builder for method chaining
   */
  NewTag tagDescription(@Nullable String tagDescription);
  
  /**
   * Sets the commit that this tag points to.
   * The tag will mark this specific commit in the repository history.
   * 
   * @param commitId the hash identifier of the target commit
   * @return builder for method chaining
   */
  NewTag commitId(String commitId);
  
  /**
   * Sets the creation timestamp for this tag.
   * Used for audit trails and chronological ordering.
   * When null, the current system time will be used automatically.
   * 
   * @param tagCreatedAt the tag creation timestamp, null for current time
   * @return builder for method chaining
   */
  NewTag tagCreatedAt(@Nullable OffsetDateTime tagCreatedAt);
  
  /**
   * Sets the author who created this tag.
   * Used for accountability and audit purposes.
   * 
   * @param tagAuthor the tag creator identifier
   * @return builder for method chaining
   */
  NewTag tagAuthor(String tagAuthor);
  
  /**
   * Sets custom extension data for the tag.
   * Allows storing application-specific metadata or configuration.
   * 
   * @param tagExtension JSON object containing extension data, null for no extensions
   * @return builder for method chaining
   */
  NewTag tagExtension(@Nullable JsonObject tagExtension);
  
  /**
   * Sets error information associated with this tag.
   * Used to track failures or issues related to tag processing or workflows.
   * When null, indicates no errors occurred during tag processing.
   * 
   * @param tagErrors JSON object containing error details, null for no errors
   * @return builder for method chaining
   */
  NewTag tagErrors(@Nullable JsonObject tagErrors);
  
  /**
   * Sets an external system identifier for this tag.
   * Enables correlation with external workflow systems or databases.
   * 
   * @param externalId identifier from external system, null if not applicable
   * @return builder for method chaining
   */
  NewTag externalId(@Nullable String externalId);
  
  /**
   * Sets the external tenant identifier for this tag.
   * Used in multi-tenant scenarios to associate tags with external tenant systems.
   * 
   * @param externalTenantId external tenant identifier, null if not applicable
   * @return builder for method chaining
   */
  NewTag externalTenantId(@Nullable String externalTenantId);
  
  /**
   * Sets the scheduled start time for tag-related operations.
   * Used for delayed or scheduled processing of tagged commits.
   * 
   * @param tagStartsAt when tag processing should begin, null for immediate
   * @return builder for method chaining
   */
  NewTag tagStartsAt(@Nullable OffsetDateTime tagStartsAt);
  
  /**
   * Sets the processing report for this tag.
   * Contains results, metrics, or status information from tag-related workflows.
   * 
   * @param tagReport JSON object containing processing report, null if no report
   * @return builder for method chaining
   */
  NewTag tagReport(@Nullable JsonObject tagReport);
  
  /**
   * Registers a callback that executes before tag completion.
   * Allows for dynamic validation, error handling, and default value setting
   * based on the loaded tag context and related entities.
   * 
   * @param callback lambda that receives loaded context and can modify the builder
   * @return builder for method chaining
   */
  NewTag beforeTagCompletion(BeforeTagCompletion callback);
  
  /**
   * Executes the tag creation operation.
   * Creates the tag and associates it with the specified commit.
   * 
   * @return reactive stream containing the tag creation result
   */
  Uni<TagResult> build();
  
  /**
   * Callback interface for pre-completion tag processing.
   * Provides access to loaded tag context and allows builder modification.
   */
  @FunctionalInterface
  interface BeforeTagCompletion {
    /**
     * Applies custom logic before tag completion.
     * Can modify the builder based on the loaded tag context and related entities.
     * 
     * @param loaded the loaded tag context with related entities
     * @param builder the tag builder that can be modified
     */
    void apply(TagTransitives loaded, NewTag builder);
  }
  
  
  /**
   * Result envelope containing the outcome of a commit operation.
   * Includes the created commit, operation status, and any diagnostic messages.
   */
  @Value.Immutable
  interface TagResult extends ThenaEnvelope {
    /**
     * @return the tenant identifier where the tag was applied
     */
    String getTenantId();
    
    /**
     * @return the created Tag object, null if tagging failed
     */
    @Nullable Tag getTag();
    
    /**
     * @return the overall status of the tag operation
     */
    CommitResultStatus getStatus();
    
    /**
     * @return list of diagnostic messages from the tag process
     */
    List<Message> getMessages();
  }
}
