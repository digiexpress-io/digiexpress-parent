package io.resys.thena.fs.spi.snapshot;

import java.util.Optional;
import java.util.function.Consumer;

import io.resys.thena.fs.api.commits.CommitBuilder.NewFile;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class NewFileImpl implements NewFile {
  
  private final Optional<Ref> lock;
  
  private String filePath;
  private String fileName;
  private String fileId;
  private String fileType;
  private JsonObject fileValue;
  private Consumer<PropsBuilder> fileProps;
  private boolean validated = false;

  @Override
  public NewFile fileProps(Consumer<PropsBuilder> props) {
    this.fileProps = props;
    return this;
  }

  @Override
  public NewFile fileValue(JsonObject blob) {
    this.fileValue = blob;
    return this;
  }

  @Override
  public NewFile fileType(String type) {
    this.fileType = type;
    return this;
  }

  @Override
  public NewFile fileName(String name) {
    this.fileName = name;
    return this;
  }

  @Override
  public NewFile fileId(String fileId) {
    this.fileId = fileId;
    return this;
  }

  @Override
  public NewFile filePath(String path) {
    this.filePath = path;
    return this;
  }

  @Override
  public void build() {
    validateFilePath(this.filePath);
    validateFileName(this.fileName);
    validateFileValue(this.fileValue);
    validateFileType(this.fileType);
    this.validated = true;
  }
  
  public NewFileResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    final var nodeId = Optional
        .ofNullable(this.fileId)
        .orElseGet(() -> OidUtils.genUUID());
    
    final var blob = Blob.newInstance(this.fileValue, this.fileType).build();
    
    final var props = Optional.ofNullable(this.fileProps).map(p -> {
      final var builder = new PropsBuilderImpl(lock);
      p.accept(builder);
      return builder.close().getProps();
    }).orElse(null);
    
    final var blobId = Optional.of(blob.getId());
    final var propsId = Optional.ofNullable(props).map(Props::getId);
    final var node = Node.newInstance(this.filePath, nodeId, this.fileName, blobId, propsId).build();
    
    return new NewFileResult(node, blob, props);
  }
  
  private void validateFilePath(String path) {
    RepoAssert.notNull(path, () -> "filePath is required");
    RepoAssert.isTrue(!path.trim().isEmpty(), () -> "filePath cannot be empty");
    RepoAssert.isTrue(path.matches("^[a-zA-Z0-9/_-]+$"), () -> "filePath contains invalid characters, only a-z, A-Z, 0-9, /, _, - allowed");
    RepoAssert.isTrue(!path.contains("//"), () -> "filePath cannot contain double slashes");
    RepoAssert.isTrue(!path.endsWith("/"), () -> "filePath cannot end with slash");
  }
  
  private void validateFileName(String name) {
    RepoAssert.notNull(name, () -> "fileName is required");
    RepoAssert.isTrue(!name.trim().isEmpty(), () -> "fileName cannot be empty");
    RepoAssert.isTrue(name.matches("^[a-zA-Z0-9_.-]+$"), () -> "fileName contains invalid characters, only a-z, A-Z, 0-9, _, ., - allowed");
    RepoAssert.isTrue(!name.contains("/"), () -> "fileName cannot contain slashes");
  }
  
  private void validateFileValue(JsonObject value) {
    RepoAssert.notNull(value, () -> "fileValue is required");
    RepoAssert.isTrue(!value.isEmpty(), () -> "fileValue cannot be empty");
  }
  
  private void validateFileType(String type) {
    RepoAssert.notNull(type, () -> "fileType is required");
    RepoAssert.isTrue(!type.trim().isEmpty(), () -> "fileType cannot be empty");
    RepoAssert.isTrue(type.matches("^[a-zA-Z0-9_/-]+$"), () -> "fileType contains invalid characters, only a-z, A-Z, 0-9, _, /, - allowed");
  }

  @Value
  public static class NewFileResult {
    Node node;
    Blob blob;
    Props props;    
  }
}
