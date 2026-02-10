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
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import io.resys.thena.api.annotations.TenantSql.SqlBuilder;
import io.resys.thena.api.entities.Tenant;
import io.resys.thena.datasource.ImmutableSqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.vertx.mutiny.sqlclient.Tuple;


@Value.Immutable
public interface RefTableLockFilter {
  Optional<List<String>> getDocIds();
  String getRefName();
  
  final static class SQL implements SqlBuilder<RefTableLockFilter> {

    
    @Override
    public SqlTuple apply(Tenant tenant, String baseline, RefTableLockFilter filter) {
      final var params = new ArrayList<Object>();
      params.add(filter.getRefName());
      params.add(filter.getDocIds().map(e -> e.toArray(new String[] {})).orElse(new String[] {}));

      return ImmutableSqlTuple.builder()
          .value(baseline)
          .props(Tuple.from(params))
          .build();
    }
  }
}
