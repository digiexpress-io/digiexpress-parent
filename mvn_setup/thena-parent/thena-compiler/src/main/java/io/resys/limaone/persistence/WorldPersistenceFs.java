package io.resys.limaone.persistence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.Body;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.api.FileSystem.FileSystemTenant;
import io.resys.thena.fs.api.commits.CommitBuilder;
import io.resys.thena.fs.entities.Ref;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class WorldPersistenceFs implements WorldPersistence {

  private final FileSystem fileSystem;
  
  @Override
  public WorldBuilder worldBuilder() {
    return new WorldBuilder() {
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
        return tenant.branchQuery()
          .branchId(branchName)
          .excludeBlobs(false)
          .excludeNodes(false)
          .getOne()
          .onItem().transformToUni(ref -> {
            if(commitId != null && !ref.getCommitId().equals(commitId)) {
              throw new WorldLockException();
            }

            final var nextWorld = new NextWorldImpl(tenant, ref);
            final var mapped = mergeFunction.apply(nextWorld);
            final var commitBuilder = nextWorld.close();
            
            return commitBuilder
                .branchName(branchName)
                .branchLock(ref.getCommitId())
                .build()
                .onItem().transform(commited -> mapped);
          });
      }
    };
  }

  
  @RequiredArgsConstructor
  public static class NextWorldImpl implements NextWorld {
    
    private final FileSystemTenant tenant;
    private final Ref ref;
    
    private String commitAuthor = "world-builder";
    private String commitMessage = "default-authoring";
    
    
    
    public CommitBuilder close() {
      return null;
    }



    @Override
    public ModelWorld getCurrentWorld() {
      // TODO Auto-generated method stub
      return null;
    }



    @Override
    public <T extends Body> Model<T> newModel(T body) {
      // TODO Auto-generated method stub
      return null;
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
