package io.thestencil.quarkus.ide.handlers;

/*-
 * #%L
 * quarkus-stencil-ide
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

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class NoEndpointHandler implements Handler<RoutingContext> {
  private static final String CONTENT_TYPE = "text/plain; charset=UTF-8";
  private static final String MESSAGE = "";

  @Override
  public void handle(RoutingContext event) {
    HttpServerResponse response = event.response();
    response.headers().set(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
    response.setStatusCode(404).end(MESSAGE);
  }
}
