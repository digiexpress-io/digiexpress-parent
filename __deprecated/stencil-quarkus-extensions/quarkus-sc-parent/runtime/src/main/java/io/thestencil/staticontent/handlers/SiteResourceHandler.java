package io.thestencil.staticontent.handlers;

/*-
 * #%L
 * quarkus-stencil-sc
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

import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.thestencil.staticontent.StaticContentContext;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class SiteResourceHandler extends HdesResourceHandler {

  public SiteResourceHandler(
      CurrentIdentityAssociation currentIdentityAssociation,
      CurrentVertxRequest currentVertxRequest) {
    super(currentIdentityAssociation, currentVertxRequest);
  }

  @Override
  protected void handleResource(RoutingContext event, HttpServerResponse response, StaticContentContext ctx) {
    
    
    switch (HttpMethod.valueOf(event.request().method().name())) {
    case GET:
      String locale = event.request().getParam("locale");
      String defs = ctx.getContentValue(locale);
      response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
      response.end(defs);
      break;
    default:
      catch404("no-supported", ctx, response);
      break;
    }
  }
}
