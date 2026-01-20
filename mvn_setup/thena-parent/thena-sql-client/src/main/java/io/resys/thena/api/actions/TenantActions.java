package io.resys.thena.api.actions;

/*-
 * #%L
 * thena-docdb-api
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

import java.util.List;

import org.immutables.value.Value;

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.api.entities.Tenant.StructureType;
import io.resys.thena.api.envelope.Message;
import io.resys.thena.api.envelope.ThenaEnvelope;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import jakarta.annotation.Nullable;

public interface TenantActions {

  TenantQuery queryTenants();
  CreateOneTenant createOneTenant();  
  Uni<Void> deleteAllTenants();

  interface TenantQuery {
    TenantQuery id(String id);
    TenantQuery rev(String rev);
    Multi<Tenant> findAll();
    Uni<Tenant> deleteOne();
    Uni<Tenant> getOne();
  }
  
  interface CreateOneTenant {
    CreateOneTenant externalId(String externalId); // optional can be null
    CreateOneTenant name(String name, StructureType type);
    CreateOneTenant name(String name);
    CreateOneTenant label(@Nullable String label);
    CreateOneTenant comment(@Nullable String comment);
    Uni<CreatedTenant> build();
    
    Uni<Tuple2<Boolean, CreatedTenant>> buildOnlyIfNotCreated();
  }
  
  enum TenantOperationStatus {
    OK, CONFLICT
  }
  
  @Value.Immutable
  interface CreatedTenant extends ThenaEnvelope {
    @Nullable
    Tenant getRepo();
    TenantOperationStatus getStatus();
    List<Message> getMessages();
  }

}
