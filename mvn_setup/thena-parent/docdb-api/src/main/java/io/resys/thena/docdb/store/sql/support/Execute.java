package io.resys.thena.docdb.store.sql.support;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.resys.thena.docdb.store.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTupleList;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;

public class Execute {
  private static final Logger LOGGER = LoggerFactory.getLogger(Execute.class);

  public static Uni<RowSet<Row>> apply(SqlClient client, Sql sql) {
    return client.preparedQuery(sql.getValue()).execute()
      .onFailure().invoke(e -> {
      LOGGER.error(System.lineSeparator() +
          "Failed to execute command." + System.lineSeparator() +
          "  sql: " + sql.getValue() + System.lineSeparator() +
          "  error:" + e.getMessage(), e);
    });
  }
  
  public static Uni<RowSet<Row>> apply(SqlClient client, SqlTuple sql) {
    return client.preparedQuery(sql.getValue()).execute(sql.getProps())
        .onFailure().invoke(e -> {
          LOGGER.error(System.lineSeparator() +
              "Failed to execute single command." + System.lineSeparator() +
              "  sql: " + sql.getValue() + System.lineSeparator() +
              "  error:" + e.getMessage(), e);
        });
  }
  public static Uni<RowSet<Row>> apply(SqlClient client, SqlTupleList sql) {
    if(sql.getProps().isEmpty()) {
      LOGGER.info(System.lineSeparator() +
          "Skipping batch command with no values to update or insert." + System.lineSeparator() +
          "  sql: " + sql.getValue() + System.lineSeparator());
      return Uni.createFrom().nullItem();
    }
    
    return client.preparedQuery(sql.getValue()).executeBatch(sql.getProps())
        .onFailure().invoke(e -> {
          LOGGER.error(System.lineSeparator() +
              "Failed to execute batch command." + System.lineSeparator() +
              "  sql: " + sql.getValue() + System.lineSeparator() +
              "  error:" + e.getMessage(), e);
        });
  }
}
