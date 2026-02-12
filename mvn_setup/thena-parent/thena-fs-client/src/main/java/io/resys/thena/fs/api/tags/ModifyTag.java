package io.resys.thena.fs.api.tags;

import java.util.function.BiConsumer;

import io.resys.thena.fs.api.tags.TagBuilder.BeforeTagCompletion;
import io.resys.thena.fs.entities.Tag;
import io.smallrye.mutiny.Uni;

/**
 * Builder for creating tags that mark specific commits with metadata and scheduling information.
 * Supports both simple tagging and complex workflow integration with external systems.
 */
public interface ModifyTag {
  
  /**
   * Sets the tag that will be modified.
   * 
   * @param tagIdOrName the identifier of the target tag OR tag name
   * @return builder for method chaining
   */
  ModifyTag tagId(String tagIdOrName);
  
  /**
   * Modified excising tag based on tagName or tagId.
   * The system will query the current commit and branch state within the transaction
   * and provide it to the consumer for conditional updates.
   * 
   * @param tagBuilder lambda that receives current commit state and configures changes
   * @return builder for method chaining
   */
  ModifyTag modifyTag(BiConsumer<Tag, TagBuilder> tagBuilder);
  
  /**
   * Registers a callback that executes before tag completion.
   * Allows for dynamic validation, error handling, and default value setting
   * based on the loaded tag context and related entities.
   * 
   * @param callback lambda that receives loaded context and can modify the builder
   * @return builder for method chaining
   */
  ModifyTag beforeTagCompletion(BeforeTagCompletion callback);
  
  /**
   * Executes the tag creation operation.
   * Creates the tag and associates it with the specified commit.
   * 
   * @return reactive stream containing the tag creation result
   */
  Uni<TagResult> build();
}
