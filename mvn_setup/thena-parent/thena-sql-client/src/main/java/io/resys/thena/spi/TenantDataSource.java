package io.resys.thena.spi;

import java.util.Optional;
import java.util.UUID;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.entities.Alias;
import io.resys.thena.api.entities.Member;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ThenaDataSource;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;



public interface TenantDataSource {
  ThenaDataSource getDataSource();
  InternalTenantQuery tenant();
  InternalAliasQuery alias();
  InternalMemberQuery member();
  
  
  interface InternalAliasQuery {
    Uni<Alias> getByName(String name);
    Uni<Alias> getByNameOrId(String nameOrId);
    Uni<Optional<Alias>> findByNameOrId(String nameOrId);
    Multi<Alias> findAll();
    Uni<Alias> delete(Alias existing);
    Uni<Alias> insert(Alias newAlias);
    Uni<Alias> merge(Alias existing);
  }
  
  interface InternalMemberQuery {
    Uni<Optional<Member>> findById(UUID id);
    Uni<Member> getById(UUID id);
    Uni<Optional<Member>> findByExtIdAndAliasId(String extId, UUID uuid);
    
    Multi<Member> findByExtIdAndAliasIdAndRef(String extId, UUID uuid, String ref);
    
    Multi<Member> findAllByExtId(String externalId);
    Multi<Member> findAll();
    Uni<Member> delete(Member existing);
    Uni<Member> insert(Member newMember);
    Uni<Member> merge(Member existing);
    
  }
  
  interface InternalTenantQuery {
    Uni<Tenant> getByName(String name);
    Uni<Tenant> getByNameOrId(String nameOrId);
    Uni<Optional<Tenant>> findByNameOrId(String nameOrId);
    
    Multi<Tenant> findAllWithLabels();
    Multi<Tenant> findAll();
    Uni<Void> delete();
    Uni<Tenant> delete(Tenant newRepo);
    Uni<Tenant> insert(Tenant newRepo);
  }
  
  @Value.Immutable
  interface TxScope {
    String getTenantId();
    String getCommitAuthor();
    String getCommitMessage();
  }
}
