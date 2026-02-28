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

import java.util.function.BiConsumer;

import io.resys.thena.fs.api.tags.TagBuilder.BeforeTagCompletion;
import io.resys.thena.fs.entities.Tag;
import io.smallrye.mutiny.Uni;

/**
 * Builder for modifying tags that mark specific commits with metadata and scheduling information.
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
   * Sets the author who modified this tag.
   * Used for accountability and audit purposes.
   * 
   * @param tagAuthor the tag author identifier
   * @return builder for method chaining
   */
  ModifyTag tagAuthor(String tagAuthor);
  
  /**
   * Executes the tag creation operation.
   * Creates the tag and associates it with the specified commit.
   * 
   * @return reactive stream containing the tag creation result
   */
  Uni<TagResult> build();
}
