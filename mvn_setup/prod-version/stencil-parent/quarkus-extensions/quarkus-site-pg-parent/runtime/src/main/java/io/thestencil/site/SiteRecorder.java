package io.thestencil.site;

/*-
 * #%L
 * quarkus-stencil-ide-services
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

import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.inject.spi.CDI;

import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.thestencil.site.handlers.SiteHandler;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class SiteRecorder {
  public static final String FEATURE_BUILD_ITEM = "stencil-site-pg";
  
  public BeanContainerListener configureBuildtimeConfig(
      String servicePath) {
    
    return beanContainer -> {
      SiteProducer producer = beanContainer.instance(SiteProducer.class);
      producer.setServicePath(servicePath);
    };
  }
  
  public void configureRuntimeConfig(RuntimeConfig runtimeConfig) {
    CDI.current().select(SiteProducer.class).get().setRuntimeConfig(runtimeConfig);
  }

  public Handler<RoutingContext> ideServicesHandler() {
    final var identityAssociations = CDI.current().select(CurrentIdentityAssociation.class);
    CurrentIdentityAssociation association;
    if (identityAssociations.isResolvable()) {
      association = identityAssociations.get();
    } else {
      association = null;
    }
    CurrentVertxRequest currentVertxRequest = CDI.current().select(CurrentVertxRequest.class).get();
    return new SiteHandler(association, currentVertxRequest);
  }

  public Consumer<Route> routeFunction(Handler<RoutingContext> bodyHandler) {
    return (route) -> route.handler(bodyHandler);
  }

  public Consumer<Route> idRouteFunctionGet(Handler<RoutingContext> bodyHandler) {
    return (route) -> route.method(HttpMethod.GET).handler(bodyHandler);
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
