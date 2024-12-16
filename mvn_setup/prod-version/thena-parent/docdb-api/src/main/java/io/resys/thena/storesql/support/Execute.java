package io.resys.thena.storesql.support;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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

import io.resys.thena.datasource.ThenaSqlClient;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler.SqlExecutionFailed;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Execute {

  private static class ThenaSqlBatchException extends RuntimeException {
    private static final long serialVersionUID = -6960481243464191887L;
    public ThenaSqlBatchException(Throwable e) {
      super(e);
    }
  }

  private static class ThenaStackDebug extends RuntimeException {
    private static final long serialVersionUID = -6960481243464191887L;
    public ThenaStackDebug(String thread) {
      super(thread, null, false, true);
    }
  }

  
  public static Uni<RowSet<Row>> apply(ThenaSqlClient client, ThenaSqlClient.Sql sql) {

    return client.preparedQuery(sql.getValue()).execute()
    .onFailure().transform(e -> {
      final var msg = System.lineSeparator() +
          "Failed to execute SQL command." + System.lineSeparator() +
          "  message: " + e.getMessage() +
          "  sql: " + System.lineSeparator() + sql.getValue() + System.lineSeparator();
      final var failFrom = fillStack(new ThenaSqlBatchException(e));
      log.error(msg, failFrom);
      return new SqlExecutionFailed(msg, failFrom);
    });
  }

  public static Uni<RowSet<Row>> apply(ThenaSqlClient client, ThenaSqlClient.SqlTuple sql) {
    return client.preparedQuery(sql.getValue()).execute(sql.getProps())
        .onFailure().transform(e -> {

          final var msg = System.lineSeparator() +
              "Failed to execute single SQL command." + System.lineSeparator() +
              "  message: " + e.getMessage() +
              "  props:" + sql.getProps().deepToString() + System.lineSeparator() +
              "  sql: " + System.lineSeparator() + sql.getValue();

          final var failFrom = fillStack(new ThenaSqlBatchException(e));
          log.error(failFrom.getMessage(), failFrom);
          return new SqlExecutionFailed(msg, failFrom); 
        });
  }
  public static Uni<RowSet<Row>> apply(ThenaSqlClient client, ThenaSqlClient.SqlTupleList sql) {
    if(sql.getProps().isEmpty()) {
      log.trace(System.lineSeparator() +
          "Skipping batch SQL command with no values to update or insert." + System.lineSeparator() +
          "  sql: " + sql.getValue() + System.lineSeparator());
      return Uni.createFrom().nullItem();
    }
    return client.preparedQuery(sql.getValue()).executeBatch(sql.getProps())
        .onFailure().transform(e -> {

          final var entries = new StringBuilder();
        	var index = 0;
        	for(final var tuple : sql.getProps()) {
        		entries.append(
        				"  - props[" + index++ + "] = " + tuple.deepToString()  + System.lineSeparator());
        	}
      		final var msg = System.lineSeparator() +
          "Failed to execute batch SQL command." + System.lineSeparator() +
          "  message: " + e.getMessage() + System.lineSeparator() +
          "  sql: " + System.lineSeparator() + sql.getValue() +
          entries;
        	
          final var failFrom = fillStack(new ThenaSqlBatchException(e));
          log.error(msg, failFrom);
          return new SqlExecutionFailed(msg, failFrom); 
        });
  }
  
  private static ThenaSqlBatchException fillStack(ThenaSqlBatchException ex) {

    for(final var stackEntry : Thread.getAllStackTraces().entrySet()) {
      final var relevantStack = new ArrayList<StackTraceElement>();
      for(final var stackElement : stackEntry.getValue()) {
        if(stackElement.getClassName().startsWith("io.resys")) {
          relevantStack.add(stackElement);
        }          
      }
      if(!relevantStack.isEmpty()) {
        final var debug = new ThenaStackDebug("thread name: '"+ stackEntry.getKey().getName() + "'");
        debug.setStackTrace(relevantStack.toArray(new StackTraceElement[] {}));
        ex.addSuppressed(debug);
      }
    }
    return ex;
  }
}
