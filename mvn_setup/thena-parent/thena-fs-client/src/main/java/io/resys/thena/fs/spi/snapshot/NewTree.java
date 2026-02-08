package io.resys.thena.fs.spi.snapshot;

import java.util.List;

import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Tree;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class NewTree {
  
  private final List<Node> nodes;
  private final List<Props> props;
  private final List<Blob> blobs;
  
  public NewTreeResult close() {
    final var newTree = Tree.newInstance(nodes).build();
    return new NewTreeResult(newTree);
  }
  
  @Value
  public static class NewTreeResult {
    Tree tree;
  }

}
