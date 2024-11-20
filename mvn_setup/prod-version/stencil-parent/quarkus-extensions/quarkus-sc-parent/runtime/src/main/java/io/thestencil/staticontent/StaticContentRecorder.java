package io.thestencil.staticontent;

/*-
 * #%L
 * quarkus-stencil-sc
 * %%
 * Copyright (C) 2015 - 2024 Copyright 2022 ReSys OÜ
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
import io.quarkus.security.identity.CurrentIdentityAssociation;
import io.quarkus.vertx.http.runtime.CurrentVertxRequest;
import io.thestencil.staticontent.handlers.SiteResourceHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.inject.spi.CDI;

import java.util.Map;

@Recorder
public class StaticContentRecorder {

  public BeanContainerListener injectContentToInMemoryContentProviderBeanFactory(
    Map<String, String> serializedContent,
    String defaultLocale) {
    return beanContainer ->
      beanContainer.beanInstance(InMemoryContentProviderBeanFactory.class)
        .setDefaultLocale(defaultLocale)
        .setSerializedContent(serializedContent);
  }

  public Handler<RoutingContext> staticContentHandler() {
    final var identityAssociations = CDI.current().select(CurrentIdentityAssociation.class);
    CurrentIdentityAssociation association = null;
    if (identityAssociations.isResolvable()) {
      association = identityAssociations.get();
    }
    CurrentVertxRequest currentVertxRequest = CDI.current().select(CurrentVertxRequest.class).get();
    return new SiteResourceHandler(association, currentVertxRequest);
  }

}
