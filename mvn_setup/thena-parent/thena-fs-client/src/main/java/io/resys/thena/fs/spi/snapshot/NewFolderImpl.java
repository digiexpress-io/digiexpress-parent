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
    validateFolderPath(this.folderPath);
    validateFolderName(this.folderName);
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
    }).orElse(null);
    
    final var propsId = Optional.ofNullable(props).map(Props::getId);
    final var node = Node.newInstance(path, nodeId, name, blobId, propsId).build();
    
    return new NewFolderResult(node, props);
  }
  
  private void validateFolderPath(String path) {
    RepoAssert.notNull(path, () -> "folderPath is required");
    RepoAssert.isTrue(!path.trim().isEmpty(), () -> "folderPath cannot be empty");
    RepoAssert.isTrue(path.matches("^[a-zA-Z0-9/_-]+$"), () -> "folderPath contains invalid characters, only a-z, A-Z, 0-9, /, _, - allowed");
    RepoAssert.isTrue(!path.contains("//"), () -> "folderPath cannot contain double slashes");
    RepoAssert.isTrue(!path.endsWith("/"), () -> "folderPath cannot end with slash");
  }
  
  private void validateFolderName(String name) {
    RepoAssert.notNull(name, () -> "folderName is required");
    RepoAssert.isTrue(!name.trim().isEmpty(), () -> "folderName cannot be empty");
    RepoAssert.isTrue(name.matches("^[a-zA-Z0-9_-]+$"), () -> "folderName contains invalid characters, only a-z, A-Z, 0-9, _, - allowed");
    RepoAssert.isTrue(!name.contains("/"), () -> "folderName cannot contain slashes");
  }
  
  @Value
  public static class NewFolderResult {
    Node node;
    Props props;
  }
}
