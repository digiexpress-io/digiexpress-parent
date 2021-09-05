package io.thestencil.client.web;

/*-
 * #%L
 * stencil-client
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

import io.thestencil.client.api.StencilClient;

public class HandlerContext {
  private final StencilClient client;
  private final ServicesPathConfig paths;
  
  public HandlerContext(StencilClient client, ServicesPathConfig paths) {
    super();
    this.client = client;
    this.paths = paths;
  }

  public StencilClient getClient() {
    return client;
  }

  public ServicesPathConfig getPaths() {
    return paths;
  }
}
