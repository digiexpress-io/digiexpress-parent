package io.resys.thena.fs.spi.commitbuilder;

import java.util.Optional;
import java.util.function.BiConsumer;

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFolder;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.tables.ImmutablePersistenceUnit;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MergeFolderImpl implements MergeFolder {

  private final Node previousNode;
  private final Optional<Props> previousProps;
  private final Optional<Blob> previousBlobs;

  
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
    // TODO Auto-generated method stub
    
  }

  
  public void close(ImmutablePersistenceUnit.Builder unit) {
    
  }
}
