package io.resys.thena.docdb.spi.pgsql;

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

import io.resys.thena.docdb.spi.ErrorHandler;
import io.vertx.pgclient.PgException;

public class PgErrors implements ErrorHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(PgErrors.class);
  
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
  
  public void deadEnd(String additionalMsg, Throwable e) {
    LOGGER.error(additionalMsg + System.lineSeparator() + e.getMessage(), e);
  }
  
  public void deadEnd(String additionalMsg) {
    LOGGER.error(additionalMsg);
  }
}
