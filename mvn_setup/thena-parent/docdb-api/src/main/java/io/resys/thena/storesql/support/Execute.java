package io.resys.thena.storesql.support;

import io.resys.thena.storesql.SqlBuilder.Sql;
import io.resys.thena.storesql.SqlBuilder.SqlTuple;
import io.resys.thena.storesql.SqlBuilder.SqlTupleList;
import io.resys.thena.support.ErrorHandler.SqlExecutionFailed;
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
