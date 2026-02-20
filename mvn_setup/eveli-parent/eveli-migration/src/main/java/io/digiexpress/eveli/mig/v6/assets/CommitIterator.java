package io.digiexpress.eveli.mig.v6.assets;

import java.util.ArrayList;
import java.util.List;

import io.digiexpress.eveli.mig.v6.assets.CommitNodeBuilder.CommitNode_Impl;

public class CommitIterator {
  private final List<CommitNode> nodes = new ArrayList<>();
  
  public CommitIterator(List<CommitNode> nodes) {
    this.nodes.addAll(nodes);
  }

  public boolean isNext() {
    return nodes.stream().map(e -> e.getNext())
      .filter(next -> next.isPresent())
      .findFirst().isPresent();
  }
  
  public CommitNode_Impl next() {
     final var sortedNodes = nodes.stream()
        .filter(e -> e.getNext().isPresent())
        .sorted((a, b) -> a.getNext().get().getCommit().getDatetime().compareTo(b.getNext().get().getCommit().getDatetime()));
     
     final var nodeWithNext = sortedNodes.iterator().next();
     final var next = (CommitNode_Impl) nodeWithNext.getNext().get();
     
     nodes.remove(nodeWithNext);
     nodes.add(next);
     return next;
     
  }
}