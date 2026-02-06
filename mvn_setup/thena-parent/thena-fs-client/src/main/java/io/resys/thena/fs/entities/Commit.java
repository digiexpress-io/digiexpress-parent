package io.resys.thena.fs.entities;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import jakarta.annotation.Nullable;

@Value.Immutable
@JsonSerialize(as = ImmutableCommit.class)
@JsonDeserialize(as = ImmutableCommit.class)
public interface Commit extends FileSystemEntity {
  
  String getId();
  OffsetDateTime getCommitCreatedAt();
  String getCommitAuthor();
  String getCommitMessage();
  String getTreeId();
  Optional<String> getParentId();
  Optional<String> getMergeId();

  @Value.Auxiliary
  @Nullable 
  CommitTransitives getTransitives();

  @Override
  default FileSystemEntityType getDocType() { 
    return FileSystemEntityType.COMMIT; 
  }

  @Value.Immutable
  @JsonSerialize(as = ImmutableCommitTransitives.class)
  @JsonDeserialize(as = ImmutableCommitTransitives.class)
  interface CommitTransitives {
    Optional<OffsetDateTime> getParentCreatedAt();
    Optional<OffsetDateTime> getMergeCreatedAt();
  }
}