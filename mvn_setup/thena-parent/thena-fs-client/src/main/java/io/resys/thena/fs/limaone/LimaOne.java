package io.resys.thena.fs.limaone;

import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Tree;
import io.smallrye.mutiny.Multi;

/**
 * L1 cache of compiled or interpreted asset
 */
public interface LimaOne {

  CachableQuery cachableQuery();
  
  ArtifactQuery artifactQuery();
  
  interface ArtifactQuery {
    Multi<Artifact> findAll();
  }
  
  
  interface CachableQuery {
    Cachable<Blob> blob();
    Cachable<Tree> tree();
    Cachable<Props> props();
    Cachable<Node> node();
    void flushAll();
  }
  
  interface Cachable<T> {
    // store into cache
    T cache(T t);
    
    // get from cache
    T get(String id);
    
    // retrieve from persistence and cache it... WARNING SLOW operation
    T force(String id);
    
    void flush(String id);
  }
}
