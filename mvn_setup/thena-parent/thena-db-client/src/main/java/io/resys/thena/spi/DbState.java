package io.resys.thena.spi;

/*-
 * #%L
 * thena-db-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import io.resys.thena.api.entities.Tenant;
import io.resys.thena.structures.fs.FsState;
import io.resys.thena.structures.git.GitState;
import io.resys.thena.structures.org.OrgState;
import io.smallrye.mutiny.Uni;

public interface DbState extends TenantDataSource {

  Uni<FsState> toFsState(String tenantId);
  FsState toFsState(Tenant repo);
  <R> Uni<R> withFsTransaction(TxScope tenantId, FsState.TransactionFunction<R> callback);
  
  Uni<GitState> toGitState(String tenantId);
  GitState toGitState(Tenant repo);
  <R> Uni<R> withGitTransaction(TxScope tenantId, GitState.TransactionFunction<R> callback);
  
  Uni<OrgState> toOrgState(String tenantId);
  OrgState toOrgState(Tenant repo);
  <R> Uni<R> withOrgTransaction(TxScope tenantId, OrgState.TransactionFunction<R> callback);

}
