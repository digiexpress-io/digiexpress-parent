package io.resys.thena.fs.spi.snapshot;

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.commits.CommitBuilder.NewFolder;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class NewFolderImpl implements NewFolder {
  private final Optional<Ref> lock;
  
  private String folderPath;
  private String folderName;
  private String folderId;
  private Consumer<PropsBuilder> folderProps;
  private boolean validated = false;

  @Override
  public NewFolder folderPath(String folderPath) {
    this.folderPath = folderPath;
    return this;
  }
  @Override
  public NewFolder folderName(String folderName) {
    this.folderName = folderName;
    return this;
  }
  @Override
  public NewFolder folderId(String folderId) {
    this.folderId = folderId;
    return this;
  }
  @Override
  public NewFolder folderProps(Consumer<PropsBuilder> folderProps) {
    this.folderProps = folderProps;
    return this;
  }
  @Override
  public void build() {
    this.validated = true;
  }
  
  public NewFolderResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    final var nodeId = Optional
        .ofNullable(this.folderId)
        .orElseGet(() -> OidUtils.genUUID());
    
    final var path = this.folderPath;
    final var name = this.folderName;
    final var blobId = Optional.<String>empty(); 
    
    final var props = Optional.ofNullable(this.folderProps).map(p -> {
      final var builder = new PropsBuilderImpl(lock);
      p.accept(builder);
      return builder.close().getProps();
    });
    
    final var propsId = props.map(Props::getId);
    final var node = Node.newInstance(Optional.ofNullable(path), nodeId, name, blobId, propsId).build();
    
    return new NewFolderResult(node, props);
  }
  

  @Value
  public static class NewFolderResult {
    Node node;
    Optional<Props> props;
  }
}
