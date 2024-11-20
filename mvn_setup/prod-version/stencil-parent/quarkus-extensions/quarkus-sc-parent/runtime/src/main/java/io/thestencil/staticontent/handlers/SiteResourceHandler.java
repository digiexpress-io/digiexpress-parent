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
import io.thestencil.staticontent.ContentProvider;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class SiteResourceHandler extends HdesResourceHandler {

  public SiteResourceHandler(
      CurrentIdentityAssociation currentIdentityAssociation,
      CurrentVertxRequest currentVertxRequest) {
    super(currentIdentityAssociation, currentVertxRequest);
  }

  @Override
  protected void handleResource(RoutingContext event, HttpServerResponse response, ContentProvider contentProvider) {
    if (HttpMethod.GET.equals(event.request().method())) {
      String locale = event.request().getParam("locale");
      response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json");
      String content = contentProvider.getContentValue(locale);
      if (content == null) {
        response.setStatusCode(404).end();
        return;
      }
      response.end(content);
    } else {
      // 405 Method not allowed
      response.setStatusCode(405).end();
    }
  }
}
