package io.resys.wrench.assets.flow.spi;

/*-
 * #%L
 * hdes-flow
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

import io.resys.hdes.client.api.programs.FlowResult;
import io.resys.hdes.client.api.programs.FlowProgram.Step;

public class FlowException extends RuntimeException {

  private static final long serialVersionUID = 6659681954052672940L;

  private final Step node;
  private final FlowResult flow;
  
  public FlowException(String message, FlowResult flow, Step node, Throwable cause) {
    super(message, cause);
    this.node = node;
    this.flow = flow;
  }
  public FlowException(String message, FlowResult flow, Step node) {
    super(message);
    this.node = node;
    this.flow = flow;
  }
  public FlowException(FlowResult flow, String message) {
    super(message);
    this.node = null;
    this.flow = flow;
  }

  public FlowException(FlowResult flow, String message, Throwable cause) {
    super(message, cause);
    this.node = null;
    this.flow = flow;
  }

  public Step getNode() {
    return node;
  }
  public FlowResult getFlow() {
    return flow;
  }
}
