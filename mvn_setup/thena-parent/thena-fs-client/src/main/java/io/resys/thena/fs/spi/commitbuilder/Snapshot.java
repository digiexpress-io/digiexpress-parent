package io.resys.thena.fs.spi.commitbuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFile;
import io.resys.thena.fs.api.commits.CommitBuilder.MergeFolder;
import io.resys.thena.fs.api.commits.CommitBuilder.NewFile;
import io.resys.thena.fs.api.commits.CommitBuilder.NewFolder;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.ImmutableCommit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.tables.FsDbBuilder.PersistenceUnit;
import io.resys.thena.fs.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class Snapshot {
  private final Optional<Ref> lock;
  private final String branchName;
  private final ImmutablePersistenceUnit.Builder unit = ImmutablePersistenceUnit.builder(); 
  private final Map<String, Props> propsByNodeId = new HashMap<>();
  private final Map<String, Blob> blobsById = new HashMap<>();
  private final Map<String, Node> nodesById = new HashMap<>();
  
  
  public interface ChangeCommand {};
  
  public static record RmCommand(String docId) implements ChangeCommand {}
  
  public static record NewFolderCommand(Consumer<NewFolder> consumer) implements ChangeCommand {}
  public static record NewFileCommand(Consumer<NewFile> consumer) implements ChangeCommand {}
  
  public static record MergeFolderCommand(String docId, Consumer<MergeFolder> consumer) implements ChangeCommand{}
  public static record MergeFileCommand(String docId, Consumer<MergeFile> consumer) implements ChangeCommand {}
  
  
  public Snapshot addAll(List<ChangeCommand> changes) {
    for(final var change : changes) {
      if(change instanceof RmCommand) {
        visitRmCommand((RmCommand) change);
      } else if(change instanceof NewFolderCommand) {
        visitNewFolderCommand((NewFolderCommand) change);
      } else if(change instanceof NewFileCommand) {
        visitNewFileCommand((NewFileCommand) change);
        
      } else if(change instanceof MergeFolderCommand) {
        visitMergeFolderCommand((MergeFolderCommand) change);
      } else if(change instanceof MergeFileCommand) {
        visitMergeFileCommand((MergeFileCommand) change);
      }
      
      RepoAssert.fail("Unknown command: " + change.getClass().getSimpleName());
    }
    return this;
  }
  
  public Snapshot visitRmCommand(RmCommand command) {
    return this;
  }
  
  public Snapshot visitNewFolderCommand(NewFolderCommand command) {
    return this;
  }

  public Snapshot visitNewFileCommand(NewFileCommand command) {
    return this;
  }
  
  public Snapshot visitMergeFolderCommand(MergeFolderCommand command) {
    final var previousNode = nodesById.get(command.docId());
    RepoAssert.notNull(previousNode, () -> "Can't find folder to merge by id: '" + command.docId() + "'!");
    
    final var previousProps = Optional.ofNullable(propsByNodeId.get(previousNode.getId()));
    final var previousBlob = previousNode.getBlobId().map(blobsById::get);
    
    final var merger = new MergeFolderImpl(previousNode, previousProps, previousBlob); 
    command.consumer().accept(merger);
    merger.close(unit);
    return this;
  }
  
  public Snapshot visitMergeFileCommand(MergeFileCommand command) {
    final var previousNode = nodesById.get(command.docId());
    RepoAssert.notNull(previousNode, () -> "Can't find file to merge by id: '" + command.docId() + "'!");
    
    final var previousProps = Optional.ofNullable(propsByNodeId.get(previousNode.getId()));
    final var previousBlob = previousNode.getBlobId().map(blobsById::get).orElseThrow();
    
    final var merger = new MergeFileImpl(previousNode, previousProps, previousBlob); 
    command.consumer().accept(merger);
    merger.close(unit);
    return this;
  }
  
  
  public PersistenceUnit build(Consumer<ImmutableCommit.Builder> callback) {
    
  }
}
