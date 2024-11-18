package io.thestencil.quarkus.ide.build;

/*-
 * #%L
 * quarkus-stencil-ide-deployment
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

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.maven.dependency.GACT;
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.deployment.devmode.NotFoundPageDisplayableEndpointBuildItem;
import io.quarkus.vertx.http.deployment.webjar.WebJarBuildItem;
import io.quarkus.vertx.http.deployment.webjar.WebJarResourcesFilter;
import io.quarkus.vertx.http.deployment.webjar.WebJarResultsBuildItem;
import io.smallrye.common.constraint.Assert;
import io.thestencil.quarkus.ide.FrontendBeanFactory;
import io.thestencil.quarkus.ide.FrontendRecorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class FrontendProcessor {

  FrontendConfig config;

  String getWebjarRoot() {
    return config.webjarRoot.orElseGet(() -> "META-INF/resources/webjars/" + config.artifactId + "/" + config.stencilComposerVersion);
  }

  GACT getGACT() {
    return new GACT(config.groupId, config.artifactId, "", "jar");
  }

  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(FrontendRecorder.FEATURE_BUILD_ITEM);
  }

  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  void backendBeans(
          FrontendRecorder recorder,
          BuildProducer<AdditionalBeanBuildItem> buildItems,
          BuildProducer<BeanContainerListenerBuildItem> beans) {
    buildItems.produce(AdditionalBeanBuildItem.builder().setUnremovable().addBeanClass(FrontendBeanFactory.class).build());
    beans.produce(new BeanContainerListenerBuildItem(recorder.listener()));
  }

  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  public void frontendHandler(
          Optional<HdesUIBuildItem> buildItem,
          FrontendRecorder recorder,
          HttpRootPathBuildItem httpRoot,
          BuildProducer<RouteBuildItem> routes
  ) {
    buildItem.ifPresent(bi -> {
      final var contextPath = StringUtils.prependIfMissing (bi.getContextPath(), "/");
      Handler<RoutingContext> staticAssetsHandler = recorder.staticContentHandler(bi.getStaticContentLocation() + "/" + bi.getStaticContentPath());
      Handler<RoutingContext> indexAssetsHandler = recorder.staticContentHandler(bi.getStaticContentLocation());
      Handler<RoutingContext> indexPageHandler = recorder.indexPageHandler(contextPath, indexAssetsHandler);
      routes.produce(httpRoot.routeBuilder()
              .handler(staticAssetsHandler)
              .routeFunction(contextPath, recorder.staticContentRoute(bi.getStaticContentPath()))
              .build());
      routes.produce(httpRoot.routeBuilder()
              .handler(indexPageHandler)
              .routeFunction(contextPath, recorder.indexPageRoute())
              .build());
    });
  }

  @BuildStep
  WebJarBuildItem updateIndex(HttpRootPathBuildItem httpRootPathBuildItem) {
    return WebJarBuildItem.builder()
            .artifactKey(getGACT()) // Locate known webjar
            .root(getWebjarRoot())
            .onlyCopyNonArtifactFiles(false)
            .filter((fileName, stream) -> {
              boolean changed = false;
              if ("index.html".equals(fileName)) {
                // Inject configuration into index page
                var newIndex = IndexFactory.builder()
                        .frontend(httpRootPathBuildItem.resolvePath(config.servicePath))
                        .locked(config.locked)
                        .oidc(config.oidcPath.orElse(null))
                        .status(config.statusPath.orElse(null))
                        .server(httpRootPathBuildItem.resolvePath(config.serverPath))
                        .index(stream.readAllBytes())
                        .build();
                stream = new ByteArrayInputStream(newIndex);
                changed = true;
              }
              return new WebJarResourcesFilter.FilterResult(stream, changed);
            })
            .build();
  }

  @BuildStep
  public void registerStaticEndpoints(WebJarResultsBuildItem webJarResultsBuildItem,
                                      BuildProducer<HdesUIBuildItem> buildProducer,
                                      BuildProducer<NotFoundPageDisplayableEndpointBuildItem> displayableEndpoints) {
    GACT gact = getGACT();
    var webJar = webJarResultsBuildItem.byArtifactKey(gact);
    var finalDestination = Objects.requireNonNull(webJar, () -> "webjar %s not found".formatted(gact)).getFinalDestination();
    buildProducer.produce(new HdesUIBuildItem(
            finalDestination,
            "/static",
            config.servicePath));
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(config.servicePath, "Stencil Composer"));
  }

}
