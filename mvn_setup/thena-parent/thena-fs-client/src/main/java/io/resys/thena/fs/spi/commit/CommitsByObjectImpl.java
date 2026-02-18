package io.resys.thena.fs.spi.commit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.resys.thena.fs.api.commits.CommitQuery.CommitsByObject;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CommitsByObjectImpl implements CommitsByObject {
  private final String objectId;
  
  private Map<String, Node> nodesById = new HashMap<>();
  private Map<String, Commit> commitsById = new HashMap<>();
  private Map<String, Blob> blobs = new HashMap<>();
  private Map<String, Props> props = new HashMap<>();
  
  @Override
  public String getObjectId() {
    return objectId;
  }

  @Override
  public Map<String, Node> getNodesById() {
    return nodesById;
  }

  @Override
  public Map<String, Commit> getCommitsById() {
    return commitsById;
  }

  @Override
  public Map<String, Blob> getBlobsById() {
    return blobs;
  }

  @Override
  public Map<String, Props> getPropsById() {
    return props;
  }
  public CommitsByObjectImpl addProps(Optional<Props> props) {
    if(props.isPresent()) {
      this.props.put(props.get().getId(), props.get());
    }
    return this;
  }
  public CommitsByObjectImpl addBlobs(Optional<Blob> blob) {
    if(blob.isPresent()) {
      this.blobs.put(blob.get().getId(), blob.get());
    }
    return this;
  }
  public CommitsByObjectImpl add(Node node) {
    nodesById.put(node.getId(), node);
    return this;
  }
  public CommitsByObjectImpl add(Commit commit) {
    commitsById.put(commit.getId(), commit);
    return this;
  }
  public CommitsByObjectImpl add(Blob blob) {
    return this;
  }
  public CommitsByObjectImpl add(Props props) {
    return this;
  }
  public void close() {
    this.nodesById = Collections.unmodifiableMap(nodesById);
    this.commitsById = Collections.unmodifiableMap(commitsById);
    this.blobs = Collections.unmodifiableMap(blobs);
    this.props = Collections.unmodifiableMap(props);
  }
}
