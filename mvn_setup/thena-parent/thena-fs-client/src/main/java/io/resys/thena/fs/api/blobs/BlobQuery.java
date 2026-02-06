package io.resys.thena.fs.api.blobs;

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.fs.api.trees.PathExpressionBuilder;
import io.resys.thena.fs.entities.Blob;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Nullable;

public interface BlobQuery {
  /**
   * Constrains query to files within a specific branch's current state.
   * When branch is specified, only files from the HEAD commit are included.
   * When branch is omitted, the default branch HEAD is used.
   * When explicitly set to null, queries across all files regardless of branch.
   * 
   * @param branch target branch name, null for cross-branch queries, omit for default branch
   * @return query builder for method chaining
   */
  BlobQuery branchName(@Nullable String branch); 
  
  /**
   * Filters to a specific file by its unique content hash identifier.
   * Useful for retrieving exact file versions or checking file existence.
   * 
   * @param id the SHA-1 hash of the file content
   * @return query builder for method chaining
   */
  BlobQuery blobId(String id);
  
  /**
   * Filters files by their MIME type or custom type classification.
   * Enables querying for specific file categories like images, documents, or custom types.
   * 
   * @param type the file type identifier (e.g., "text/plain", "image/jpeg", "config")
   * @return query builder for method chaining
   */
  BlobQuery blobType(String type);

  /**
   * Applies complex path-based filtering using expression builder patterns.
   * Supports operations like startsWith("/src"), contains("test"), depth constraints, etc.
   * 
   * @param pathExpr lambda that configures the path expression builder
   * @return query builder for method chaining
   */
  BlobQuery treePath(Consumer<PathExpressionBuilder> pathExpr);
  
  /**
   * Applies complex filename filtering using expression builder patterns.
   * Supports operations like endsWith(".java"), matches(regex), contains("test"), etc.
   * 
   * @param nameExpr lambda that configures the name expression builder
   * @return query builder for method chaining
   */
  BlobQuery treeName(Consumer<NameExpressionBuilder> nameExpr);

  /**
   * Executes query expecting exactly one result.
   * Throws exception if zero or multiple files match the criteria.
   * 
   * @return reactive stream containing the single matching file
   */
  Uni<Blob> getOne();
  
  /**
   * Executes query that may return zero or one result.
   * Safe alternative when unsure if file exists or criteria might match nothing.
   * 
   * @return reactive stream containing optional file result
   */
  Uni<Optional<Blob>> findOne();
  
  /**
   * Executes query returning all matching files as a reactive stream.
   * Efficient for processing large result sets or when multiple files are expected.
   * 
   * @return reactive stream of all matching files
   */
  Multi<Blob> findAll();
  
  


}