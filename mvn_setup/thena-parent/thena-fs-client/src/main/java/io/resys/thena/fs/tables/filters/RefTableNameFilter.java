package io.resys.thena.fs.tables.filters;

/*-
 * #%L
 * thena-fs-client
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

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;
import org.immutables.value.Value;

import io.resys.thena.api.annotations.TenantSql.SqlBuilder;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.fs.api.trees.NameExpressionBuilder;
import io.resys.thena.storesql.support.SqlStatement;
import io.resys.thena.support.TableUtils;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.annotation.Nullable;


@Value.Immutable
public interface RefTableNameFilter {
  @Nullable Consumer<NameExpressionBuilder> getNameExpr();
  @Nullable String getBranchId();
  
  final static class SQL implements SqlBuilder<RefTableNameFilter> {
    
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, RefTableNameFilter filter) {
      final var params = new ArrayList<Object>();
      
      final var index = new MutableInt(params.size());
      final var stmt = new SqlStatement();

      // Handle branch ID filter
      if (filter.getBranchId() != null) {
        stmt.append("(");
        
        try {
          final UUID uuid =  TableUtils.toUuid(filter.getBranchId());
          stmt.append("ref.ref_id = $").append(index.incrementAndGet());
          params.add(uuid);
        } catch(Exception e) {}
        
        stmt.append("ref.ref_name = $").append(index.incrementAndGet()).append(")");
        params.add(filter.getBranchId());
      }
      
      // Handle name expression filter
      if (filter.getNameExpr() != null) {
        if (!params.isEmpty()) {
          stmt.append(" AND ");
        }
        
        
        final var nameSql = new StringBuilder();
        final var nameBuilder = new NameExpressionBuilderImpl(
          "ref.ref_name",
          param -> {
            params.add(param);
            return index.incrementAndGet();
          },
          nameSql
        );
        
        filter.getNameExpr().accept(nameBuilder);
        nameBuilder.close();
        stmt.append(nameSql.toString());
      }
      
      final var result = stmt.toString();
      final var clause = (result.isBlank() ? "" : " WHERE ") + result;
      
      return ImmutableSqlTuple.builder()
          .value(baseline + clause)
          .props(Tuple.from(params))
          .build();
    }
  }
}
