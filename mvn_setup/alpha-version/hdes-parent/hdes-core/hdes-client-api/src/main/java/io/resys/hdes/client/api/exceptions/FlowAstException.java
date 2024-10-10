package io.resys.hdes.client.api.exceptions;

/*-
 * #%L
 * wrench-assets-flow
 * %%
 * Copyright (C) 2016 - 2019 Copyright 2016 ReSys OÜ
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

public class FlowAstException extends AstException {

  private static final long serialVersionUID = -6178092648334273740L;

  private final String src;

  public FlowAstException(String message) {
    super(message);
    this.src = null;
  }

  public FlowAstException(String message, String src) {
    super(message);
    this.src = src;
  }

  public FlowAstException(String message, Exception e) {
    super(message, e);
    this.src = null;
  }
  
  public String getSrc() {
    return src;
  }
}
