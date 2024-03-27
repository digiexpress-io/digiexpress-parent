package io.resys.thena.storefile;

import io.resys.thena.datasource.TenantTableNames;
import io.resys.thena.datasource.ThenaSqlDataSourceErrorHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileErrors implements ThenaSqlDataSourceErrorHandler {

  @Override
  public boolean notFound(Throwable e) {
    log.error(e.getMessage(), e);
    return false;
  }

  @Override
  public boolean duplicate(Throwable e) {
    log.error(e.getMessage(), e);
    return false;
  }

  @Override
  public boolean isLocked(Throwable e) {
    return false;
  }

	@Override
	public void deadEnd(SqlTupleFailed e) {
		log.error(e.getMessage(), e);
	}

	@Override
	public void deadEnd(SqlSchemaFailed e) {
		log.error(e.getMessage(), e);
	}

	@Override
	public void deadEnd(SqlFailed e) {
		log.error(e.getMessage(), e);
	}

	@Override
	public void deadEnd(SqlTupleListFailed e) {
		log.error(e.getMessage(), e);
	}

  @Override
  public ThenaSqlDataSourceErrorHandler withOptions(TenantTableNames options) {
    return this;
  }
}
