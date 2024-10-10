package io.thestencil.iam.spi.integrations;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.jwt.JsonWebToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.smallrye.mutiny.Uni;
import io.thestencil.iam.api.ImmutableAuthorizationAction;
import io.thestencil.iam.api.UserActionsClient.AuthorizationAction;
import io.thestencil.iam.api.UserActionsClient.AuthorizationActionQuery;
import io.thestencil.iam.api.UserActionsClient.UserActionsClientConfig;
import io.thestencil.iam.spi.support.BuilderTemplate;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.ext.web.client.HttpResponse;



public class AuthorizationActionQueryDefault extends BuilderTemplate implements AuthorizationActionQuery {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthorizationActionQueryDefault.class);
  private final List<String> userRoles = new ArrayList<>();
  
  public AuthorizationActionQueryDefault(
      RequestOptions init, UserActionsClientConfig config, JsonWebToken idToken) {
    super(config.getWebClient(), init, idToken);

  }
  @Override
  public AuthorizationActionQuery userRoles(List<String> userRoles) {
    this.userRoles.addAll(userRoles);
    return this;
  }

  @Override
  public Uni<AuthorizationAction> get() {
    
    return post(getUri("/processesAuthorizations/"))
        .putHeader(HttpHeaderNames.CONTENT_TYPE.toString(), "application/json")
        .sendBuffer(io.vertx.mutiny.core.buffer.Buffer.newInstance(JsonObject.mapFrom(Map.of("userRoles", userRoles)).toBuffer()))
        .onItem().transform(resp -> map(resp));
  }
  
  @SuppressWarnings("unchecked")
  private AuthorizationAction map(HttpResponse<?> resp) {
    if (resp.statusCode() != 200) {
      String error = "USER AUTHORIZATION ACTIONS: Can't create response, e = " + resp.statusCode() + " | " + resp.statusMessage() + " | " + resp.headers();
      LOGGER.error(error);
      return ImmutableAuthorizationAction.builder()
          .addAllUserRoles(userRoles)
          .build();
    }
    final var body = resp.bodyAsJsonObject();
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("USER AUTHORIZATION ACTIONS query succeeded: {}!", body);
    }
    ;
    return ImmutableAuthorizationAction.builder()
        .allowedProcessNames(body.getJsonArray("allowedProcessNames").getList())
        .userRoles(body.getJsonArray("userRoles").getList())
        .build();
  }
}
