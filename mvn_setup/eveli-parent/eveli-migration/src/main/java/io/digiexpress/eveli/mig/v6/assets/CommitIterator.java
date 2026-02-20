package io.digiexpress.eveli.mig.v6.assets;

import java.util.ArrayList;
import java.util.List;

import io.digiexpress.eveli.mig.v6.assets.ExtractedNode.TemplateNode;

public class CommitIterator {
  private final List<TemplateNode> nodes = new ArrayList<>();
  
  public CommitIterator(List<TemplateNode> nodes) {
    this.nodes.addAll(nodes);
  }

  public boolean isNext() {
    return nodes.stream().map(e -> e.getNext())
      .filter(next -> next.isPresent())
      .findFirst().isPresent();
  }
  
  public TemplateNode next() {
     final var sortedNodes = nodes.stream()
        .filter(e -> e.getNext().isPresent())
        .sorted((a, b) -> a.getNext().get().getCommit().getDatetime().compareTo(b.getNext().get().getCommit().getDatetime()));
     
     final var nodeWithNext = sortedNodes.iterator().next();
     final var next = (TemplateNode) nodeWithNext.getNext().get();
     
     nodes.remove(nodeWithNext);
     nodes.add(next);
     return next;
     
  }
}