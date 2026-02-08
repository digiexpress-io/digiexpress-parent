package io.resys.thena.fs.spi.snapshot;

import java.util.Optional;
import java.util.function.BiConsumer;

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFile;
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
public class MergeFileImpl implements MergeFile {
  private final Ref lock;
  private final String targetId;
  
  private String filePath;
  private String fileName;
  private JsonObject fileValue;
  private BiConsumer<Props, PropsBuilder> fileProps;
  private boolean validated = false;

  @Override
  public MergeFile fileValue(JsonObject blob) {
    this.fileValue = blob;
    return this;
  }

  @Override
  public MergeFile fileName(String name) {
    this.fileName = name;
    return this;
  }

  @Override
  public MergeFile filePath(String path) {
    this.filePath = path;
    return this;
  }

  @Override
  public MergeFile fileProps(BiConsumer<Props, PropsBuilder> props) {
    this.fileProps = props;
    return this;
  }
  
  @Override
  public void build() {
    if(filePath != null) {
      validateFilePath(this.filePath);
    }
    
    validateFileName(this.fileName);
    validateFileValue(this.fileValue);
    
    this.validated = true;
  }

  public MergeFileResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    final var prevNode = lock.getTransitives().getNodesById().get(targetId);
    final var prevProps = prevNode.getPropsId().map(lock.getTransitives().getPropsById()::get);
    final var prevBlob = prevNode.getPropsId().map(lock.getTransitives().getBlobsById()::get);
    
    // TODO: Load existing node from lock.getTransitives().getTree() by path/name
    // For now using placeholders
    final var nodeId = OidUtils.genUUID(); // TODO: use existing node ID
    final var existingProps = (Props) null; // TODO: load existing props
    
    // Create new blob if fileValue provided
    final var blob = Optional.ofNullable(this.fileValue)
        .map(value -> {
          // TODO: determine fileType from existing blob or default
          return Blob.newInstance(value, "application/json").build();
        })
        .orElse(null); // TODO: use existing blob
    
    // Merge props if provided
    final var props = Optional.ofNullable(this.fileProps)
        .map(p -> {
          final var builder = new PropsBuilderImpl(Optional.of(lock));
          p.accept(existingProps, builder);
          return builder.close().getProps();
        })
        .orElse(existingProps);
    
    final var blobId = Optional.ofNullable(blob).map(Blob::getId);
    final var propsId = Optional.ofNullable(props).map(Props::getId);
    
    final var path = Optional.ofNullable(this.filePath);
    final var name = this.fileName;
    final var node = Node.newInstance(path.orElse(""), nodeId, name, blobId, propsId).build();
    
    return new MergeFileResult(node, blob, props);
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
  
  @Value
  public static class MergeFileResult {
    Node node;
    Blob blob;
    Props props;    
  }
}
