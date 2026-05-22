package io.resys.thena.client.sample2;

import java.util.Collection;

/*-
 * #%L
 * thena-batch-client
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

import io.resys.thena.api.annotations.TenantSql;

import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.resys.thena.datasource.ThenaSqlClient.SqlTupleList;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "customer",
  order = 0,
  ddl = """
    CREATE TABLE IF NOT EXISTS {customer}
    (
      id                           VARCHAR(40) PRIMARY KEY,
      customer_name                TEXT NOT NULL,

      customer_updated_at          TIMESTAMP WITH TIME ZONE,
      customer_updated_by          TEXT
    );

  """,
  constraints = """
    --- constraints for {customer}
  """,
  drop = """
    DROP TABLE {customer};
  """
)
public interface CustomerTable {

  @TenantSql.FindAll(
    sql = "SELECT * FROM {customer} inner join {batch}",
    rowMapper = CustomerMapper.class
  )
  Sql findAll();

  @TenantSql.Find(
    optional = false,
    sql = """
      SELECT *
        FROM {customer}
        WHERE id = $1
    """,
    rowMapper = CustomerMapper.class
  )
  SqlTuple getById(String id);

  @TenantSql.InsertAll(
    sql = """
      INSERT INTO {customer}
      (id, customer_name, 
       customer_updated_at, customer_updated_by)
       VALUES($1, $2, $3, $4)
    """,
    propsMapper = CustomerInsertMapper.class
  )
  SqlTupleList insertMany(List<Customer> users);

  @TenantSql.UpdateAll(
    sql = """
      UPDATE {customer}
       SET customer_name = $1, 
           customer_updated_at = $2, customer_updated_by = $3
       WHERE id = $7
    """,
    propsMapper = CustomerUpdateMapper.class
  )
  SqlTupleList updateMany(List<Customer> users);


  @TenantSql.Delete(
    sql = "DELETE FROM {customer} WHERE id = $1",
    propsMapper = CustomerIdMapper.class
  )
  SqlTuple deleteById(String id);

  @TenantSql.DeleteAll(
    sql = "DELETE FROM {customer} WHERE id = $1",
    propsMapper = CustomerDeleteMapper.class
  )
  SqlTupleList deleteAll(Collection<Customer> missionId);
  

  // Mapper classes
  class CustomerMapper implements TenantSql.RowMapper<Customer> {
    @Override
    public Customer apply(Row row) {
      // Implementation uses defaultMapper()
      return null;
    }
  }

  class CustomerInsertMapper implements TenantSql.PropsMapper<Customer> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Customer doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getId(),
        doc.getCustomerName(),
        doc.getUpdatedAt().orElse(null),
        doc.getUpdatedBy().orElse(null)
      });
    }
  }

  class CustomerUpdateMapper implements TenantSql.PropsMapper<Customer> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Customer doc) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[]{
        doc.getCustomerName(),
        doc.getUpdatedAt().orElse(null),
        doc.getUpdatedBy().orElse(null),
        doc.getId()
      });
    }
  }

  class CustomerIdMapper implements TenantSql.PropsMapper<String> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(String id) {
      return io.vertx.mutiny.sqlclient.Tuple.of(id);
    }
  }
  class CustomerDeleteMapper implements TenantSql.PropsMapper<Customer> {
    @Override
    public io.vertx.mutiny.sqlclient.Tuple apply(Customer customers) {
      return io.vertx.mutiny.sqlclient.Tuple.from(new Object[] {
        customers.getId()
      });
    }
  }
}
