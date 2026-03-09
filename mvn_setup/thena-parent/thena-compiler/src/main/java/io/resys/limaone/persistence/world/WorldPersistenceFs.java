package io.resys.limaone.persistence.world;

import io.resys.limaone.persistence.WorldPersistence;
import io.resys.thena.fs.api.FileSystem;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WorldPersistenceFs implements WorldPersistence {

  private final FileSystem fileSystem;
  
  @Override
  public WorldBuilder worldBuilder() {
    return new WorldBuilderImpl(fileSystem);
  }
  
  public static class WorldLockException extends RuntimeException {
    private static final long serialVersionUID = -1868980098559928896L;
  }
}
