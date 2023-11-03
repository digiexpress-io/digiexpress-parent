package io.thestencil.quarkus.iam.handler;

import com.fasterxml.jackson.databind.ObjectMapper;

/*-
 * #%L
 * quarkus-stencil-iam
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



import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.thestencil.iam.api.IAMClient;
import io.thestencil.iam.api.IAMClient.UserRolesResult;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.EncodeException;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class IAMHandler extends IAMTemplate {

  private final ObjectMapper objectMapper;
  private final String liveness;
  private final String roles;
  
  public IAMHandler(
      ObjectMapper objectMapper,
      CurrentIdentityAssociation currentIdentityAssociation,
      CurrentVertxRequest currentVertxRequest, String liveness, String roles) {
    super(currentIdentityAssociation, currentVertxRequest);
    this.liveness = liveness;
    this.roles = roles;
    this.objectMapper = objectMapper;
  }
  
  @Override
  protected void handleResource(RoutingContext event, HttpServerResponse response, IAMClient ctx) {
    
    if(event.request().method() != HttpMethod.GET) {
      return;
    }
    final var path = event.normalizedPath();
    if(path.startsWith(liveness)) {
      ctx.livenessQuery().get()
      .onItem().transform(data -> toBuffer(data))
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));
    } else if(path.startsWith(roles)) {
      
      final var id = event.request().getHeader("cookie");
      
      ctx.userQuery().get().onItem()
        .transformToUni(user -> {
          if(user.getUser().getRepresentedCompany() != null) {
            return ctx.companyRolesQuery().id(id).get();
          } else if(user.getUser().getRepresentedPerson() != null) {
            return ctx.personRolesQuery().id(id).get();
          }
          throw new RuntimeException("Represented person/company could not be resolved!");
        })
        .onItem().transform((UserRolesResult data) -> toBuffer(data))
        .onFailure().invoke(e -> catch422(e, ctx, response))
        .subscribe().with(data -> response.end(data));
    
    } else {
      ctx.userQuery().get()
      .onItem().transform(data -> toBuffer(data))
      .onFailure().invoke(e -> catch422(e, ctx, response))
      .subscribe().with(data -> response.end(data));
    }
  
  }
  
  private io.vertx.core.buffer.Buffer toBuffer(Object object) {

    try {
      log.debug("Responding object: {} body: {}, bodyAsString: {}", object.getClass(), object, objectMapper.writeValueAsString(object));
      return io.vertx.core.buffer.Buffer.buffer(objectMapper.writeValueAsBytes(object));
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }
}
