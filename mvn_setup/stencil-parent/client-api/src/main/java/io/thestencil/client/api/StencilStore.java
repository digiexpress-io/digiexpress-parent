package io.thestencil.client.api;

/*-
 * #%L
 * stencil-client-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.api.envelope.DocContainer.DocObject;
import io.resys.thena.api.envelope.QueryEnvelope;
import io.resys.thena.spi.DocStore.StoreTenantQuery;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityBody;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilComposer.SiteState;

public interface StencilStore {
  <T extends EntityBody> Uni<Entity<T>> delete(Entity<T> toBeDeleted);
  <T extends EntityBody> Uni<EntityState<T>> get(String blobId, EntityType type);
  <T extends EntityBody> Uni<Entity<T>> save(Entity<T> toBeSaved);
  <T extends EntityBody> Uni<Entity<T>> create(Entity<T> toBeSaved);
  Uni<List<Entity<?>>> saveAll(List<Entity<?>> toBeSaved);
  Uni<List<Entity<?>>> batch(BatchCommand batch);
  StencilStore withTenantId(String tenantId);
  StencilQuery stencilQuery();
  StoreTenantQuery<? extends StencilStore> tenantQuery();

  @Value.Immutable
  interface EntityState<T extends EntityBody> {
    QueryEnvelope<DocObject> getSrc();
    Entity<T> getEntity();
  }
  
  interface StencilQuery {
    Uni<SiteState> head();
    <T extends EntityBody> Uni<List<Entity<T>>> head(List<String> ids, EntityType type);
  }
  

  @Value.Immutable
  @SuppressWarnings("rawtypes")
  interface BatchCommand {
    List<Entity> getToBeCreated();
    List<Entity> getToBeSaved();
    List<Entity> getToBeDeleted();
  }
}
