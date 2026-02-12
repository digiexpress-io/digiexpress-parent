package io.resys.thena.fs.api.tags;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

import io.resys.thena.fs.api.tags.TagBuilder.BeforeTagCompletion;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

/**
 * Builder for creating tags that mark specific commits with metadata and scheduling information.
 * Supports both simple tagging and complex workflow integration with external systems.
 */
public interface NewTag {
  
  /**
   * Sets the commit that this tag points to.
   * The tag will mark this specific commit in the repository history.
   * 
   * @param commitIdOrBranchName the hash identifier of the target commit OR branch id OR branch name
   * @return builder for method chaining
   */
  NewTag commitId(String commitIdOrBranchName);
  
  /**
   * Create new tag based on commitId or branchName.
   * The system will query the current commit and branch state within the transaction
   * and provide it to the consumer for conditional updates.
   * 
   * @param tagBuilder lambda that receives current commit state and configures changes
   * @return builder for method chaining
   */
  NewTag newTag(Consumer<TagBuilder> tagBuilder);
  
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
}
