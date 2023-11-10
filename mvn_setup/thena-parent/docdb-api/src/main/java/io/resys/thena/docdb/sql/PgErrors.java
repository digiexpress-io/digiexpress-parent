package io.resys.thena.docdb.sql;

/*-
 * #%L
 * thena-docdb-pgsql
 * %%
 * Copyright (C) 2021 - 2023 Copyright 2021 ReSys OÜ
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
import java.util.Arrays;

import io.resys.thena.docdb.spi.ErrorHandler;
import io.vertx.pgclient.PgException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PgErrors implements ErrorHandler {
  
  public boolean notFound(Throwable e) {
    if(e instanceof PgException) {
      PgException ogre = (PgException) e;
      
      return "42P01".equals(ogre.getCode());
    }
    return false;
  }
  
  public boolean duplicate(Throwable e) {
    if(e instanceof PgException) {
      PgException ogre = (PgException) e;
      
      return "23505".equals(ogre.getCode());
    }
    return false;
  }
  
  @Override
  public boolean isLocked(Throwable e) {
    if(e instanceof PgException) {
      PgException ogre = (PgException) e;
      return "55P03".equals(ogre.getCode());
    }
    return false;
  }
  
  public void deadEnd(String additionalMsg, Throwable e) {
    log.error(System.lineSeparator() + 
        "  - message: " + additionalMsg + System.lineSeparator() +
        "  - exception: " + e.getMessage(), e);
  }
  
  public void deadEnd(String additionalMsg) {
    log.error(additionalMsg);
  }

  @Override
  public void deadEnd(String additionalMsg, Throwable e, Object... args) {
    final var allArgs = new ArrayList<>(Arrays.asList(args));
    allArgs.add(e);
    log.error(additionalMsg + System.lineSeparator() + e.getMessage(), allArgs.toArray()); 
  }
  
  
}
