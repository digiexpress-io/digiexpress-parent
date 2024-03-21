package io.resys.thena.docdb.storefile;

import io.resys.thena.docdb.support.ErrorHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileErrors implements ErrorHandler {

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
}
