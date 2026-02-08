package io.resys.thena.fs.spi.snapshot;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.commits.CommitBuilder.NewFile;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class NewFileImpl implements NewFile {
  
  private final Optional<Ref> lock;

  @Override
  public NewFile fileProps(Consumer<PropsBuilder> props) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NewFile fileValue(JsonObject blob) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NewFile fileType(String type) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NewFile fileName(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NewFile fileId(String fileId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public NewFile filePath(String path) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void build() {
    // TODO Auto-generated method stub
    
  }
  
  public NewFileResult close() {

  }

  @Value
  public static class NewFileResult {
    Node node;
    Blob blob;
    Props props;    
  }
}
