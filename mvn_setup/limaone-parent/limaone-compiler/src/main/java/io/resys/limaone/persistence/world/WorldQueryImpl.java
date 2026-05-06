package io.resys.limaone.persistence.world;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import io.resys.limaone.authoring.Authoring.WorldQuery;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.thena.fs.api.FileSystem;
import io.resys.thena.fs.entities.Tag;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WorldQueryImpl implements WorldQuery {
  private final ScheduledExecutorService workerPool;
  private final Duration workerTimeout;
  private final FileSystem filesystem;
  private final FormDb formDb;
  private final List<BodyType> userTypes = new ArrayList<>();
  private final List<String> objectIds = new ArrayList<>();
  private final String branchName;
  private UUID commitId;
  

  @Override
  public WorldQuery commitId(UUID commitId) {
    this.commitId = commitId;
    return this;
  }
  @Override
  public WorldQuery addObjectId(String objectId) {
    this.objectIds.add(objectId);
    return this;
  }
  @Override
  public WorldQuery docs(BodyType... userTypes) {
    Objects.requireNonNull(userTypes, () -> "docTypes can't be null");
    this.userTypes.addAll(Arrays.asList(userTypes));
    return this;
  }

  @Override
  public Uni<Model<?>> getOneById(String objectId) {
    objectIds.add(objectId);
    return findAll().onItem().transform(world -> world.findAnyObject(objectId))
        .map(model -> model.orElseThrow(() -> new WorldQueryException("Could not find object by id: " + objectId)));
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
        .commitId(commitId)
        .branchName(name -> name.equals(branchName))
        .blobTypes(blobTypes)
        .docIds(objectIds)
        .getOne();
    
    final Uni<List<Tag>> tagsUni = userTypes.contains(BodyType.DEPLOYMENT) ?  
        tenant.tagQuery().findAll().collect().asList() :
        Uni.createFrom().item(Collections.emptyList());
    
    return Uni.combine().all().unis(refUni, tagsUni).asTuple()
      .onItem().transformToUni(tuple -> {
        
        if(userTypes.contains(BodyType.DIALOB_FORM)) {
          return WorldFactory
            .from(Optional.ofNullable(tuple.getItem1()))
            .addTags(tuple.getItem2())
            .buildWithForms(formDb);
        }
        final var world = WorldFactory
            .from(Optional.ofNullable(tuple.getItem1()))
            .addTags(tuple.getItem2())
            .build();
        return Uni.createFrom().item(world);
      });
  }

  @Override
  public ModelWorld findAllSync() {
    return this.findAll()
        .runSubscriptionOn(workerPool)
        .await().atMost(workerTimeout);
  }
  
  
  public static class WorldQueryException extends RuntimeException {
    private static final long serialVersionUID = -3132760363608981079L;
    
    public WorldQueryException(String msg) {
      super(msg);
    }
  }

}
