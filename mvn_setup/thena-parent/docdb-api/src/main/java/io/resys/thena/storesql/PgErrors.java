package io.resys.thena.storesql;

import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import io.vertx.pgclient.PgException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class PgErrors implements ThenaSqlDataSourceErrorHandler {
  @SuppressWarnings("unused")
  private final TenantTableNames options;
  
  public boolean notFound(Throwable e) {
    if(e instanceof PgException) {
      PgException ogre = (PgException) e;
      
      return "42P01".equals(ogre.getSqlState());
    }
    return false;
  }
  
  public boolean duplicate(Throwable e) {
    if(e instanceof PgException) {
      PgException ogre = (PgException) e;
      
      return "23505".equals(ogre.getSqlState());
    }
    return false;
  }
  
  @Override
  public boolean isLocked(Throwable e) {
    if(e instanceof PgException) {
      PgException ogre = (PgException) e;
      return "55P03".equals(ogre.getSqlState());
    }
    return false;
  }
  
  public static void deadEnd(String additionalMsg, Throwable e) {
    log.error(System.lineSeparator() + 
        "  - message: " + additionalMsg + System.lineSeparator() +
        "  - exception: " + e.getMessage(), e);
  }
  
  public void deadEnd(String additionalMsg) {
    log.error(additionalMsg);
  }

	@Override
	public void deadEnd(SqlTupleFailed e) {
		final var sql = e.getSql();
  	final var msg = System.lineSeparator() +
        "Failed to execute SQL query." + System.lineSeparator() +
        "  message: " + e.getMessage() + System.lineSeparator() +
        "  sql: " + sql.getValue() + System.lineSeparator() +
        "  props:" + sql.getProps().deepToString() + System.lineSeparator();
    log.error(msg, e);
	}

	@Override
	public void deadEnd(SqlSchemaFailed e) {
		final var sql = e.getSql();
  	final var msg = System.lineSeparator() +
        "Failed to execute SQL query." + System.lineSeparator() +
        "  message: " + e.getMessage() +
        "  sql: " + sql + System.lineSeparator();
    log.error(msg, e);		
	}

	@Override
	public void deadEnd(SqlFailed e) {
		final var sql = e.getSql();
  	final var msg = System.lineSeparator() +
        "Failed to execute SQL query." + System.lineSeparator() +
        "  message: " + e.getMessage() +
        "  sql: " + sql.getValue() + System.lineSeparator();
    log.error(msg, e);		
	}

	@Override
	public void deadEnd(SqlTupleListFailed e) {
		final var sql = e.getSql();
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
  	
    log.error(msg, e);
	}

  @Override
  public ThenaSqlDataSourceErrorHandler withOptions(TenantTableNames options) {
    return new PgErrors(options);
  }
}
