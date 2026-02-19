package io.resys.thena.fs.spi.committree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.resys.thena.fs.api.commits.CommitQuery.CommitsByObject;
import io.resys.thena.fs.spi.commit.CommitsByObjectImpl;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GroupByObject implements CommitTreeVisitor {
  private final CommitTreeCache cache;
  private final Map<String, CommitsByObject> objects = new HashMap<>();

  
  @Override
  public void visit(CommitTree previous, CommitTree next) {
    if(previous == null) {
      init(next);
    } else {
      diff(previous, next);
    }
  }

  private void init(CommitTree next) {
    for(final var entry : next.getNodes().entrySet()) {
      final var objectId = entry.getValue().getObjectId();
      getBuilder(objectId)
        .add(next.getCommit())
        .add(entry.getValue())
        .addBlobs(cache.getBlob(entry.getValue()))
        .addProps(cache.getProps(entry.getValue()));
    }
  }
      
  private void diff(CommitTree previous, CommitTree next) {
    for(final var entry : next.getNodes().entrySet()) {

      final var objectId = entry.getValue().getObjectId();
      final var prev_node = previous.getNodes().get(objectId);
      final var next_node = next.getNodes().get(objectId);
      
      final var isBlobChanged = prev_node.getBlobId().orElse("").equals(next_node.getBlobId().orElse(""));
      final var isPropsChanged = prev_node.getPropsId().orElse("").equals(next_node.getPropsId().orElse(""));
      
      if(isBlobChanged || isPropsChanged) {
        getBuilder(objectId)
          .add(next.getCommit())
          .add(next_node)
          .addBlobs(cache.getBlob(next_node))
          .addProps(cache.getProps(next_node));
      }
    }
  }

  public Collection<CommitsByObject> close() {
    return objects.values();
  }
  
  
  private CommitsByObjectImpl getBuilder(String objectId) {
    CommitsByObjectImpl result = (CommitsByObjectImpl) objects.get(objectId);
    if(result == null) {
      result = new CommitsByObjectImpl(objectId);
      objects.put(objectId, result); 
    }
    return result;
  }

}