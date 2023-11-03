package io.thestencil.site.handlers;

import java.io.IOException;
import java.util.Collections;

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
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.MigrationBuilder.LocalizedSite;
import io.thestencil.client.spi.beans.LocalizedSiteBean;
import io.thestencil.client.spi.composer.HandlerStatusCodes;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

public class SiteHandler extends HdesResourceHandler {

  public SiteHandler(
      CurrentIdentityAssociation currentIdentityAssociation,
      CurrentVertxRequest currentVertxRequest) {
    super(currentIdentityAssociation, currentVertxRequest);
  }

  @Override
  protected void handleResource(RoutingContext event, HttpServerResponse response, SiteHandlerContext ctx) {
    final var client = ctx.getClient();
    
    switch (HttpMethod.valueOf(event.request().method().name())) {
    case GET:
      String locale = event.request().getParam("locale");
      
      client.query().head()
      .onItem().transform(state -> ctx.getContent().markdown().json(state, true).build())
      .onItem().transform(markdowns -> ctx.getContent().sites()
          .imagePath("images")
          .created(System.currentTimeMillis())
          .source(markdowns)
          .build())
      .onItem().transform(sites -> sites.getSites().get(locale))
      .onItem().transform(data -> {
        try {
          if(data == null) {
            return Buffer.buffer(ctx.getObjectMapper().writeValueAsBytes(Collections.emptyMap()));
          }
          final LocalizedSite result = LocalizedSiteBean.builder().from(data).id(data.getId() + "::dev").build();
          return Buffer.buffer(ctx.getObjectMapper().writeValueAsBytes(result));
        } catch(IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .onFailure().invoke(e -> HandlerStatusCodes.catch422(e, response))
      .subscribe().with(data -> {
        response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");  
        response.end(data);
      });
      break;
    default:
      HandlerStatusCodes.catch404("no-supported", response);
      break;
    }
  }
  
  public <T> void subscribe(Uni<T> uni, HttpServerResponse response, SiteHandlerContext ctx) {
    uni.onItem().transform(data -> {
      try {
        return Buffer.buffer(ctx.getObjectMapper().writeValueAsBytes(data));
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    })
    .onFailure().invoke(e -> HandlerStatusCodes.catch422(e, response))
    .subscribe().with(data -> response.end(data)); 
  }
}
