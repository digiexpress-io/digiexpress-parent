package io.digiexpress.eveli.mig.v6.baseline;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Value;


public interface OldGit {
  
  Uni<OldGitObjects> findAll(String tenanPrefix);  

  @Value
  static class OldGitObjects {
    Tenant tenant;

    List<Branch> branches;
    List<Tag> tags;
    Map<String, Tree> trees;
    Map<String, Blob> blobs;
    Map<String, Commit> commits;
    
    
    public Blob getBlob(String commitId, String objectId) {
      
      final var commit = this.getCommits().get(commitId);
      final var tree = this.getTrees().get(commit.getTree());
      final var treeValue = tree.getValues().get(objectId);
      final var blob = RepoAssert.notNull(this.getBlobs().get(treeValue.getBlob()), () -> "Can't find blob");
      return blob;
    }
  }

  @Value
  static class Tree {
    String id;
    Map<String, TreeValue> values;
  }

  @Value
  static class TreeValue {
    String name;
    String blob;
    String tree;
  }

  @Value
  static class Blob {
    String id;
    JsonObject value;
  }

  @Value
  static class Commit {
    String id;
    String author;
    LocalDateTime datetime;
    String message;
    Optional<String> parent;
    Optional<String> merge;
    String tree;
  }

  @Value
  static class Branch {
    String name;
    String commit;
  }

  @Value
  static class Tag {
    String id;
    String commit;
    LocalDateTime datetime;
    String author;
    String message;
  }
}