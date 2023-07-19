/*
 * Copyright © 2015 - 2021 ReSys (info@dialob.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.dialob.client.spi.exceptions;

import io.dialob.client.api.DialobErrorHandler.DatabaseException;
import io.dialob.client.api.DialobErrorHandler.DialobClientException;

public class DocumentConflictException extends DatabaseException implements DialobClientException {

  private static final long serialVersionUID = 2488464005418137846L;

  public DocumentConflictException(String message) {
    super(message);
  }
}
