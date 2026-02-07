package io.resys.thena.fs.entities;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.hash.Hashing;

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

  // H(commit) = μ(H(tree) ⊕ H(parent) ⊕ H(merge) ⊕ author ⊕ timestamp ⊕ message)
  public static ImmutableCommit.Builder newInstance(
      String treeId, 
      Optional<String> parentId, 
      Optional<String> mergeId, 
      String author, 
      OffsetDateTime createdAt, 
      String message) {
    
    final var content = new StringBuilder();
    content.append("tree ").append(treeId);
    if (parentId.isPresent()) {
      content.append("parent ").append(parentId.get());
    }
    if (mergeId.isPresent()) {
      content.append("merge ").append(mergeId.get());
    }
    content.append("author ").append(author).append(" ").append(createdAt.toEpochSecond());
    content.append("committer ").append(author).append(" ").append(createdAt.toEpochSecond());
    content.append(message);
    
    final var hash = Hashing.murmur3_128().hashString(content.toString(), StandardCharsets.UTF_8).toString();
    return ImmutableCommit.builder()
        .id(hash)
        .treeId(treeId)
        .parentId(parentId)
        .mergeId(mergeId)
        .commitAuthor(author)
        .commitCreatedAt(createdAt)
        .commitMessage(message);
  }
}