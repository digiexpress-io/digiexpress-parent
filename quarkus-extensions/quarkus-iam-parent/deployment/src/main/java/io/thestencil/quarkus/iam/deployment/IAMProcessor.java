package io.thestencil.quarkus.iam.deployment;

/*-
 * #%L
 * quarkus-stencil-iam-deployment
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
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.deployment.devmode.NotFoundPageDisplayableEndpointBuildItem;
import io.thestencil.iam.api.ImmutableAddress;
import io.thestencil.iam.api.ImmutableContact;
import io.thestencil.iam.api.ImmutableRepresentedCompany;
import io.thestencil.iam.api.ImmutableRepresentedPerson;
import io.thestencil.iam.api.ImmutableUser;
import io.thestencil.iam.api.ImmutableUserLiveness;
import io.thestencil.iam.api.ImmutableUserQueryResult;
import io.thestencil.iam.api.ImmutableUserRoles;
import io.thestencil.iam.api.ImmutableUserRolesPrincipal;
import io.thestencil.iam.api.ImmutableUserRolesResult;
import io.thestencil.quarkus.iam.IAMBeanFactory;
import io.thestencil.quarkus.iam.IAMRecorder;
import io.thestencil.quarkus.iam.RuntimeConfig;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class IAMProcessor {
  
  IAMConfig config;
  
  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(IAMRecorder.FEATURE_BUILD_ITEM);
  }

  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  void buildtimeInit(
      IAMBuildItem buildItem,
      IAMRecorder recorder,

      BuildProducer<AdditionalBeanBuildItem> buildItems,
      BuildProducer<BeanContainerListenerBuildItem> beans) {

    buildItems.produce(AdditionalBeanBuildItem.builder().setUnremovable().addBeanClass(IAMBeanFactory.class).build());
    beans.produce(new BeanContainerListenerBuildItem(recorder.buildtimeConfig(buildItem.getServicePath())));
  }
  
  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  void runtimeInit(
      RuntimeConfig config,
      IAMBuildItem buildItem,
      IAMRecorder recorder,

      BeanContainerBuildItem beanContainer, 
      ShutdownContextBuildItem shutdown) {
    
    recorder.runtimeConfig(config);
  }

  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  public void staticContentHandler(
    IAMBuildItem buildItem,
    IAMRecorder recorder,
    HttpRootPathBuildItem httpRoot,
    BuildProducer<RouteBuildItem> routes,
    IAMConfig config) throws Exception {
    
    Handler<RoutingContext> handler = recorder.iamHandler(buildItem.getLivenessPath(), buildItem.getRolesPath());

    routes.produce(httpRoot.routeBuilder()
        .route(config.servicePath)
        .handler(handler)
        .build());
    routes.produce(httpRoot.routeBuilder()
        .route(config.servicePath + "/*")
        .handler(handler)
        .build());
  }
  
  @BuildStep
  public ReflectiveClassBuildItem reflection() {
    return new ReflectiveClassBuildItem(true, true,
        ImmutableRepresentedPerson.class,
        ImmutableRepresentedCompany.class,
        ImmutableUserRoles.class,
        ImmutableUserRolesResult.class,
        ImmutableUserRolesPrincipal.class,
        ImmutableUser.class,
        ImmutableAddress.class,
        ImmutableContact.class,
        ImmutableUserLiveness.class,
        ImmutableUserQueryResult.class);
  }
  
  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  public void frontendBeans(
      IAMRecorder recorder,
      BuildProducer<IAMBuildItem> buildProducer,
      HttpRootPathBuildItem httpRootPathBuildItem,
      BuildProducer<NotFoundPageDisplayableEndpointBuildItem> displayableEndpoints) throws Exception {

    final var userPath = httpRootPathBuildItem.resolvePath(config.servicePath);
    final var livenessPath = httpRootPathBuildItem.resolvePath(config.servicePath + "/liveness");
    final var rolesPath = httpRootPathBuildItem.resolvePath(config.servicePath + "/roles");
    
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(userPath, "User IAM"));
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(livenessPath, "User IAM Liveness"));
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(rolesPath, "User IAM Roles"));
    buildProducer.produce(new IAMBuildItem(config.servicePath, livenessPath, rolesPath));
  }
}
