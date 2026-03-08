package io.resys.limaone.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.resys.limaone.model.ImmutableModel;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.api.FileSystem.FileSystemTenant;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.support.OidUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WorldPersistenceFs implements WorldPersistence {

  private final FileSystem fileSystem;
  
  @Override
  public WorldBuilder worldBuilder() {
    return new WorldBuilder() {
      private final WorldPersistenceLogger logger = new WorldPersistenceLogger();
      private String branchName = "main";
      private String commitId;
      private final List<String> blobTypes = new ArrayList<>();
      
      @Override
      public WorldBuilder lockWithCommit(String commitId) {
        this.commitId = commitId;
        return this;
      }
      @Override
      public WorldBuilder docs(BodyType... type) {
        this.blobTypes.addAll(Arrays.asList(type).stream().map(e -> e.name()).toList());
        return this;
      }
      
      @Override
      public <T> Uni<T> build(Function<NextWorld, T> mergeFunction) {
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

            final var nextWorld = new NextWorldImpl(tenant, ref);
            final var mapped = mergeFunction.apply(nextWorld);
            final var commitBuilder = nextWorld.close();
            
            
            logger.stage4NextState();
            
            return commitBuilder
                .branchName(branchName)
                .branchLock(ref.getCommitId())
                .queryHeadOnly()
                .build()
                .onItem().transform(commited -> mapped)
                .onFailure().invoke((e) -> logger.closeWithFailure(e))
                .onItem().invoke(() -> logger.close());

          });
      }
    };
  }

  
  
  public static class NextWorldImpl implements NextWorld {
    private final FileSystemTenant tenant;
    private final Ref ref;
    private final ModelWorld world;
    private final CommitBuilder commitBuilder;
    
    private String commitAuthor = "world-builder";
    private String commitMessage = "default-authoring";
        
    public NextWorldImpl(FileSystemTenant tenant, Ref ref) {
      super();
      this.tenant = tenant;
      this.ref = ref;
      this.world = WorldPersistenceMapper.mapFrom(ref);
      this.commitBuilder = tenant.commitBuilder();
    }

    public CommitBuilder close() {
      return commitBuilder.commitAuthor(commitAuthor).commitMessage(commitMessage);
    }

    @Override
    public ModelWorld getCurrentWorld() {
      return world;
    }

    @Override
    public <T extends Body> Model<T> newModel(String name, T body) {
      final var id = OidUtils.genUUID();
      this.commitBuilder.newFile(newFile -> {
        newFile
          .fileId(id)
          .fileName(name)
          .fileValue(JsonObject.mapFrom(body))
          .fileType(body.getBodyType().name())
          .build()
        ;
      });

      
      return ImmutableModel.<T>builder()
          .id(id)
          .body(body)
          .bodyType(body.getBodyType())
          .bodyHash("not possible at a time")
          .build();
    }
    @Override
    public <T extends Body> Model<T> mergeModel(String id, T body) {
      // TODO Auto-generated method stub
      return null;
    }
  }
  
  
  public static class WorldLockException extends RuntimeException {
    
  }
}
