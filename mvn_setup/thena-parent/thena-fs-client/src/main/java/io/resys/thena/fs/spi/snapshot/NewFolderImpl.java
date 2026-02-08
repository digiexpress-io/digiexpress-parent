package io.resys.thena.fs.spi.snapshot;

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.commits.CommitBuilder.NewFolder;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class NewFolderImpl implements NewFolder {

  private final Optional<Ref> lock;

  @Override
  public NewFolder folderPath(String path) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public NewFolder folderId(String folderId) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public NewFolder folderProps(Consumer<PropsBuilder> props) {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public void build() {
    // TODO Auto-generated method stub
    
  }
  
  
  public NewFolderResult close() {
    
  }
  
  @Value
  public static class NewFolderResult {
    Node node;
    Props props;
  }

}
