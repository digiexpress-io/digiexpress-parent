package io.resys.thena.api.exceptions;

import java.util.List;

import io.resys.thena.api.models.ImmutableMessage;
import io.resys.thena.api.models.Message;
import io.resys.thena.api.models.Repo;


public class RepoException extends DocDBException {
  private static final long serialVersionUID = 4311634600357697485L;

  public RepoException(String msg) {
    super(msg);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    public Message notRepoWithName(String repoId) {
      final var text = new StringBuilder()
          .append("Repo with name: '").append(repoId).append("' does not exist!")
          .toString();
      return ImmutableMessage.builder()
            .text(text)
          .build();
    }
    public Message notRepoWithName(String repoId, List<Repo> others) {
      final var text = new StringBuilder()
          .append("Repo with name: '").append(repoId).append("' does not exist!")
          .append(" known repos: '").append(String.join(",", others.stream().map(r -> r.getName()).toList())).append("'")
          .toString();
      return ImmutableMessage.builder()
            .text(text)
          .build();
    }
    public Message noRepoRef(String repo, String ref) {
      return ImmutableMessage.builder()
            .text(new StringBuilder()
            .append("Repo with name: '").append(repo).append("',")
            .append(" has no ref: '").append(ref).append("'")
            .append("!")
            .toString())
          .build();
    }
    public Message noRepoRef(String repo, String ref, List<String> allRefs) {
      return ImmutableMessage.builder()
            .text(new StringBuilder()
            .append("Repo with name: '").append(repo).append("',")
            .append(" has no ref: '").append(ref).append("'")
            .append(" known refs: '").append(String.join(",", allRefs)).append("'")
            .append("!")
            .toString())
          .build();
    }
    public Message nameNotUnique(String name, String id) {
      return ImmutableMessage.builder()
            .text(new StringBuilder()
            .append("Repo with name: '").append(name).append("' already exists,")
            .append(" id: '").append(id).append("'")
            .append("!")
            .toString())
          .build();
    }
    public String updateConflict(String id, String dbRev, String userRev, String name) {
      return new StringBuilder()
          .append("Repo with")
          .append(" id: '").append(id).append("'")
          .append(" name: '").append(name).append("'")
          .append(" can't be updated")
          .append(" because of revision conflict")
          .append(" '").append(dbRev).append("' (db) != (user) '").append(userRev).append("'")
          .append("!")
          .toString();
    }
    
    
    public Message noCommit(Repo repo, String refCriteria) {
      return ImmutableMessage.builder()
        .text(new StringBuilder()
        .append("Repo with name: '").append(repo.getName()).append("'")
        .append(" does not contain: tag, ref or commit with id:")
        .append(" '").append(refCriteria).append("'")
        .toString())
        .build();
    }
    
    public Message noBlob(Repo repo, String tree, String refCriteria, String ...blobName) {
      return ImmutableMessage.builder()
        .text(new StringBuilder()
        .append("Repo with name: '").append(repo.getName()).append("'")
        .append(", tag, ref or commit with id: ").append(" '").append(refCriteria).append("'")
        .append(" and tree: '").append(tree).append("'")
        .append(" does not contain a blob with name: ").append("'").append(String.join(",", blobName)).append("'").append("!")
        .toString())
        .build();
    }
  }
}
