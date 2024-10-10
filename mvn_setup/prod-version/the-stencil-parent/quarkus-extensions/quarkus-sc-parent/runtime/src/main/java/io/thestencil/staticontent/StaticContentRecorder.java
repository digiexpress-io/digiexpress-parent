package io.thestencil.staticontent;

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

import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;

import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.thestencil.client.api.MigrationBuilder.Sites;
import io.thestencil.staticontent.handlers.SiteResourceHandler;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class StaticContentRecorder {

  public BeanContainerListener listener(
      Sites staticContent,
      String defaultLocale) {
    
    final var contentValues = staticContent.getSites().entrySet().stream()
        .collect(Collectors.toMap(e -> e.getKey(), e -> Json.encode(e.getValue())));
    
    return beanContainer -> {
      StaticContentBeanFactory producer = beanContainer.instance(StaticContentBeanFactory.class);
      producer
        .setDefaultLocale(defaultLocale)
        .setStaticContent(staticContent)
        .setSerializedContent(contentValues);
    };
  }

  
  public Handler<RoutingContext> staticContentHandler() {
    final var identityAssociations = CDI.current().select(CurrentIdentityAssociation.class);
    CurrentIdentityAssociation association;
    if (identityAssociations.isResolvable()) {
      association = identityAssociations.get();
    } else {
      association = null;
    }
    CurrentVertxRequest currentVertxRequest = CDI.current().select(CurrentVertxRequest.class).get();
    return new SiteResourceHandler(association, currentVertxRequest);
  }

  public Function<Router, Route> routeFunction(String rootPath, Handler<RoutingContext> bodyHandler) {
    return new Function<Router, Route>() {
      @Override
      public Route apply(Router router) {
        return router.route(rootPath).handler(bodyHandler);
      }
    };
  }
}
