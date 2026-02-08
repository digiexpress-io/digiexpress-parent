package io.resys.thena.fs.spi.snapshot;

import java.util.Optional;
import java.util.function.BiConsumer;

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFile;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.tables.ImmutablePersistenceUnit;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class MergeFileImpl implements MergeFile {
  private final Ref lock;

  

  @Override
  public MergeFile fileValue(JsonObject blob) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public MergeFile fileName(String name) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public MergeFile filePath(String path) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public MergeFile fileProps(BiConsumer<Props, PropsBuilder> props) {
    // TODO Auto-generated method stub
    return null;
  }
  
  @Override
  public void build() {
    final var previousNode = nodesById.get(command.docId());
    RepoAssert.notNull(previousNode, () -> "Can't find file to merge by id: '" + command.docId() + "'!");
    
    final var previousProps = Optional.ofNullable(propsByNodeId.get(previousNode.getId()));
    final var previousBlob = previousNode.getBlobId().map(blobsById::get).orElseThrow();
    
    
  }

  public MergeFileResult close() {
    
  }
  
  @Value
  public static class MergeFileResult {
    Node node;
    Blob blob;
    Props props;    
  }
}
