package io.resys.thena.docdb.file;

import java.util.ArrayList;
import java.util.Arrays;

/*-
 * #%L
 * thena-docdb-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import io.resys.thena.docdb.spi.ErrorHandler;
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
