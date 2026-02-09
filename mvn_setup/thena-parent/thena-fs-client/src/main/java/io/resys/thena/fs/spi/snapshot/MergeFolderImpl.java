package io.resys.thena.fs.spi.snapshot;

import java.util.Optional;
import java.util.function.BiConsumer;

import io.resys.thena.fs.api.commits.CommitBuilder.MergeFolder;
import io.resys.thena.fs.api.commits.CommitBuilder.PropsBuilder;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class MergeFolderImpl implements MergeFolder {
  private final Node prevNode;
  
  private final MutableField<String> folderPath = new MutableField<String>();
  private final MutableField<String> folderName = new MutableField<String>();

  private BiConsumer<Optional<Props>, PropsBuilder> folderProps;
  private boolean validated = false;
  
  @Override
  public MergeFolder folderName(String folderName) {
    this.folderName.withNewValue(folderName);
    return this;
  }
  @Override
  public MergeFolder folderPath(String folderPath) {
    this.folderPath.withNewValue(folderPath);
    return this;
  }
  @Override
  public MergeFolder folderProps(BiConsumer<Optional<Props>, PropsBuilder> folderProps) {
    this.folderProps = folderProps;
    return this;
  }
  @Override
  public void build() {
    RepoAssert.isTrue(
      folderName.isNewValueSet() ||
      folderPath.isNewValueSet() ||  
      folderProps != null,
      () -> "cannot have empty folder('" + prevNode.getNodePath() + "') merge(there are no changes)!");
    this.validated = true;
  }
  
  public MergeFolderResult close() {
    RepoAssert.isTrue(validated, () -> "build() method must be called before close()");
    
    // Merge props if provided
    final var nextProps = Optional.ofNullable(folderProps).map(p -> {
      final var prevProps = Optional.ofNullable(prevNode.getTransitives().getProps());
      final var builder = new PropsBuilderImpl(prevProps);
      p.accept(prevProps, builder);
      return builder.close().getProps();
    });
    
    // static data, once in its in... can't change PK
    final var nodeId = prevNode.getNodeId();
    
    final String folderPath = this.folderPath.orElse(prevNode.getNodePath().orElse(null));

    final String folderName = this.folderName.orElse(prevNode.getNodeName());
    
    final Optional<String> blobId = Optional.empty();
    final var nextNode = Node.newInstance(
        Optional.ofNullable(folderPath), 
        nodeId, 
        folderName, 
        blobId, 
        nextProps.map(Props::getId)
    ).build();
    
    return new MergeFolderResult(nextNode, nextProps);
  }
  
  @Value
  public static class MergeFolderResult {
    Node node;
    Optional<Props> props;    
  }
}
