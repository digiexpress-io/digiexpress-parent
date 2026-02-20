package io.digiexpress.eveli.mig.v6.assets;

import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.mig.v6.baseline.OldGit.Commit;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.TreeValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;

public interface ExtractedNode {
  ExtractedNodeType getType();
  Commit getCommit();
  List<NodeOperation> getNodeOperations();
  Optional<ExtractedNode> getPrevious();
  Optional<ExtractedNode> getNext();
  
  enum ExtractedNodeType {
    WRENCH, STENCIL
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
    private final ExtractedNodeType type;
    private final Commit commit;
    private final List<NodeOperation> nodeOperations;
    private final Optional<ExtractedNode> previous;
    
    private Optional<ExtractedNode> next = Optional.empty();
    
    
    public TemplateNode add(Commit commit, List<NodeOperation> op) {
      if(this.commit.getId().equals(commit.getParent().get())) {
        this.next = Optional.of(new TemplateNode(type, commit, op, Optional.ofNullable(this)));
      } else {
        ((TemplateNode)this.next.get()).add(commit, op);
      }
      
      return this;
    }
    public TemplateNode add(TemplateNode next) {
      this.next = Optional.of(next);
      return this;
    }
  }
}