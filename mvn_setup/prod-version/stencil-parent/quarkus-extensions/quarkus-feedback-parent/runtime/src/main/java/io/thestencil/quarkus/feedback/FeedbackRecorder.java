package io.thestencil.quarkus.feedback;

/*-
 * #%L
 * quarkus-stencil-user-actions
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


import java.util.function.Consumer;
import java.util.function.Function;

import javax.enterprise.inject.spi.CDI;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.thestencil.quarkus.feedback.handlers.FeedbackHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class FeedbackRecorder {
  public static final String FEATURE_BUILD_ITEM = "feedback";
  
  public BeanContainerListener configureBuildtimeConfig(
      String servicePath, String fillPath, String allowedPath) {
    
    return beanContainer -> {
      FeedbackProducer producer = beanContainer.beanInstance(FeedbackProducer.class);
      producer
        .setServicePath(servicePath)
        .setFillPath(fillPath)
        .setAllowedPath(allowedPath);
    };
  }
  
  public void configureRuntimeConfig(RuntimeConfig runtimeConfig) {
    CDI.current().select(FeedbackProducer.class).get().setRuntimeConfig(runtimeConfig);
  }

  public Handler<RoutingContext> feedbackHandler() {

    CurrentVertxRequest currentVertxRequest = CDI.current().select(CurrentVertxRequest.class).get();
    ArcContainer container = Arc.container();
    return new FeedbackHandler(
        currentVertxRequest, 
        container.instance(ObjectMapper.class).get()
        );
  }

  public Consumer<Route> routeFunction(Handler<RoutingContext> bodyHandler) {
    return (route) -> route.handler(bodyHandler);
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
