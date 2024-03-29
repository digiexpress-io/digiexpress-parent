package io.thestencil;

/*-
 * #%L
 * demo-app
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

import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class IndexRouteFilter {
  @RouteFilter(400)                           
  void myRedirector(RoutingContext rc) {
    String uri = rc.request().uri();
    if (!uri.startsWith("/q") && !uri.startsWith("/portal-app/")) {
      rc.reroute("/portal-app/");
      return;
    }
    rc.next();
  }
}
