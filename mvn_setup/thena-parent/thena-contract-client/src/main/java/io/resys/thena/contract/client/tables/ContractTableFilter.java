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
import java.util.UUID;
import java.util.stream.Collectors;

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

      // append reference numbers and other ids
      final boolean addRefNumbers = filter.getContractIds().isPresent();
      if(addRefNumbers) {
        baseline +=
"""
  LEFT JOIN ( 
  SELECT 
    inv_plan.contract_id,
    ARRAY_AGG(inv_plan.external_id::text) || ARRAY_AGG(inv_plan.inv_plan_ref_number::text) as refs
  FROM {inv_plan} as inv_plan
  GROUP BY 
    inv_plan.contract_id
  ) as ref_number_1 
  ON ref_number_1.contract_id = contract.id
"""; 
      }
      
      
      // main contract id filter
      if(filter.getContractIds().isPresent()) {
        builder.append("(")
          .append(" contract.id = ANY($").append(index++).append(")")
          .append(" OR contract.contract_number = ANY($").append(index).append(")")
          .append(" OR contract.external_id = ANY($").append(index).append(")")
          .append(" OR ref_number_1.refs && $").append(index++).append("")
          .append(")")
          .ln();
        
        final var uuid = filter.getContractIds().get()
          .stream().map(id -> {
            try {
              return UUID.fromString(id);
            } catch(Exception e) {
              // ignore
              return null;
            }
          })
          .filter(e -> e != null)
          .collect(Collectors.toList());
        
        params.add(uuid.toArray(new UUID[]{}));    
        params.add(filter.getContractIds().get().toArray(new String[]{}));
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
