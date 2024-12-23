package io.thestencil.quarkus.ide;

/*-
 * #%L
 * quarkus-stencil-ide
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

import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

@Recorder
@Slf4j
public class FrontendRecorder {
  public static final String FEATURE_BUILD_ITEM = "stencil-composer";

  public BeanContainerListener listener() {
    return beanContainer -> beanContainer.beanInstance(FrontendBeanFactory.class);
  }

  public Handler<RoutingContext> indexPageHandler(String contextPath, Handler<RoutingContext> staticContentHandler) {
    final var indexPath = contextPath + "/index.html";
    return (event) -> {
      String requestPath = event.request().path();
      if (shouldRerouteToIndex(indexPath, requestPath)) {
        log.trace("Reroute request {} to {}", requestPath, indexPath);
        event.reroute(indexPath);
        return;
      }
      staticContentHandler.handle(event);
    };
  }

  private static boolean shouldRerouteToIndex(String indexPath, String requestPath) {
    if (indexPath.equals(requestPath)) {
      // Explicit request to index.html
      return false;
    }
    var last = StringUtils.substringAfterLast(requestPath, "/");

    if (StringUtils.isNotBlank(last) && last.contains(".")) {
      return false;
    }
    return true;
  }


  public Handler<RoutingContext> staticContentHandler(String location) {
    return StaticHandler.create(FileSystemAccess.ROOT, location)
            .setDefaultContentEncoding("UTF-8");
  }

  public Consumer<Route> staticContentRoute(String staticPath) {
    return (route) ->
            route.method(HttpMethod.GET)
                    .path(route.getPath() + StringUtils.appendIfMissing(StringUtils.prependIfMissing(staticPath, "/"), "/*"));
  }

  public Consumer<Route> indexPageRoute() {
    return (route) ->
            route.method(HttpMethod.GET)
                    .produces("text/html")
                    .path(route.getPath() + "/*");
  }

}
