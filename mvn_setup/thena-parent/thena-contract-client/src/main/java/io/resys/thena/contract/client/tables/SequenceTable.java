package io.resys.thena.contract.client.tables;

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

import io.resys.thena.api.annotations.TenantSql;
import io.resys.thena.api.annotations.TenantSql.WrapperType;
import io.resys.thena.datasource.ThenaSqlClient.Sql;
import io.resys.thena.datasource.ThenaSqlClient.SqlTuple;
import io.vertx.mutiny.sqlclient.Row;

@TenantSql.Table(
  name = "contract_seq",
  order = 200,
  ddl = """
    CREATE SEQUENCE {contract_seq} MINVALUE 1 MAXVALUE 999999 CYCLE;
  """,
  drop = """
    DROP SEQUENCE {contract_seq};
  """,
  constraints = ""
)
public interface SequenceTable {

  @TenantSql.Find(
    sql = "select nextval('{contract_seq}')",
    rowMapper = SequenceMapper.class,
    optional = false
  )
  Sql getNext();
  
  @TenantSql.FindAll(
    sql = "select nextval('{contract_seq}') from generate_series(1, $1)",
    rowMapper = SequenceMapper.class,
    wrapper = WrapperType.MULTI
  )
  SqlTuple findNext(long howMany);
  
  // Mapper classes
  class SequenceMapper implements TenantSql.RowMapper<Long> {
    @Override
    public Long apply(Row row) {
      return row.getLong(0);
      
    }
  }
}
