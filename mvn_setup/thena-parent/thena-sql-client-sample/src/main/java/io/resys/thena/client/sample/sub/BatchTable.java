package io.resys.thena.client.sample.sub;

/*-
 * #%L
 * thena-sql-client-sample
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

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.client.sample.entities.Batch;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "batch",
  order = 0,
  ddl = """
    CREATE TABLE IF NOT EXISTS {batch}
    (
      id                           VARCHAR(40) PRIMARY KEY,
      batch_name                   TEXT NOT NULL,
      UNIQUE(batch_name)
    );


  """,
  constraints = """
    --- constraints for {batch}
  """,
  drop = """
    DROP TABLE {batch};
  """
)
public interface BatchTable {

  @TenantSql.FindAll(
    sql = "SELECT * FROM {batch}",
    rowMapper = BatchMapper.class
  )
  Sql findAll();
  

  // Mapper classes
  class BatchMapper implements TenantSql.RowMapper<Batch> {
    @Override
    public Batch apply(Row row) {
      // Implementation uses defaultMapper()
      return null;
    }
  }

}
