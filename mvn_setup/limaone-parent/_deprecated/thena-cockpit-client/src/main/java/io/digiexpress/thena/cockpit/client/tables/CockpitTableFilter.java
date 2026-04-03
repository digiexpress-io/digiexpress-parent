package io.digiexpress.thena.cockpit.client.tables;

/*-
 * #%L
 * thena-cockpit-client
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

import java.util.ArrayList;
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
public interface CockpitTableFilter {
  Optional<List<String>> getCockpitConfigIds();
  Optional<List<String>> getCockpitConfigNames();
  Boolean getLockForUpdate();
  
  final static class SQL implements SqlBuilder<CockpitTableFilter> {
    
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, CockpitTableFilter filter) {
      final var builder = new SqlStatement();
      final var params = new ArrayList<Object>();
      int index = 1;

      // config id filter
      if(filter.getCockpitConfigIds().isPresent()) {
        builder.append("(")
          .append(" cockpit_config.id = ANY($").append(index++).append(")")
          .append(" OR cockpit_config.external_id = ANY($").append(index++).append(")")
          .append(")")
          .ln();
        
        final var uuid = filter.getCockpitConfigIds().get()
          .stream().map(id -> {
            try {
              return UUID.fromString(id);
            } catch(Exception e) {
              return null;
            }
          })
          .filter(e -> e != null)
          .collect(Collectors.toList());
        
        params.add(uuid.toArray(new UUID[]{}));    
        params.add(filter.getCockpitConfigIds().get().toArray(new String[]{}));
      }
      
      // config name filter
      if(filter.getCockpitConfigNames().isPresent()) {
        if(!builder.toString().isBlank()) {
          builder.append(" AND ");
        }
        builder.append("cockpit_config.cockpit_config_name = ANY($").append(index++).append(")")
          .ln();
        
        params.add(filter.getCockpitConfigNames().get().toArray(new String[]{}));
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