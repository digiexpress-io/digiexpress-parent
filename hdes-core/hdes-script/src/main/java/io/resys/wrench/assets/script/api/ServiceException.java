package io.resys.wrench.assets.script.api;

/*-
 * #%L
 * hdes-script
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import io.resys.hdes.client.api.ast.ServiceAstType;

public class ServiceException extends RuntimeException {

  private static final long serialVersionUID = 4696840189119749118L;

  public ServiceException(ServiceAstType type, String message, Throwable cause) {
    super(formatMessage(type, message), cause);
  }

  public ServiceException(ServiceAstType type, String message) {
    super(formatMessage(type, message));
  }

  
  private static String formatMessage(ServiceAstType type, String msg) {
    return "Exception on service: '" + type.getName() + "'" + System.lineSeparator() + msg;
  }
}
