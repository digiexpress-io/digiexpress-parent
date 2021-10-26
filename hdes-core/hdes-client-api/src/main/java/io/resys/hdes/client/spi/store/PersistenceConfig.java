package io.resys.hdes.client.spi.store;

import java.util.Collection;

/*-
 * #%L
 * stencil-persistence
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import org.immutables.value.Value;

import io.resys.hdes.client.api.HdesStore.StoreState;
import io.resys.hdes.client.api.ast.AstBody;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.thena.docdb.api.DocDB;
import io.resys.thena.docdb.api.actions.ObjectsActions.BlobObject;
import io.resys.thena.docdb.api.actions.ObjectsActions.ObjectsResult;
import io.smallrye.mutiny.Uni;
import io.resys.hdes.client.api.HdesStore.StoreEntity;

@Value.Immutable
public interface PersistenceConfig {
  DocDB getClient();
  String getRepoName();
  String getHeadName();
  AuthorProvider getAuthorProvider();
  
  @FunctionalInterface
  interface GidProvider {
    String getNextId(AstBodyType entity);
  }
  
  GidProvider getGidProvider();
  
  @FunctionalInterface
  interface Serializer {
    String toString(StoreEntity entity);
  }
  
  interface Deserializer {
    StoreEntity fromString(String value);
    <T extends AstBody> StoreEntity fromString(AstBodyType type, String value);
  }
  Serializer getSerializer();
  Deserializer getDeserializer();
  
  @FunctionalInterface
  interface AuthorProvider {
    String getAuthor();
  }
  
  @Value.Immutable
  interface EntityState {
    ObjectsResult<BlobObject> getSrc();
    StoreEntity getEntity();
  }
  
  interface Commands {
    Uni<StoreEntity> delete(StoreEntity toBeDeleted);
    Uni<StoreState> get();
    Uni<EntityState> getEntityState(String id);
    Uni<StoreEntity> save(StoreEntity toBeSaved);
    Uni<Collection<StoreEntity>> save(Collection<StoreEntity> toBeSaved);
  }  
}