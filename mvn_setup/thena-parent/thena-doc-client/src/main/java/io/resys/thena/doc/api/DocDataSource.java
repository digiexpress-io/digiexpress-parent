package io.resys.thena.doc.api;

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
import io.resys.thena.datasource.ThenaDataSource;
import io.resys.thena.spi.TenantDataSource;
import io.smallrye.mutiny.Uni;

public interface DocDataSource extends TenantDataSource {
  
  Uni<DocState> toDocState(String tenantId);
  DocState toDocState(Tenant repo);
  <R> Uni<R> withDocTransaction(TxScope tenantId, TransactionFunction<R> callback);

  
  
  interface DocState {
    String getTenantId();
    ThenaDataSource getDataSource();
    <R> Uni<R> withTransaction(TransactionFunction<R> callback);

    DocInserts insert();
    DocQueries query();
  }

  @FunctionalInterface
  interface TransactionFunction<R> {
    Uni<R> apply(DocState repoState);
  }
}
