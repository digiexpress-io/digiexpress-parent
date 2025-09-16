package io.resys.thena.doc.spi;

/*-
 * #%L
 * thena-doc-client
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
import io.resys.thena.datasource.ThenaSqlDataSource;
import io.resys.thena.doc.api.DocDataSource;
import io.resys.thena.doc.spi.builders.DocInternalTenantQueryImpl;
import io.resys.thena.support.RepoAssert.RepoAssertException;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class DocDataSourceImpl implements DocDataSource {
  private final ThenaSqlDataSource dataSource;

  @Override
  public ThenaDataSource getDataSource() {
    return dataSource;
  }
  @Override
  public InternalTenantQuery tenant() {
    return new DocInternalTenantQueryImpl(dataSource);
  }
  
  // doc state
  @Override
  public Uni<DocState> toDocState(String tenantId) {
    return tenant().getByNameOrId(tenantId).onItem().transformToUni(tenant -> {
      if(tenant == null) {
        return tenantNotFound(tenantId);
      }
      return Uni.createFrom().item(toDocState(tenant));
    });
  }
  @Override
  public DocState toDocState(Tenant repo) {
    return new DocDbStateImpl(dataSource.withTenant(repo));
  }
  @Override
  public <R> Uni<R> withDocTransaction(TxScope scope, TransactionFunction<R> callback) {
    return toDocState(scope.getTenantId()).onItem().transformToUni(state -> state.withTransaction(callback));
  }
  
  
  private <T> Uni<T> tenantNotFound(String tenantId) {
    return tenant().findAll().collect().asList().onItem().transform(tenants -> {
      final var text = new StringBuilder()
          .append("Doc tenant with name: '").append(tenantId).append("' does not exist!")
          .append(" known tenants: '").append(String.join(",", tenants.stream().map(r -> r.getName()).toList())).append("'")
          .toString();
      
      log.error(text);
      throw new RepoAssertException(text);
    }); 
  }
}
