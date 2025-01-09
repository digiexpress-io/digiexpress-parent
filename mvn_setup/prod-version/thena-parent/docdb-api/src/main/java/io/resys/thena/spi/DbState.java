package io.resys.thena.spi;

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

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.structures.doc.DocState;
import io.resys.thena.structures.git.GitState;
import io.resys.thena.structures.grim.GrimState;
import io.resys.thena.structures.org.OrgState;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

public interface DbState {
  ThenaDataSource getDataSource();
  InternalTenantQuery tenant();
  
  Uni<GrimState> toGrimState(String tenantId);
  GrimState toGrimState(Tenant repo);
  <R> Uni<R> withGrimTransaction(TxScope tenantId, GrimState.TransactionFunction<R> callback);
  
  
  Uni<GitState> toGitState(String tenantId);
  GitState toGitState(Tenant repo);
  <R> Uni<R> withGitTransaction(TxScope tenantId, GitState.TransactionFunction<R> callback);
  
  Uni<DocState> toDocState(String tenantId);
  DocState toDocState(Tenant repo);
  <R> Uni<R> withDocTransaction(TxScope tenantId, DocState.TransactionFunction<R> callback);
  
  Uni<OrgState> toOrgState(String tenantId);
  OrgState toOrgState(Tenant repo);
  <R> Uni<R> withOrgTransaction(TxScope tenantId, OrgState.TransactionFunction<R> callback);
  
  interface InternalTenantQuery {
    Uni<Tenant> getByName(String name);
    Uni<Tenant> getByNameOrId(String nameOrId);
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
