package io.resys.limaone.spi.program.expression;

/*-
 * #%L
 * limaone-compiler
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import io.vertx.core.json.JsonObject;

public class ExpressionException extends RuntimeException {

  private static final long serialVersionUID = -7154685569622201632L;

  private final JsonObject props;

  public ExpressionException(Object props, String message, Throwable cause) {
    super(message, cause);
    this.props = props == null ? null : JsonObject.mapFrom(props);
  }

  public ExpressionException(String message) {
    super(message);
    this.props = null;
  }
  public ExpressionException(String message, Throwable cause) {
    super(message, cause);
    this.props = null;
  }

  public ExpressionException(Object props, String message) {
    super(message);
    this.props = props == null ? null : JsonObject.mapFrom(props);
  }

  public JsonObject getCommand() {
    return props;
  }
}
