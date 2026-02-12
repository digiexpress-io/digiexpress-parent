package io.resys.thena.fs.api.tags;

import java.util.function.Consumer;

import io.smallrye.mutiny.Uni;

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
   * Executes the tag creation operation.
   * Creates the tag and associates it with the specified commit.
   * 
   * @return reactive stream containing the tag creation result
   */
  Uni<TagResult> build();
}
