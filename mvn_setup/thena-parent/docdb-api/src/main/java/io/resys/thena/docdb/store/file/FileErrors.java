package io.resys.thena.docdb.store.file;

import java.util.ArrayList;
import java.util.Arrays;

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
  public void deadEnd(String additionalMsg, Throwable e) {
    log.error(additionalMsg, e);
  }

  @Override
  public void deadEnd(String additionalMsg) {
    log.error(additionalMsg);
  }

  @Override
  public boolean isLocked(Throwable e) {
    return false;
  }

  @Override
  public void deadEnd(String additionalMsg, Throwable e, Object... args) {
    final var allArgs = new ArrayList<>(Arrays.asList(args));
    allArgs.add(e);
    log.error(additionalMsg + System.lineSeparator() + e.getMessage(), allArgs.toArray());     
  }
}
