package io.resys.hdes.quarkus.composer.pg;

/*-
 * #%L
 * quarkus-composer-pg
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

import io.vertx.core.Handler;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;

public class FrontendRouter implements Handler<RoutingContext> {

  private String uiFinalDestination;
  private String uiPath;
  private String hash;

  public FrontendRouter() {}

  public FrontendRouter(String uiFinalDestination, String uiPath, String hash) {
    this.uiFinalDestination = uiFinalDestination;
    this.uiPath = uiPath;
    this.hash = hash;
  }
  public String getUiFinalDestination() {
    return uiFinalDestination;
  }
  public void setUiFinalDestination(String uiFinalDestination) {
    this.uiFinalDestination = uiFinalDestination;
  }
  public String getUiPath() {
    return uiPath;
  }
  public void setUiPath(String uiPath) {
    this.uiPath = uiPath;
  }

  @Override
  public void handle(RoutingContext event) {
    StaticHandler staticHandler = StaticHandler.create()
      .setAllowRootFileSystemAccess(true)
      .setWebRoot(uiFinalDestination)
      .setDefaultContentEncoding("UTF-8");

    event.response().headers().set(HttpHeaders.ETAG, hash);
    
    if (event.normalizedPath().length() == uiPath.length()) {
      event.response().setStatusCode(302);
      event.response().headers().set(HttpHeaders.LOCATION, uiPath + "/");
      event.response().end();
      return;
    } else if (event.normalizedPath().length() == uiPath.length() + 1) {
      event.reroute(uiPath + "/index.html");
      return;
    }
    staticHandler.handle(event);
  }
}
