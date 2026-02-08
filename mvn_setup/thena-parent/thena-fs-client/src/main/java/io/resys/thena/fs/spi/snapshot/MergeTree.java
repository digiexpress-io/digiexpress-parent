package io.resys.thena.fs.spi.snapshot;

import java.util.List;

import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tree;
import lombok.RequiredArgsConstructor;
import lombok.Value;


@RequiredArgsConstructor
public class MergeTree {
  private final Ref lock;
  private final List<Node> nodes;
  private final List<Props> props;
  private final List<Blob> blobs;
  
  public MergeTreeResult close() {
    
  }
  
  @Value
  public static class MergeTreeResult {
    Tree tree;
  }
}
