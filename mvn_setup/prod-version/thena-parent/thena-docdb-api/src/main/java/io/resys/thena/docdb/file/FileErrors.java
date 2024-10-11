package io.resys.thena.docdb.file;

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

public class FileErrors implements ErrorHandler {

  @Override
  public boolean notFound(Throwable e) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean duplicate(Throwable e) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void deadEnd(String additionalMsg, Throwable e) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deadEnd(String additionalMsg) {
    // TODO Auto-generated method stub
    
  }

}
