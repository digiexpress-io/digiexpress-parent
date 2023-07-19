package io.thestencil.quarkus.ide.services;

import java.util.ArrayList;
import java.util.List;

/*-
 * #%L
 * quarkus-stencil-ide-services-deployment
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

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.vertx.http.deployment.BodyHandlerBuildItem;
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.deployment.devmode.NotFoundPageDisplayableEndpointBuildItem;
import io.thestencil.client.api.Serializers;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;



public class IDEServicesProcessor {
  IDEServicesConfig config;
  
  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(IDEServicesRecorder.FEATURE_BUILD_ITEM);
  }
  
  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  void buildtimeInit(
      IDEServicesBuildItem buildItem,
      IDEServicesRecorder recorder,
      BuildProducer<AdditionalBeanBuildItem> buildItems,
      BuildProducer<BeanContainerListenerBuildItem> beans) {
    
    buildItems.produce(AdditionalBeanBuildItem.builder().setUnremovable().addBeanClass(IDEServicesProducer.class).build());
    beans.produce(new BeanContainerListenerBuildItem(recorder.configureBuildtimeConfig(
        buildItem.getServicePath(), 
        buildItem.getMigrationPath(),
        buildItem.getArticlesPath(), 
        buildItem.getPagesPath(), 
        buildItem.getWorkflowsPath(), 
        buildItem.getLinksPath(), 
        buildItem.getReleasesPath(), 
        buildItem.getLocalePath(),
        buildItem.getTemplatesPath(),
        buildItem.getVersionPath())));
  }

  
  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  void runtimeInit(
      RuntimeConfig config,
      IDEServicesBuildItem buildItem,
      IDEServicesRecorder recorder,
      
      BeanContainerBuildItem beanContainer, 
      ShutdownContextBuildItem shutdown) {
    
    recorder.configureRuntimeConfig(config);
  }
  
  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  public void staticContentHandler(
    IDEServicesBuildItem buildItem,
    IDEServicesRecorder recorder,
    HttpRootPathBuildItem httpRoot,
    BuildProducer<RouteBuildItem> routes,
    BodyHandlerBuildItem body,
    IDEServicesConfig config) throws Exception {
    
    final var bodyHandler = body.getHandler();
    final Handler<RoutingContext> handler = recorder.ideServicesHandler();
    
    
    final Consumer<String> addRoute = (path) -> {
      routes.produce(httpRoot.routeBuilder()
          .routeFunction(path, recorder.routeFunction(bodyHandler))
          .handler(handler)
          .displayOnNotFoundPage()
          .build());
      routes.produce(httpRoot.routeBuilder()
          .routeFunction(path + "/", recorder.routeFunction(bodyHandler))
          .handler(handler)
          .displayOnNotFoundPage()
          .build());
      routes.produce(httpRoot.routeBuilder()
          .routeFunction(path + "/:id", recorder.idRouteFunctionDelete(bodyHandler))
          .handler(handler)
          .displayOnNotFoundPage()
          .build());
    };
    
    addRoute.accept(buildItem.getServicePath());
    addRoute.accept(buildItem.getMigrationPath());
    addRoute.accept(buildItem.getArticlesPath());
    addRoute.accept(buildItem.getPagesPath());
    addRoute.accept(buildItem.getLinksPath());
    addRoute.accept(buildItem.getLocalePath());
    addRoute.accept(buildItem.getReleasesPath());
    addRoute.accept(buildItem.getTemplatesPath());
    addRoute.accept(buildItem.getWorkflowsPath());
    addRoute.accept(buildItem.getVersionPath());

    routes.produce(httpRoot.routeBuilder()
        .routeFunction(buildItem.getReleasesPath() + "/:id", recorder.idRouteFunctionGet(bodyHandler))
        .handler(handler)
        .displayOnNotFoundPage()
        .build());
    
    routes.produce(httpRoot.routeBuilder()
        .routeFunction(buildItem.getLinksPath() + "/:id?:articleId", recorder.idRouteFunctionDelete(bodyHandler))
        .handler(handler)
        .displayOnNotFoundPage()
        .build());
    routes.produce(httpRoot.routeBuilder()
        .routeFunction(buildItem.getWorkflowsPath() + "/:id?:articleId", recorder.idRouteFunctionDelete(bodyHandler))
        .handler(handler)
        .displayOnNotFoundPage()
        .build());
  }
  
  @BuildStep
  public ReflectiveClassBuildItem reflection() throws SecurityException, ClassNotFoundException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    List<String> names = new ArrayList<>();
    
    for(Class<?> clazz : Serializers.VALUES) {
      Class<?>[] declaredClasses = classLoader.loadClass(clazz.getName()).getDeclaredClasses();
      
      names.add(clazz.getName());
      for (Class<?> decl : declaredClasses) {
        names.add(decl.getName());
      }
    }
    
    return new ReflectiveClassBuildItem(true, true, names.toArray(new String[] {}));
  }
  
  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  public void frontendBeans(
      IDEServicesRecorder recorder,
      BuildProducer<IDEServicesBuildItem> buildProducer,
      HttpRootPathBuildItem httpRootPathBuildItem,
      BuildProducer<NotFoundPageDisplayableEndpointBuildItem> displayableEndpoints) throws Exception {
    
    final var servicePath = cleanPath(config.servicePath);
    final var buildItem = IDEServicesBuildItem.builder(servicePath)
        .articlesPath("articles")
        .pagesPath("pages")
        .localePath("locales")
        .workflowsPath("workflows")
        .linksPath("links")
        .releasesPath("releases")
        .migrationPath("migrations")
        .templatesPath("templates")
        .versionPath("version")
        .build();
    
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(servicePath), "Stencil Actions"));
    buildProducer.produce(buildItem);
  }
  
  private static String cleanPath(String value) {
    return IDEServicesProducer.cleanPath(value);
  }
}
