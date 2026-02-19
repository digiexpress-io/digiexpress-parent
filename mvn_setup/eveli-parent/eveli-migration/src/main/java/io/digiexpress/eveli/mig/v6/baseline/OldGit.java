package io.digiexpress.eveli.mig.v6.baseline;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Value;


public interface OldGit {
  
  Uni<OldGitObjects> findAll(String tenanPrefix);  

  @Value
  class OldGitObjects {
    String tenantPrefix;

    List<Branch> branches;
    List<Tag> tags;
    List<Tree> trees;

    Map<String, Blob> blobs;
    Map<String, Commit> commits;
  }

  @Value
  class Tree {
    String id;
    List<TreeValue> values;
  }

  @Value
  class TreeValue {
    String name;
    String blob;
    String tree;
  }

  @Value
  class Blob {
    String id;
    JsonObject value;
  }

  @Value
  class Commit {
    String id;
    String author;
    LocalDateTime datetime;
    String message;
    Optional<String> parent;
    Optional<String> merge;
    String tree;
  }

  @Value
  class Branch {
    String name;
    String commit;
  }

  @Value
  class Tag {
    String id;
    String commit;
    LocalDateTime datetime;
    String author;
    String message;
  }
}