package io.resys.limaone.persistence.fs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.resys.limaone.fs.ImmutableDirentBase;
import io.resys.limaone.fs.WorldFsProps;
import io.resys.limaone.model.Article;
import io.resys.limaone.model.Locale;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import io.resys.thena.support.RepoAssert;

public class WorldFsState {
  private final Map<String, NodePathAndName> nodePathAndName_by_object_id = new HashMap<>();
  private final Map<String, NodeAndBody> nodes_by_object_id = new HashMap<>();
  private final Map<String, WorldFsProps> props_by_object_id = new HashMap<>();
  private final Map<String, ImmutableDirentBase> dirents_by_fullpath = new HashMap<>();
  
  // parent path -> fullpath : dirent
  private final Map<String, Map<String, ImmutableDirentBase>> dirents_grouped_by_path = new HashMap<>();
  
  
  /**
   * @return folder names that are refered in the state. Does not mean that they actually exist.
   */
  public Collection<String> getFolderNames() {
    return dirents_grouped_by_path.keySet().stream().toList();
  }
  
  public WorldFsProps getProps(String objectId) {
    return props_by_object_id.get(objectId);
  }
  
  public WorldFsProps putProps(NodeAndBody nodeAndBody, Function<NodeAndBody, WorldFsProps> createProps) {
    final var objectId = nodeAndBody.getObjectId();
    if(!props_by_object_id.containsKey(objectId)) {
      props_by_object_id.put(objectId, createProps.apply(nodeAndBody));      
    }
    return props_by_object_id.get(objectId);
  }

  public NodeAndBody putNodeAndBody(NodeAndBody nodeAndBody) {
    nodes_by_object_id.put(nodeAndBody.getValue().getObjectId(), nodeAndBody);
    return nodeAndBody;
  }
  
  public NodeAndBody getNodeAndBody(String id) {
    return nodes_by_object_id.get(id);
  }
  
  public boolean isFolderDirentCreated(String folderName) {
    return dirents_by_fullpath.containsKey(folderName);
  }
  
  public ImmutableDirentBase getFolderDirent(String folderName) {
    final var dirent = dirents_by_fullpath.get(folderName);
    RepoAssert.isTrue(dirent.getType() != BodyType.FOLDER, () -> "Dirent must be a folder but was: " + dirent.getType());
    return dirent;
  }
  
  
  public Collection<ImmutableDirentBase> getChildDirents(String folderName) {
    final var children = Optional.ofNullable(dirents_grouped_by_path.get(folderName))
        .map(e -> e.values())
        .orElse(Collections.emptyList());
    return children;
  }
  
  
  public NodePathAndName getPathAndName(NodeAndBody node, Function<NodeAndBody, NodePathAndName> createPath) {
    if(nodePathAndName_by_object_id.containsKey(node.getObjectId())) {
      return nodePathAndName_by_object_id.get(node.getObjectId());
    }
    final var next = createPath.apply(node);
    nodePathAndName_by_object_id.put(node.getObjectId(), next);
    return next;
  }


  public void putDirent(String path, ImmutableDirentBase dirent) {
    if(!dirents_grouped_by_path.containsKey(path)) {
      dirents_grouped_by_path.put(path, new HashMap<>());
    }
    dirents_grouped_by_path.get(path).put(dirent.getFullPath(), dirent);
    dirents_by_fullpath.put(dirent.getFullPath(), dirent);
  }
  
  
  public Locale getLocale(String localeId) {
    return (Locale) nodes_by_object_id.get(localeId).getBody().get(); 
  }
  
  @SuppressWarnings("unchecked")
  public <T extends Body> T getBodyOfType(NodeAndBody node) {
    return (T) nodes_by_object_id.get(node.getObjectId()).getBody().get(); 
  }
  
  public List<NodeAndBody> getArticleHierarchy(NodeAndBody node) {
    Article article = node.getBodyOfType(); 
    final List<NodeAndBody> result = new ArrayList<>(); 
    
    // add self
    result.add(node);
    
    if(article.getParentId() == null) {
      return result;
    }
    
    // add parents
    while(article != null) {
      if(article.getParentId() == null) {
        break;
      }
      final var nextNode = nodes_by_object_id.get(article.getParentId());
      result.add(nextNode);
      article = nextNode.getBodyOfType();
      
    }
    return result.reversed();
  }
}
