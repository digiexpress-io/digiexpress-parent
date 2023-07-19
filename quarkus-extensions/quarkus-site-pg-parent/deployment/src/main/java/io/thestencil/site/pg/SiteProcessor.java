package io.thestencil.site.pg;

/*-
 * #%L
 * quarkus-stencil-site-pg-deployment
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

import java.util.ArrayList;
import java.util.List;

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
import io.thestencil.site.RuntimeConfig;
import io.thestencil.site.SiteProducer;
import io.thestencil.site.SiteRecorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;



public class SiteProcessor {
  SiteConfig config;
  
  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(SiteRecorder.FEATURE_BUILD_ITEM);
  }
  
  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  void buildtimeInit(
      SiteBuildItem buildItem,
      SiteRecorder recorder,
      BuildProducer<AdditionalBeanBuildItem> buildItems,
      BuildProducer<BeanContainerListenerBuildItem> beans) {
    
    buildItems.produce(AdditionalBeanBuildItem.builder().setUnremovable().addBeanClass(SiteProducer.class).build());
    beans.produce(new BeanContainerListenerBuildItem(recorder.configureBuildtimeConfig(buildItem.getServicePath())));
  }

  
  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  void runtimeInit(
      RuntimeConfig config,
      SiteBuildItem buildItem,
      SiteRecorder recorder,
      
      BeanContainerBuildItem beanContainer, 
      ShutdownContextBuildItem shutdown) {
    
    recorder.configureRuntimeConfig(config);
  }
  
  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  public void staticContentHandler(
    SiteBuildItem buildItem,
    SiteRecorder recorder,
    HttpRootPathBuildItem httpRoot,
    BuildProducer<RouteBuildItem> routes,
    BodyHandlerBuildItem body,
    SiteConfig config) throws Exception {
    
    final var bodyHandler = body.getHandler();
    final Handler<RoutingContext> handler = recorder.ideServicesHandler();

    routes.produce(httpRoot.routeBuilder()
        .routeFunction(buildItem.getServicePath(), recorder.idRouteFunctionGet(bodyHandler))
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
      SiteRecorder recorder,
      BuildProducer<SiteBuildItem> buildProducer,
      HttpRootPathBuildItem httpRootPathBuildItem,
      BuildProducer<NotFoundPageDisplayableEndpointBuildItem> displayableEndpoints) throws Exception {
    
    final var servicePath = cleanPath(config.servicePath);
    final var buildItem = SiteBuildItem.builder(servicePath).build();
    
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(servicePath), "Stencil Postgre Site"));
    buildProducer.produce(buildItem);
  }
  
  private static String cleanPath(String value) {
    return SiteProducer.cleanPath(value);
  }
}
