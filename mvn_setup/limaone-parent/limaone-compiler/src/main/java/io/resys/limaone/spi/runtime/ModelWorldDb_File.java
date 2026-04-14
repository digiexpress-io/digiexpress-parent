package io.resys.limaone.spi.runtime;

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

import java.util.Optional;
import java.util.UUID;

import io.resys.limaone.authoring.Authoring.WorldFsQuery;
import io.resys.limaone.authoring.Authoring.WorldIndexQuery;
import io.resys.limaone.authoring.Authoring.WorldQuery;
import io.resys.limaone.authoring.Authoring.WorldRef;
import io.resys.limaone.authoring.Authoring.WorldRefQuery;
import io.resys.limaone.model.Model;
import io.resys.limaone.model.Model.BodyType;
import io.resys.limaone.model.Model.ModelWorld;
import io.resys.limaone.model.Model.ModelWorldIndex;
import io.resys.limaone.persistence.ModelWorldDb;
import io.resys.limaone.spi.dialob.FormDb;
import io.resys.limaone.spi.runtime.DefaultEnvironmentProperties.WSP;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@SuppressWarnings("unused")
public class ModelWorldDb_File implements ModelWorldDb {
  
  private final WSP wsp;
  private final FormDb formDb;
  
  @Override
  public ModelWorldDb withBranchName(Optional<String> branchName) {
    return this;
  }
  @Override
  public ModelWorldDb withTenant(Optional<String> tenantName) {
    return this;
  }

  @Override
  public WorldQuery worldQuery() {
    final var world = wsp.getWorld();
    return new WorldQuery() {
      
      @Override
      public Uni<Model<?>> getOneById(String objectId) {
        return Uni.createFrom().item(world.findAnyObject(objectId).get());
      }
      
      @Override
      public ModelWorld findAllSync() {
        return world;
      }
      @Override
      public Uni<ModelWorld> findAll() {
        return Uni.createFrom().item(world);
      }
      
      @Override
      public WorldQuery docs(BodyType... types) {
        return this;
      }
      
      @Override
      public WorldQuery commitId(UUID commitId) {
        return this;
      }
    };
  }

  @Override
  public WorldBuilder worldBuilder() {
    throw new UnsupportedOperationException("Read only env. can't build new worlds!");
  }

  @Override
  public WorldIndexQuery worldIndexQuery() {
    return new WorldIndexQuery() {
      @Override
      public Multi<ModelWorldIndex> findAll() {
        return Multi.createFrom().empty();
      }
    };
  }
  @Override
  public CreateModelWorldDb createModelWorldDb() {
    throw new UnsupportedOperationException("Read only env. can't build new worlds!");
  }
  @Override
  public WorldRefQuery worldRefQuery() {
    return new WorldRefQuery() {
      @Override
      public Optional<WorldRef> findOneSync() {
        return Optional.empty();
      }
      @Override
      public Uni<Optional<WorldRef>> findOne() {
        return Uni.createFrom().item(Optional.empty());
      }
    };
  }
  @Override
  public WorldFsQuery worldFsQuery() {
    throw new UnsupportedOperationException("Read only env. can't build new worlds!");
  }

}
