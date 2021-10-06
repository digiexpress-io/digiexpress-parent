package io.resys.hdes.client.api.exceptions;

/*-
 * #%L
 * hdes-client-api
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

import io.resys.hdes.client.api.ast.AstDataType;

public class DataTypeException extends RuntimeException {
  private static final long serialVersionUID = 1479713119727436525L;
  private final AstDataType dataType;
  private final Object value;

  public DataTypeException(AstDataType dataType, Object value, Exception e) {
    super(e.getMessage(), e);
    this.dataType = dataType;
    this.value = value;
  }

  public AstDataType getDataType() {
    return dataType;
  }

  public Object getValue() {
    return value;
  }
}
