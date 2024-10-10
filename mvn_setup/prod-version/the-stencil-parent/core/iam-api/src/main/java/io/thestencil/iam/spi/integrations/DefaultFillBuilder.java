package io.thestencil.iam.spi.integrations;

import org.eclipse.microprofile.jwt.JsonWebToken;

/*-
 * #%L
 * iam-api
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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

import io.netty.handler.codec.http.HttpHeaderNames;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.UserActionsClient.FillBuilder;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.thestencil.iam.spi.support.PortalAssert;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.RequestOptions;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;

public class DefaultFillBuilder extends BuilderTemplate implements FillBuilder {

  private final UserActionsClientConfig config;
  private HttpMethod method;
  private Buffer body;
  private String path;
    
  public DefaultFillBuilder(RequestOptions init, UserActionsClientConfig config, JsonWebToken idToken) {
    super(config.getWebClient(), init, idToken);
    this.config = config;
  }
  
  @Override
  public FillBuilder method(HttpMethod method) {
    this.method = method;
    return this;
  }
  @Override
  public FillBuilder body(Buffer body) {
    this.body = body;
    return this;
  }
  @Override
  public FillBuilder path(String path) {
    this.path = path;
    return this;
  }
  @Override
  public Uni<Buffer> build() {
    PortalAssert.notNull(method, () -> "method must be defined!");
    PortalAssert.notNull(path, () -> "path must be defined!");
    
    final var request = request(method, getUri(path()))
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json");
    final Uni<HttpResponse<Buffer>> response;
    if(body == null) {
      response = request.send();
    } else {
      response = request.sendBuffer(body);
    }
    return response.onItem().transform(resp -> resp.body());
  }

  private String path() {
    return path.substring(config.getFillPath().length());
  }
}
