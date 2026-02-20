package io.digiexpress.eveli.mig.v6.baseline;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.resys.thena.api.entities.Tenant;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
  
  
  interface ExtractedNode {
    Commit getCommit();
    List<NodeOperation> getNodeOperations();
    Optional<ExtractedNode> getPrevious();
    Optional<ExtractedNode> getNext();
  }
  
  interface NodeOperation {
    default boolean isAdd() {
      return this instanceof AddNodeOperation;
    }
    default boolean isRm() {
      return this instanceof RmNodeOperation;
    }
    default boolean isMerge() {
      return this instanceof MergeNodeOperation;
    }
    
    default AddNodeOperation toAdd() {
      return (AddNodeOperation) this;
    }
    default RmNodeOperation toRm() {
      return (RmNodeOperation) this;
    }
    default MergeNodeOperation toMerge() {
      return (MergeNodeOperation) this;
    }
  }
  
  @Value
  static class AddNodeOperation implements NodeOperation {
    TreeValue added;
  }
  @Value
  static class RmNodeOperation implements NodeOperation {
    TreeValue removed;
  }
  @Value
  static class MergeNodeOperation implements NodeOperation {
    TreeValue before;
    TreeValue after;
  }
  
  @RequiredArgsConstructor
  @Getter
  static class TemplateNode implements ExtractedNode {
    private final Commit commit;
    private final List<NodeOperation> nodeOperations;
    private final Optional<ExtractedNode> previous;
    
    private Optional<ExtractedNode> next;
    
    
    public void add(Commit commit, List<NodeOperation> op) {
      if(this.commit.getId().equals(commit.getParent().get())) {
        this.next = Optional.of(new TemplateNode(commit, op, Optional.ofNullable(this)));
      } else {
        ((TemplateNode)this.next.get()).add(commit, op);
      }
    }
  }
}