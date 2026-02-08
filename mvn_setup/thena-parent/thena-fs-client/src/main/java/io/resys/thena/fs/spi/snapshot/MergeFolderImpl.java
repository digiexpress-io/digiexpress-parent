package io.resys.thena.fs.spi.snapshot;

import java.util.Optional;
import java.util.function.BiConsumer;

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFolder;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class MergeFolderImpl implements MergeFolder {
  private final Ref lock;

  
  @Override
  public MergeFolder folderPath(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public MergeFolder folderProps(BiConsumer<Props, PropsBuilder> props) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void build() {
    final var previousNode = nodesById.get(command.docId());
    RepoAssert.notNull(previousNode, () -> "Can't find folder to merge by id: '" + command.docId() + "'!");
    
    final var previousProps = Optional.ofNullable(propsByNodeId.get(previousNode.getId()));
    final var previousBlob = previousNode.getBlobId().map(blobsById::get);
  }

  
  public MergeFolderResult close() {
    
  }
  
  @Value
  public static class MergeFolderResult {
    Node node;
    Props props;    
  }
}
