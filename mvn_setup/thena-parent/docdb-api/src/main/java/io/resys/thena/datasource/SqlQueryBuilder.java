package io.resys.thena.datasource;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 Copyright 2021 ReSys OÜ
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

import org.immutables.value.Value;

import io.resys.thena.api.entities.Tenant;
import io.vertx.mutiny.sqlclient.Tuple;

public interface SqlQueryBuilder extends TenantTableNames.WithTenant<SqlQueryBuilder> {

  TenantSqlBuilder repo();

  SqlQueryBuilder withTenant(TenantTableNames options);

  interface TenantSqlBuilder {
    SqlTuple exists();
    Sql findAll();
    SqlTuple getByName(String name);
    SqlTuple getByNameOrId(String name);
    SqlTuple insertOne(Tenant repo);
    SqlTuple deleteOne(Tenant repo);
  }


  @Value.Immutable
  interface Sql {
    String getValue();
  }
  @Value.Immutable
  interface SqlTuple {
    String getValue();
    Tuple getProps();
    
    default String getPropsDeepString() {
      StringBuilder sb = new StringBuilder();
      sb.append("[");
      final int size = getProps().size();
      for (int i = 0; i < size; i++) {
        final var value = getProps().getValue(i);
        if(value instanceof String[]) {
          final var unwrapped = (String[]) value;
          sb.append("[")
          .append(String.join(",", unwrapped))
          .append("]");   
        } else {
          sb.append(value);
        }

        if (i + 1 < size)
          sb.append(",");
      }
      sb.append("]");
      return sb.toString();
    }
  }
  @Value.Immutable
  interface SqlTupleList {
    String getValue();
    List<Tuple> getProps();
  }
}
