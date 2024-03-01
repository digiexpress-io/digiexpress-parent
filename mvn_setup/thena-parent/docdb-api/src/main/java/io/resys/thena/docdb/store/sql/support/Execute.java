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

import io.resys.thena.docdb.store.sql.SqlBuilder.Sql;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTuple;
import io.resys.thena.docdb.store.sql.SqlBuilder.SqlTupleList;
import io.resys.thena.docdb.support.ErrorHandler.SqlExecutionFailed;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.SqlClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Execute {

	private static class SQLExecutionExceptionForTracingStack extends RuntimeException {
		private static final long serialVersionUID = -6960481243464191887L;
	}

  public static Uni<RowSet<Row>> apply(SqlClient client, Sql sql) {
  	final var failFrom = new SQLExecutionExceptionForTracingStack();
  	
    return client.preparedQuery(sql.getValue()).execute()
      .onFailure().transform(e -> {
      	
      failFrom.addSuppressed(e);
      final var msg = System.lineSeparator() +
          "Failed to execute SQL command." + System.lineSeparator() +
          "  message: " + e.getMessage() +
          "  sql: " + sql.getValue() + System.lineSeparator();
      
      log.error(msg, failFrom);
      return new SqlExecutionFailed(msg, failFrom);
    });
  }
  
  public static Uni<RowSet<Row>> apply(SqlClient client, SqlTuple sql) {
  	final var failFrom = new SQLExecutionExceptionForTracingStack();
  	
    return client.preparedQuery(sql.getValue()).execute(sql.getProps())
        .onFailure().transform(e -> {
          failFrom.addSuppressed(e);
        	final var msg = System.lineSeparator() +
              "Failed to execute single SQL command." + System.lineSeparator() +
              "  message: " + e.getMessage() +
              "  sql: " + sql.getValue() + System.lineSeparator() +
              "  props:" + sql.getProps().deepToString() + System.lineSeparator();
        	
          log.error(msg, failFrom);
          return new SqlExecutionFailed(msg, failFrom); 
        });
  }
  public static Uni<RowSet<Row>> apply(SqlClient client, SqlTupleList sql) {
    if(sql.getProps().isEmpty()) {
      log.trace(System.lineSeparator() +
          "Skipping batch SQL command with no values to update or insert." + System.lineSeparator() +
          "  sql: " + sql.getValue() + System.lineSeparator());
      return Uni.createFrom().nullItem();
    }
    final var failFrom = new SQLExecutionExceptionForTracingStack();
    return client.preparedQuery(sql.getValue()).executeBatch(sql.getProps())
        .onFailure().transform(e -> {
          failFrom.addSuppressed(e);
          
          final var entries = new StringBuilder();
        	var index = 0;
        	for(final var tuple : sql.getProps()) {
        		entries.append(
        				"  props[" + index++ + "]" + System.lineSeparator() + 
        				"  " + tuple.deepToString()  + System.lineSeparator());
        	}
      		final var msg = System.lineSeparator() +
          "Failed to execute batch SQL command." + System.lineSeparator() +
          "  message: " + e.getMessage() + System.lineSeparator() +
          "  sql: " + sql.getValue() +
          entries;
        	
        	
          log.error(msg, failFrom);
          return new SqlExecutionFailed(msg, failFrom); 
        });
  }
}
