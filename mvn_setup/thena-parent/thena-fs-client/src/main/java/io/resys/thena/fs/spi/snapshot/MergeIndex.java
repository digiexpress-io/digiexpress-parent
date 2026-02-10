package io.resys.thena.fs.spi.snapshot;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.ImmutableObjectIndex;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.ObjectIndex;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.RepoAssert;
import lombok.RequiredArgsConstructor;
import lombok.Value;

@RequiredArgsConstructor
public class MergeIndex {
  private final Optional<Ref> ref;
  private final OffsetDateTime now;
  
  private final List<ImmutableObjectIndex.Builder> updates = new ArrayList<>();
  private final List<ImmutableObjectIndex.Builder> inserts = new ArrayList<>();
  
  public void rm(List<Node> nodes) {
    for(final var node : nodes) {
      merge(node, node);
    }
  }
  
  public void merge(Node prev, Node next) {
    RepoAssert.isTrue(ref.isPresent(), () ->  "Ref lock is missing, no previous data, merge requires previous change into what to apply changes!");
    RepoAssert.isTrue(prev.getTransitives() != null, () ->  "previous 'node.transitives' must be loaded!");
    RepoAssert.isTrue(prev.getTransitives().getObjectIndex() != null, () ->  "previous node 'node.transitives.objectIndex' must be loaded!");
    
    updates.add(ImmutableObjectIndex.builder()
        .from(prev.getTransitives().getObjectIndex())
        .updatedAt(now));
  }
  
  public void create(Node next) {
    inserts.add(ImmutableObjectIndex.builder()
        .objectId(next.getObjectId())
        .createdAt(now)
        .updatedAt(now));
  }
  
  public MergeIndexResult close(Commit commit) {
    return new MergeIndexResult(
        updates.stream().map(b -> b.updatedBy(commit.getId()).build()).toList(), 
        inserts.stream().map(b ->  b.createdBy(commit.getId()).updatedBy(commit.getId()).build()).toList()
    );
  }
  
  @Value
  public static class MergeIndexResult {
    List<? extends ObjectIndex> updates;
    List<? extends ObjectIndex> inserts;
  }
}
