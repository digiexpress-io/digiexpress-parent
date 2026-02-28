package io.digiexpress.eveli.mig.v6.assets;

import java.util.List;
import java.util.Optional;

import io.digiexpress.eveli.mig.v6.assets.AssetEvent.ObjectType;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.Commit;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.OldGitObjects;
import io.digiexpress.eveli.mig.v6.baseline.OldGit.TreeValue;
import lombok.Value;

public interface CommitNode {
  OldGitObjects getSrc();
  ObjectType getType();
  Commit getCommit();
  List<NodeOperation> getNodeOperations();
  Optional<CommitNode> getPrevious();
  Optional<CommitNode> getNext();
  
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
}