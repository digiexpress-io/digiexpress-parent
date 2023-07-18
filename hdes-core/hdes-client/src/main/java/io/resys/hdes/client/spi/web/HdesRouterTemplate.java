package io.resys.hdes.client.spi.web;

/*-
 * #%L
 * hdes-client
 * %%
 * Copyright (C) 2020 - 2022 Copyright 2020 ReSys OÜ
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

import javax.enterprise.inject.spi.CDI;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ManagedContext;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.quarkus.vertx.http.runtime.security.QuarkusHttpUser;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;


public abstract class HdesRouterTemplate implements Handler<RoutingContext> {
  private final CurrentIdentityAssociation currentIdentityAssociation;
  private final CurrentVertxRequest currentVertxRequest;
  
  public HdesRouterTemplate(
      CurrentIdentityAssociation currentIdentityAssociation,
      CurrentVertxRequest currentVertxRequest) {
    super();
    this.currentIdentityAssociation = currentIdentityAssociation;
    this.currentVertxRequest = currentVertxRequest;
  }
  
  protected abstract void doRoute(RoutingContext event, HttpServerResponse response, HdesWebContext ctx, ObjectMapper objectMapper);
  
  protected void handleSecurity(RoutingContext event) {
    if (currentIdentityAssociation != null) {
      QuarkusHttpUser existing = (QuarkusHttpUser) event.user();
      if (existing != null) {
        SecurityIdentity identity = existing.getSecurityIdentity();
        currentIdentityAssociation.setIdentity(identity);
      } else {
        currentIdentityAssociation.setIdentity(QuarkusHttpUser.getSecurityIdentity(event, null));
      }
    }
    currentVertxRequest.setCurrent(event);
  }
  
  @Override
  public void handle(RoutingContext event) {
    ManagedContext requestContext = Arc.container().requestContext();
    if (requestContext.isActive()) {
      handleSecurity(event);      
      HttpServerResponse response = event.response();
      HdesWebContext ctx = CDI.current().select(HdesWebContext.class).get();
      ObjectMapper objectMapper = CDI.current().select(ObjectMapper.class).get();
      try {
        doRoute(event, response, ctx, objectMapper);
      } catch (Exception e) {
        HdesStatusCodes.catch422(e, response);
      }
     return; 
    }
    
    HttpServerResponse response = event.response();
    HdesWebContext ctx = CDI.current().select(HdesWebContext.class).get();
    ObjectMapper objectMapper = CDI.current().select(ObjectMapper.class).get();
    try {
      requestContext.activate();
      handleSecurity(event);
      doRoute(event, response, ctx, objectMapper);
    } catch (Exception e) {
      HdesStatusCodes.catch422(e, response);
    } finally {
      requestContext.terminate();
    }
  }
}
