package io.thestencil.site.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.thestencil.client.api.StencilClient;

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

import io.thestencil.client.api.StencilComposer;

public class SiteHandlerContext {
  private final StencilComposer client;
  private final StencilClient content;
  private final String servicePath;
  private final ObjectMapper objectMapper;
  
  public SiteHandlerContext(StencilComposer client, StencilClient content, ObjectMapper objectMapper, String servicePath) {
    super();
    this.client = client;
    this.content = content;
    this.servicePath = servicePath;
    this.objectMapper = objectMapper;
  }
  
  public StencilClient getContent() {
    return content;
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public StencilComposer getClient() {
    return client;
  }

  public String getServicePath() {
    return servicePath;
  }
}
