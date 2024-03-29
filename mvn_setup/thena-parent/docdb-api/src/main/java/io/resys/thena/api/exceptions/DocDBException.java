package io.resys.thena.api.exceptions;

/*-
 * #%L
 * thena-docdb-api
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

public class DocDBException extends RuntimeException {

  private static final long serialVersionUID = -5933566310053854060L;

  public DocDBException() {
    super();
  }

  public DocDBException(String message, Throwable cause) {
    super(message, cause);
  }

  public DocDBException(String message) {
    super(message);
  }

  public DocDBException(Throwable cause) {
    super(cause);
  }

}
