package io.resys.thena.fs.spi.commitbuilder;

import java.util.Optional;
import java.util.function.BiConsumer;

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFile;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.tables.ImmutablePersistenceUnit;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MergeFileImpl implements MergeFile {
  private final Node previousNode;
  private final Optional<Props> previousProps;
  private final Blob previousBlob;


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
    // TODO Auto-generated method stub
    
  }

  public void close(ImmutablePersistenceUnit.Builder unit) {
    
  }
}
