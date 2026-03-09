package io.resys.thena.fs.spi;

import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.fs.entities.Tree;

@Value.Immutable
public interface FileSystemConfig {
  FileSystemCache getCache();
  
  interface FileSystemCache {
    Optional<Tree> findOneTreeById(String treeId);
    Tree cacheOneTree(Tree tree);

    void flushAll();
  }
}
