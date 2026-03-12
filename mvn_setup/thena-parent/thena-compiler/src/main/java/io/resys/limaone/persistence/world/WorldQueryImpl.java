package io.resys.limaone.persistence.world;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import io.resys.limaone.authoring.Authoring.WorldQuery;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.entities.Tag;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorldQueryImpl implements WorldQuery {
  private final ScheduledExecutorService workerPool;
  private final Duration workerTimeout;
  
  private final FileSystem filesystem;
  private final List<BodyType> userTypes = new ArrayList<>();
  
  @Override
  public WorldQuery docTypes(BodyType... userTypes) {
    Objects.requireNonNull(userTypes, () -> "docTypes can't be null");
    this.userTypes.addAll(Arrays.asList(userTypes));
    return this;
  }

  @Override
  public Uni<ModelWorld> findAll() {
    if(userTypes.isEmpty()) {
      userTypes.addAll(Arrays.asList(BodyType.values()));
    }
    
    final var blobTypes = userTypes.stream()
      .map(e -> e.name())
      .toList()
      .toArray(new String[]{});
    
    final var tenant = filesystem.withTenant();
    final var refUni = tenant
        .branchQuery()
        .branchName(name -> name.equals(WorldBuilderImpl.branchName))
        .blobTypes(blobTypes)
        .excludeBlobs(false)
        .excludeNodes(false)
        .getOne();
    
    final Uni<List<Tag>> tagsUni = userTypes.contains(BodyType.DEPLOYMENT) ?  
        tenant.tagQuery().findAll().collect().asList() :
        Uni.createFrom().item(Collections.emptyList());
    
    return Uni.combine().all().unis(refUni, tagsUni)
      .asTuple().onItem().transform(tuple -> WorldPersistenceMapper.mapFrom(tuple.getItem1(), tuple.getItem2()));
  }

  @Override
  public ModelWorld findAllSync() {
    return this.findAll()
        .runSubscriptionOn(workerPool)
        .await().atMost(workerTimeout);
  }
}
