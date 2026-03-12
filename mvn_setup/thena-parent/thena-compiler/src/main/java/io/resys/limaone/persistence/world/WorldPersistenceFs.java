package io.resys.limaone.persistence.world;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import io.resys.limaone.authoring.Authoring.WorldQuery;
import io.resys.limaone.persistence.WorldPersistence;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.thena.fs.api.FileSystem;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WorldPersistenceFs implements WorldPersistence {

  private final FormDb formDb;
  private final FileSystem fileSystem;
  private final ScheduledExecutorService workerPool;
  private final Duration workerTimeout;
  
  @Override
  public WorldBuilder worldBuilder() {
    return new WorldBuilderImpl(fileSystem);
  }
  
  @Override
  public WorldQuery worldQuery() {
    return new WorldQueryImpl(workerPool, workerTimeout, fileSystem, formDb);
  }
  
  public static class WorldLockException extends RuntimeException {
    private static final long serialVersionUID = -1868980098559928896L;
  }
}
