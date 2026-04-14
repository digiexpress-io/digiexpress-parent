package io.resys.limaone.persistence.world;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import io.resys.limaone.authoring.Authoring.WorldFsQuery;
import io.resys.limaone.fs.ImmutableWorldFs;
import io.resys.limaone.fs.WorldFs;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.entities.Ref;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WorldFsQueryImpl implements WorldFsQuery {
  private final FileSystem filesystem;
  private final ScheduledExecutorService workerPool;
  private final Duration workerTimeout;
  private final String branchName;

  @Override
  public Uni<WorldFs> findAll() {
    final var tenant = filesystem.withTenant();    
    return tenant
      .branchQuery()
      .branchName(name -> name.equals(branchName))
      .getOne()
      .onItem().transform(ref -> {
        return mapToWorld(ref);
      });
  }

  @Override
  public WorldFs findAllSync() {
    return findAll()
      .runSubscriptionOn(workerPool)
      .await().atMost(workerTimeout);
  }
  
  private WorldFs mapToWorld(Ref ref) {
   return ImmutableWorldFs.builder().build();
  }
}
