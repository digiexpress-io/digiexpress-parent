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
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;

@Recorder
public class FrontendRecorder {
  public static final String FEATURE_BUILD_ITEM = "stencil-composer";

  public BeanContainerListener listener() {
    return beanContainer -> {
      FrontendBeanFactory producer = beanContainer.beanInstance(FrontendBeanFactory.class);
    };
  }

  public Handler<RoutingContext> staticContentHandler(String location) {
    return StaticHandler.create(FileSystemAccess.ROOT, location)
            .setIndexPage("index.html")
            .setDefaultContentEncoding("UTF-8");
  }
}
