package io.resys.thena.fs.spi.commit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
  private Map<String, Blob> blobsByCommitId = new HashMap<>();
  private Map<String, Props> propsByCommitId = new HashMap<>();
  
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
  public Map<String, Blob> getBlobsByCommitId() {
    return blobsByCommitId;
  }

  @Override
  public Map<String, Props> getPropsByCommitId() {
    return propsByCommitId;
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
    this.blobsByCommitId = Collections.unmodifiableMap(blobsByCommitId);
    this.propsByCommitId = Collections.unmodifiableMap(propsByCommitId);
  }
}
