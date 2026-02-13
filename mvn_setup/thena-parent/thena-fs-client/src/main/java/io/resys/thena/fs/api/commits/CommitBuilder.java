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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.immutables.value.Value;

import io.resys.thena.api.envelope.CommitResultStatus;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

/**
 * Transactional builder for creating atomic filesystem commits with multiple operations.
 * Supports batching create, modify, and delete operations into a single commit.
 * All operations are executed within a transaction to prevent race conditions.
 */
public interface CommitBuilder {
  /**
   * Expected commit id on current branch head. 
   * 
   * @param commitId the hash of the parent commit, null(no locking) for branch HEAD
   * @return builder for method chaining
   */
  CommitBuilder branchLock(@Nullable String commitId);
  
  /**
   * Sets the target branch where this commit will be applied.
   * When specified, the commit updates the branch reference to point to the new commit.
   * 
   * @param branchName the branch to commit to, null for detached commit
   * @return builder for method chaining
   */
  CommitBuilder branchName(@Nullable String branchName);
  
  /**
   * Adds a new folder creation operation to this commit.
   * Multiple folders can be added by calling this method repeatedly.
   * 
   * @param doc lambda that configures the folder creation details
   * @return builder for method chaining
   */
  CommitBuilder newFolder(Consumer<NewFolder> doc);
  
  /**
   * Adds a folder modification operation to this commit.
   * The system will query the current folder state within the transaction
   * and provide it to the consumer for conditional updates.
   * 
   * @param docId the unique identifier of the existing folder to modify
   * @param doc lambda that receives current folder state and configures changes
   * @return builder for method chaining
   */
  CommitBuilder mergeFolder(String docId, BiConsumer<Node, MergeFolder> doc);
  
  /**
   * Adds a new file creation operation to this commit.
   * Multiple files can be added by calling this method repeatedly.
   * 
   * @param doc lambda that configures the file creation details
   * @return builder for method chaining
   */
  CommitBuilder newFile(Consumer<NewFile> doc);
  
  /**
   * Adds a file modification operation to this commit.
   * The system will query the current file state within the transaction
   * and provide it to the consumer for conditional updates.
   * 
   * @param docId the unique identifier of the existing file to modify
   * @param doc lambda that receives current file state and configures changes
   * @return builder for method chaining
   */
  CommitBuilder mergeFile(String docId, BiConsumer<Node, MergeFile> doc);
  
  /**
   * Adds a single item deletion operation to this commit.
   * The item (file or folder) will be removed from the filesystem.
   * 
   * @param docId the unique identifier of the item to remove
   * @return builder for method chaining
   */
  CommitBuilder remove(String docId);
  
  /**
   * Adds multiple item deletion operations to this commit.
   * All specified items will be removed from the filesystem.
   * 
   * @param docId list of unique identifiers for items to remove
   * @return builder for method chaining
   */
  CommitBuilder remove(List<String> docId);
  
  /**
   * Sets the author information for this commit.
   * Used for audit trails and commit history attribution.
   * 
   * @param author the commit author identifier
   * @return builder for method chaining
   */
  CommitBuilder commitAuthor(String author);
  
  /**
   * Sets the descriptive message for this commit.
   * Should explain the purpose and scope of the changes being made.
   * 
   * @param message the commit description
   * @return builder for method chaining
   */
  CommitBuilder commitMessage(String message);
  
  /**
   * Sets the creation timestamp for this commit.
   * When null, the system will use the current timestamp.
   * 
   * @param createdAt the commit creation timestamp, null for current time
   * @return builder for method chaining
   */
  CommitBuilder commitCreatedAt(@Nullable OffsetDateTime createdAt);
  
  /**
   * Executes all configured operations as a single atomic transaction.
   * Creates the commit and updates any specified branch references.
   * 
   * @return reactive stream containing the commit result with status and messages
   */
  Uni<CommitResult> build();
  
  /**
   * Builder for creating a new folder within the commit transaction.
   * Handles the creation of directory nodes with optional metadata.
   */
  interface NewFolder {
    /**
     * Sets the filesystem path where the folder will be created.
     * Must be a valid path within the repository structure.
     * 
     * @param path the target path for the new folder
     * @return builder for method chaining
     */
    NewFolder folderPath(String path);
    
    /**
     * Sets the filename (basename) for the file.
     * Combined with the path to determine the full file location.
     * 
     * @param path the target path for the new folder
     * @return builder for method chaining
     */
    NewFolder folderName(String folderName);
    
    /**
     * Sets a custom identifier for the folder.
     * When null, the system will auto-generate a unique identifier.
     * 
     * @param folderId custom folder identifier, null for auto-generation
     * @return builder for method chaining
     */
    NewFolder folderId(@Nullable String folderId);
    
    /**
     * Configures additional properties for the folder.
     * Allows setting labels, permissions, comments, and flags.
     * 
     * @param props lambda that configures the folder properties
     * @return builder for method chaining
     */
    NewFolder folderProps(Consumer<PropsBuilder> props);
    
    /**
     * Completes the folder creation configuration and adds it to the commit.
     * This is a terminal operation that cannot be undone.
     */
    void build();
  }
  
  /**
   * Builder for modifying an existing folder within the commit transaction.
   * Provides access to current folder state for conditional updates.
   */
  interface MergeFolder {
    /**
     * Updates the filesystem path of the existing folder.
     * Effectively moves the folder to a new location.
     * 
     * @param path the new path for the folder
     * @return builder for method chaining
     */
    MergeFolder folderPath(String path);
    
    /**
     * Updates the foldername (basename).
     * Combined with the path to determine the full file location.
     * 
     * @param path the target path for the new folder
     * @return builder for method chaining
     */
    MergeFolder folderName(String folderName);
    
    /**
     * Updates the folder properties with access to current state.
     * The BiConsumer receives both current properties and a builder for updates.
     * Enables conditional modifications based on existing values.
     * 
     * @param props lambda that receives current props and configures updates
     * @return builder for method chaining
     */
    MergeFolder folderProps(BiConsumer<Optional<Props>, PropsBuilder> props);
    
    /**
     * Completes the folder modification configuration and adds it to the commit.
     * This is a terminal operation that cannot be undone.
     */
    void build();
  }
  
  /**
   * Builder for creating a new file within the commit transaction.
   * Handles the creation of file nodes with content and metadata.
   */
  interface NewFile {
    /**
     * Configures additional properties for the file.
     * Allows setting labels, permissions, comments, and flags.
     * 
     * @param props lambda that configures the file properties
     * @return builder for method chaining
     */
    NewFile fileProps(Consumer<PropsBuilder> props);
    
    /**
     * Sets the file content as a JSON object.
     * The content will be stored as a blob and referenced by its hash.
     * 
     * @param blob the file content in JSON format
     * @return builder for method chaining
     */
    NewFile fileValue(JsonObject blob);
    
    /**
     * Sets the MIME type or custom type classification for the file.
     * Used for content-based filtering and processing logic.
     * 
     * @param type the file type identifier (e.g., "text/plain", "application/json")
     * @return builder for method chaining
     */
    NewFile fileType(String type);
    
    /**
     * Sets the filename (basename) for the file.
     * Combined with the path to determine the full file location.
     * 
     * @param name the filename including extension
     * @return builder for method chaining
     */
    NewFile fileName(String name);
    
    /**
     * Sets a custom identifier for the file.
     * When null, the system will auto-generate a unique identifier.
     * 
     * @param fileId custom file identifier, null for auto-generation
     * @return builder for method chaining
     */
    NewFile fileId(@Nullable String fileId);
    
    /**
     * Sets the directory path where the file will be created.
     * When null, the file is created in the repository root.
     * 
     * @param path the directory path, null for root directory
     * @return builder for method chaining
     */
    NewFile filePath(@Nullable String path);
    
    /**
     * Completes the file creation configuration and adds it to the commit.
     * This is a terminal operation that cannot be undone.
     */
    void build();
  }
  
  /**
   * Builder for modifying an existing file within the commit transaction.
   * Provides access to current file state for conditional updates.
   */
  interface MergeFile {
    /**
     * Updates the file content with new JSON data.
     * Creates a new blob with the updated content.
     * 
     * @param blob the new file content in JSON format
     * @return builder for method chaining
     */
    MergeFile fileValue(JsonObject blob);
    
    /**
     * Updates the filename of the existing file.
     * Effectively renames the file within its current directory.
     * 
     * @param name the new filename including extension
     * @return builder for method chaining
     */
    MergeFile fileName(String name);
    
    /**
     * Updates the directory path of the existing file.
     * When null, moves the file to the repository root.
     * 
     * @param path the new directory path, null for root directory
     * @return builder for method chaining
     */
    MergeFile filePath(@Nullable String path);
    
    /**
     * Updates the file properties with access to current state.
     * The BiConsumer receives both current properties and a builder for updates.
     * Enables conditional modifications based on existing values.
     * 
     * @param props lambda that receives current props and configures updates
     * @return builder for method chaining
     */
    MergeFile fileProps(BiConsumer<Optional<Props>, PropsBuilder> props);
    
    /**
     * Completes the file modification configuration and adds it to the commit.
     * This is a terminal operation that cannot be undone.
     */
    void build();
  }
  
  /**
   * Builder for configuring additional properties on files and folders.
   * Provides structured metadata storage for custom application needs.
   */
  interface PropsBuilder {
    /**
     * Sets classification labels for the item.
     * Used for categorization, tagging, and organizational purposes.
     * 
     * @param labels JSON object containing label key-value pairs, null to clear
     * @return builder for method chaining
     */
    PropsBuilder propsLabels(@Nullable JsonObject labels);
    
    /**
     * Sets documentation or descriptive comments for the item.
     * Useful for storing user annotations, descriptions, or notes.
     * 
     * @param comments JSON object containing comment data, null to clear
     * @return builder for method chaining
     */
    PropsBuilder propsComments(@Nullable JsonObject comments);
    
    /**
     * Sets access control and permission information for the item.
     * Enables fine-grained security and access management.
     * 
     * @param permissions JSON object containing permission rules, null to clear
     * @return builder for method chaining
     */
    PropsBuilder propsPermissions(@Nullable JsonObject permissions);
    
    /**
     * Sets behavioral flags and feature toggles for the item.
     * Used for controlling application behavior or marking special states.
     * 
     * @param flags JSON object containing flag settings, null to clear
     * @return builder for method chaining
     */
    PropsBuilder propsFlags(@Nullable JsonObject flags);
    
    /**
     * Completes the properties configuration and applies them to the item.
     * This is a terminal operation that cannot be undone.
     */
    void build();
  }
  
  /**
   * Result envelope containing the outcome of a commit operation.
   * Includes the created commit, operation status, and any diagnostic messages.
   */
  @Value.Immutable
  interface CommitResult extends ThenaEnvelope {
    /**
     * @return the tenant identifier where the commit was applied
     */
    String getTenantId();
    
    /**
     * @return the created commit object, null if commit failed
     */
    @Nullable Commit getCommit();
    
    /**
     * @return the overall status of the commit operation
     */
    CommitResultStatus getStatus();
    
    /**
     * @return git style log of what was added/merged/deleted, only when enabled using {@link LogConstants.SHOW_COMMIT)}
     */
    @Nullable String getLog();
    
    /**
     * @return list of diagnostic messages from the commit process
     */
    List<Message> getMessages();
  }
}
