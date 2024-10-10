package io.thestencil.quarkus.useractions.deployment;

/*-
 * #%L
 * quarkus-stencil-user-actions-deployment
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

import java.util.ArrayList;
import java.util.Arrays;
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
import io.thestencil.iam.api.ImmutableAttachment;
import io.thestencil.iam.api.ImmutableAttachmentData;
import io.thestencil.iam.api.ImmutableAttachmentDownloadUrl;
import io.thestencil.iam.api.ImmutableAuthorizationAction;
import io.thestencil.iam.api.ImmutableUserAction;
import io.thestencil.iam.api.ImmutableUserMessage;
import io.thestencil.iam.api.ImmutableUserTask;
import io.thestencil.iam.spi.integrations.ImmutableProcessesInit;
import io.thestencil.iam.spi.integrations.ImmutableUserActionReplyInit;
import io.thestencil.quarkus.useractions.RuntimeConfig;
import io.thestencil.quarkus.useractions.UserActionsProducer;
import io.thestencil.quarkus.useractions.UserActionsRecorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;



public class UserActionsProcessor {
  UserActionsConfig config;
  
  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(UserActionsRecorder.FEATURE_BUILD_ITEM);
  }
  
  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  void buildtimeInit(
      UserActionsBuildItem buildItem,
      UserActionsRecorder recorder,
      BuildProducer<AdditionalBeanBuildItem> buildItems,
      BuildProducer<BeanContainerListenerBuildItem> beans) {
    
    buildItems.produce(AdditionalBeanBuildItem.builder().setUnremovable().addBeanClass(UserActionsProducer.class).build());
    beans.produce(new BeanContainerListenerBuildItem(recorder.configureBuildtimeConfig(
        "/" + buildItem.getServicePath(),
        "/" + buildItem.getFillPath(),
        "/" + buildItem.getReviewPath(),
        "/" + buildItem.getMessagesPath(),
        "/" + buildItem.getAttachmentsPath(),
        "/" + buildItem.getAuthorizationsPath()
        )));
  }

  
  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  void runtimeInit(
      RuntimeConfig config,
      UserActionsBuildItem buildItem,
      UserActionsRecorder recorder,
      
      BeanContainerBuildItem beanContainer, 
      ShutdownContextBuildItem shutdown) {
    
    recorder.configureRuntimeConfig(config);
  }
  
  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  public void staticContentHandler(
    UserActionsBuildItem buildItem,
    UserActionsRecorder recorder,
    HttpRootPathBuildItem httpRoot,
    BuildProducer<RouteBuildItem> routes,
    BodyHandlerBuildItem body,
    UserActionsConfig config) throws Exception {
    
    final var bodyHandler = body.getHandler();
    final Handler<RoutingContext> handler = recorder.userActionsHandler();
    
    routes.produce(httpRoot.routeBuilder()
        .routeFunction(buildItem.getServicePath(), recorder.routeFunction(bodyHandler))
        .handler(handler)
        .build());
    routes.produce(httpRoot.routeBuilder()
        .routeFunction(buildItem.getServicePath() + "/*", recorder.routeFunction(bodyHandler))
        .handler(handler)
        .build());
    
    /* @deprecated way of doing
    routes.produce(new RouteBuildItem.Builder()
        .routeFunction(recorder.routeFunction("/"+ buildItem.getServicePath(), body.getHandler()))
        .handler(handler)
        .blockingRoute()
        .build());
    routes.produce(new RouteBuildItem.Builder()
        .routeFunction(recorder.routeFunction("/"+ buildItem.getServicePath() + "/*", body.getHandler()))
        .handler(handler)
        .blockingRoute()
        .build());*/
  }
  
  @BuildStep
  public ReflectiveClassBuildItem reflection() throws SecurityException, ClassNotFoundException {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    List<String> names = new ArrayList<>();
    
    for(Class<?> clazz : Arrays.asList(
        ImmutableAuthorizationAction.class,
        ImmutableAttachmentDownloadUrl.class,
        ImmutableUserTask.class,
        ImmutableUserAction.class,
        ImmutableAttachment.class,
        ImmutableUserMessage.class,
        ImmutableProcessesInit.class,
        ImmutableAttachmentData.class,
        ImmutableUserActionReplyInit.class)) {

      Class<?>[] declaredClasses = classLoader.loadClass(clazz.getName())
          .getDeclaredClasses();
      
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
      UserActionsRecorder recorder,
      BuildProducer<UserActionsBuildItem> buildProducer,
      HttpRootPathBuildItem httpRootPathBuildItem,
      BuildProducer<NotFoundPageDisplayableEndpointBuildItem> displayableEndpoints) throws Exception {
    
    final var servicePath = cleanPath(config.servicePath);
    final var buildItem = new UserActionsBuildItem(
        servicePath, 
        servicePath + "/fill", 
        servicePath + "/review",
        servicePath + "/messages",
        servicePath + "/attachments",
        servicePath + "/authorizations");
    
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(servicePath), "User Actions"));
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(buildItem.getAttachmentsPath()), "User Actions Attachments"));
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(buildItem.getFillPath()), "User Actions Fill Form"));
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(buildItem.getReviewPath()), "User Actions Review Form"));
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(buildItem.getMessagesPath()), "User Actions Messages"));
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(buildItem.getAuthorizationsPath()), "User Actions Authorizations"));
    buildProducer.produce(buildItem);
  }
  
  private static String cleanPath(String value) {
    return UserActionsProducer.cleanPath(value);
  }
}
