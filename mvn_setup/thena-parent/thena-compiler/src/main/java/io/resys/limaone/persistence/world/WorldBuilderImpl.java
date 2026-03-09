package io.resys.limaone.persistence.world;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.persistence.WorldPersistence.NextWorld;
import io.resys.limaone.persistence.WorldPersistence.WorldBuilder;
import io.resys.limaone.persistence.world.WorldPersistenceFs.WorldLockException;
import io.resys.thena.fs.api.FileSystem;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WorldBuilderImpl implements WorldBuilder {
  private final FileSystem fileSystem;
  private final WorldPersistenceLogger logger = new WorldPersistenceLogger();
  private final List<String> blobTypes = new ArrayList<>();
  
  private String branchName = "main";
  private String commitId;
  private String author;
  private OffsetDateTime createdAt;
  
  @Override public WorldBuilder author(String author) { this.author = author; return this; }
  @Override public WorldBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
  @Override public WorldBuilder lockWithCommit(String commitId) { this.commitId = commitId; return this; }
  @Override public WorldBuilder docs(BodyType... type) { this.blobTypes.addAll(Arrays.asList(type).stream().map(e -> e.name()).toList()); return this; }
  
  @Override
  public <T> Uni<T> build(Function<NextWorld, T> mergeFunction) {
    Objects.requireNonNull(author, () -> "author must be provided");
    Objects.requireNonNull(createdAt, () -> "createdAt must be provided");
    
    final var tenant = fileSystem.withTenant();
    logger.stage1TenantConfigured(tenant, commitId);
    return tenant.branchQuery()
      .branchId(branchName)
      .getOne()
      .onItem().transformToUni(ref -> {
        logger.stage2CurrentState(ref);
        if(commitId != null && !ref.getCommitId().equals(commitId)) {
          logger.stage3LockFailed(ref);
          throw new WorldLockException();
        }

        final var nextWorld = new NextWorldImpl(tenant, ref, author, createdAt);
        final var mapped = mergeFunction.apply(nextWorld);
        final var commitBuilder = nextWorld.close();
        
        
        logger.stage4NextState();
        
        return commitBuilder
            .branchName(branchName)
            .branchLock(ref.getCommitId())
            .build()
            .onItem().transform(commited -> mapped)
            .onFailure().invoke((e) -> logger.closeWithFailure(e))
            .onItem().invoke(() -> logger.close());

      });
  }

}
