package io.resys.thena.contract.client.tables;

import java.util.ArrayList;

/*-
 * #%L
 * thena-contract-client
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

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.annotations.TenantSql.SqlBuilder;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.storesql.support.SqlStatement;
import io.vertx.mutiny.sqlclient.Tuple;


@Value.Immutable
public interface ContractTableFilter {
  Optional<List<String>> getContractIds();
  
  Boolean getLockForUpdate();
  
  
  
  
  final static class SQL implements SqlBuilder<ContractTableFilter> {
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, ContractTableFilter filter) {
      final var builder = new SqlStatement();
      final var params = new ArrayList<Object>();
      int index = 1;
      
      if(filter.getContractIds().isPresent()) {
        builder.append("(")
          .append(" contract.id = ANY($").append(index).append(")")
          .append(" OR contract.contract_number = ANY($").append(index).append(")")
          .append(" OR contract.external_id = ANY($").append(index++).append(")")
          .append(")")
          .ln();
        params.add(filter.getContractIds().get().toArray());
      }
      
      final var result = builder.toString();
      final var clause = (result.isBlank() ? "" : " WHERE ") + builder.toString();
      return ImmutableSqlTuple.builder()
          .value(baseline + clause)
          .props(Tuple.from(params))
          .build();
    }
  }
}
