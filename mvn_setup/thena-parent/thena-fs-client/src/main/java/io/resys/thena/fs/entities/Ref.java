package io.resys.thena.fs.entities;

import java.util.Map;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.vertx.core.json.JsonObject;
import jakarta.annotation.Nullable;

/**
 * Branch reference entity representing a named pointer to a specific commit.
 * Extended with metadata to support branch-specific configuration and documentation.
 */
@Value.Immutable
@JsonSerialize(as = ImmutableRef.class)
@JsonDeserialize(as = ImmutableRef.class)
public interface Ref extends FileSystemEntity {
  
  /**
   * @return the unique branch name identifier
   */
  String getRefName();
  
  /**
   * @return the commit hash that this branch currently points to
   */
  String getCommitId();
  
  /**
   * @return optional description explaining the purpose of this branch
   */
  Optional<String> getBranchDescription();
  
  /**
   * @return optional extension properties for UI configuration and custom behavior
   */
  Optional<JsonObject> getBranchProps();
  
  /**
   * @return optional permission settings controlling branch access and operations
   */
  Optional<JsonObject> getBranchPermissions();
  
  /**
   * @return optional behavioral flags for branch-specific feature toggles
   */
  Optional<JsonObject> getBranchFlags();
  
  /**
   * @return optional author identifier who created this branch
   */
  Optional<String> getBranchAuthor();

  @Value.Auxiliary
  @Nullable 
  RefTransitives getTransitives();

  @Override
  default String getId() {
    return getRefName();
  }

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.REF; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableRefTransitives.class)
  @JsonDeserialize(as = ImmutableRefTransitives.class)
  interface RefTransitives {
    Commit getCommit();
    
    // might not be loaded
    @Nullable Tree getTree();

    // might not be loaded
    @Nullable Map<String, Blob> getBlobsById();  
  }
}