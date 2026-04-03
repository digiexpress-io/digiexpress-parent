package io.resys.thena.api.actions;

/*-
 * #%L
 * thena-sql-client
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


import java.util.List;
import java.util.UUID;

import org.immutables.value.Value;

import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Alias.AliasConfig;
import io.resys.thena.api.entities.Member;
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
  
  
  AliasQuery queryAliases();
  CreateOneAlias createOneAlias();
  ModifyOneAlias modifyOneAlias();
  
  MemberQuery queryMembers();
  CreateOneMember createOneMember();
  ModifyOneMember modifyOneMember();
  
  Uni<Void> deleteAllTenants();

  
  interface MemberQuery {
    MemberQuery externalId(String externalId);
    MemberQuery refTenant(@Nullable String refTenant);
    MemberQuery aliasId(@Nullable UUID aliasId);
    Multi<Member> findAll();
  }
  
  interface CreateOneMember {
    CreateOneMember externalId(String externalId);
    CreateOneMember aliasStatus(Boolean aliasStatus);
    CreateOneMember aliasId(UUID aliasId);
    Uni<Member> build();
  }
  
  interface ModifyOneMember {
    ModifyOneMember memberId(UUID memberId);
    ModifyOneMember aliasId(UUID aliasId);
    ModifyOneMember aliasStatus(Boolean aliasStatus);
    Uni<Member> build();
  }
  
  
  interface TenantQuery {
    TenantQuery id(String id);
    TenantQuery rev(String rev);
    Multi<Tenant> findAll();
    Uni<Tenant> deleteOne();
    Uni<Tenant> getOne();
  }
  

  interface CreateOneAlias {
    CreateOneAlias author(String author);
    
    // whats the real tenant where the data lives
    CreateOneAlias refTenantId(String refTenantId);
    // whats the real tenant where the data lives
    CreateOneAlias aliasTenantId(String aliasTenantId);
    
    
    CreateOneAlias aliasName(String aliasName);
    CreateOneAlias aliasDesc(String aliasDesc);
    
    CreateOneAlias aliasConfig(List<AliasConfig> config);
    
    Uni<Alias> build();
  }
  
  interface ModifyOneAlias {
    ModifyOneAlias author(String author);
    ModifyOneAlias aliasId(String aliasId);
    ModifyOneAlias aliasDesc(String aliasDesc);
    ModifyOneAlias aliasConfig(List<AliasConfig> config);
    Uni<Alias> build();
  }
  
  interface AliasQuery {
    AliasQuery id(UUID id);
    Multi<Alias> findAll();
    Uni<Alias> deleteOne();
    Uni<Alias> getOne();
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

  interface TenantAware<T extends TenantAware<T>> {
    TenantActions getActions();
    T withTenantDb(TenantDb db);
  }
  interface TenantDb {
    String getTenantByAnything(@Nullable String id);
    String getCurrentUserTenant();
  }
}
