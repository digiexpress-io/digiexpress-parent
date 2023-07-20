package io.resys.hdes.quarkus.composer.pg.deployment;

/*-
 * #%L
 * quarkus-composer-pg-deployment
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

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Hex;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.bootstrap.model.AppArtifact;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.GeneratedResourceBuildItem;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.deployment.builditem.LiveReloadBuildItem;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.quarkus.deployment.configuration.ConfigurationError;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.deployment.util.WebJarUtil;
import io.quarkus.vertx.http.deployment.BodyHandlerBuildItem;
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.deployment.devmode.NotFoundPageDisplayableEndpointBuildItem;
import io.resys.hdes.client.spi.Serializers;
import io.resys.hdes.client.spi.web.HdesWebConfig;
import io.resys.hdes.quarkus.composer.pg.ComposerBeansProducer;
import io.resys.hdes.quarkus.composer.pg.ComposerRecorder;
import io.resys.hdes.quarkus.composer.pg.ComposerRuntimeConfig;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;



public class ComposerProcessor {
  private static final String WEBJAR_GROUP_ID = "io.digiexpress";
  private static final String WEBJAR_ARTIFACT_ID = "hdes-composer-ui";
  private static final String WEBJAR_PREFIX = "META-INF/resources/webjars/" + WEBJAR_ARTIFACT_ID + "/";
  
  private static final String FINAL_DESTINATION = "META-INF/portal-files";
  
  @Inject
  private LaunchModeBuildItem launch;
  
  ComposerCompiletimeConfig config;
  
  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(ComposerRecorder.FEATURE_BUILD_ITEM);
  }
  
  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  public void frontendConfig(
    ComposerRecorder recorder,
    HttpRootPathBuildItem httpRoot,
    BuildProducer<RouteBuildItem> routes,
    ComposerFrontendBuildItem buildItem,
    BodyHandlerBuildItem body) throws Exception {
    
    final var frontendPath = httpRoot.resolvePath(config.frontendPath);
    final Handler<RoutingContext> router = recorder.frontendRouter(buildItem.getUiFinalDestination(), buildItem.getUiPath(), buildItem.getHash());
    
    routes.produce(httpRoot.routeBuilder()
        .route(frontendPath)
        .handler(router)
        .displayOnNotFoundPage()
        .build());
    routes.produce(httpRoot.routeBuilder()
        .route(frontendPath + "/")
        .handler(router)
        .displayOnNotFoundPage()
        .build());
    
    routes.produce(httpRoot.routeBuilder()
        .route(frontendPath + "/*")
        .handler(router)
        .displayOnNotFoundPage()
        .build());
  }
  
  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  void runtimeConfig(
      ComposerRuntimeConfig config,
      ComposerBuildItem buildItem,
      ComposerRecorder recorder,
      
      BeanContainerBuildItem beanContainer, 
      ShutdownContextBuildItem shutdown) {

    
    recorder.configureRuntimeConfig(config);
  }
  
  @BuildStep
  @Record(ExecutionTime.RUNTIME_INIT)
  public void router(
    ComposerBuildItem buildItem,
    ComposerRecorder recorder,
    HttpRootPathBuildItem httpRoot,
    BuildProducer<RouteBuildItem> routes,
    BodyHandlerBuildItem body,
    ComposerCompiletimeConfig config) throws Exception {
    
    final var bodyHandler = body.getHandler();
    final Handler<RoutingContext> router = recorder.backendRouter();
    
    
    final Consumer<String> addRoute = (path) -> {
      routes.produce(httpRoot.routeBuilder()
          .routeFunction(path, recorder.routeFunction(bodyHandler))
          .handler(router)
          .displayOnNotFoundPage()
          .build());
      routes.produce(httpRoot.routeBuilder()
          .routeFunction(path + "/", recorder.routeFunction(bodyHandler))
          .handler(router)
          .displayOnNotFoundPage()
          .build());
    };
    
    addRoute.accept(buildItem.getConfig().getServicePath());
    addRoute.accept(buildItem.getConfig().getCommandsPath());
    addRoute.accept(buildItem.getConfig().getCopyasPath());
    addRoute.accept(buildItem.getConfig().getDebugsPath());
    addRoute.accept(buildItem.getConfig().getExportsPath());
    addRoute.accept(buildItem.getConfig().getHistoryPath());
    addRoute.accept(buildItem.getConfig().getImportsPath());
    addRoute.accept(buildItem.getConfig().getModelsPath());
    addRoute.accept(buildItem.getConfig().getResourcesPath());

    routes.produce(httpRoot.routeBuilder()
        .routeFunction(buildItem.getConfig().getHistoryPath() + "/:id", recorder.idRouteFunctionGet(bodyHandler))
        .handler(router)
        .displayOnNotFoundPage()
        .build());
    routes.produce(httpRoot.routeBuilder()
        .routeFunction(buildItem.getConfig().getResourcesPath() + "/:id", recorder.idRouteFunctionGet(bodyHandler))
        .handler(router)
        .displayOnNotFoundPage()
        .build());

  }
  
  
  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  void deploymentConfig(
      ComposerBuildItem buildItem,
      ComposerRecorder recorder,
      BuildProducer<AdditionalBeanBuildItem> buildItems,
      BuildProducer<BeanContainerListenerBuildItem> beans) {
    
    buildItems.produce(AdditionalBeanBuildItem.builder().setUnremovable().addBeanClass(ComposerBeansProducer.class).build());
    beans.produce(new BeanContainerListenerBuildItem(recorder.configureBuildtimeConfig(
        buildItem.getConfig().getServicePath(), 
        buildItem.getConfig().getModelsPath(),
        buildItem.getConfig().getExportsPath(), 
        buildItem.getConfig().getCommandsPath(), 
        buildItem.getConfig().getDebugsPath(), 
        buildItem.getConfig().getImportsPath(), 
        buildItem.getConfig().getCopyasPath(), 
        buildItem.getConfig().getResourcesPath(), 
        buildItem.getConfig().getHistoryPath())));
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
  //@Record(ExecutionTime.STATIC_INIT)
  public void buildItem(
      BuildProducer<ComposerBuildItem> buildProducer,
      HttpRootPathBuildItem httpRootPathBuildItem,
      BuildProducer<NotFoundPageDisplayableEndpointBuildItem> displayableEndpoints) throws Exception {
    

    final var servicePath = "/" + cleanPath(config.servicePath);
    final var webConfig = HdesWebConfig.builder()
        .servicePath(servicePath)
        .modelsPath(servicePath     + "/" + HdesWebConfig.MODELS)
        .exportsPath(servicePath    + "/" + HdesWebConfig.EXPORTS)
        .commandsPath(servicePath   + "/" + HdesWebConfig.COMMANDS)
        .debugsPath(servicePath     + "/" + HdesWebConfig.DEBUGS)
        .importsPath(servicePath    + "/" + HdesWebConfig.IMPORTS)
        .copyasPath(servicePath     + "/" + HdesWebConfig.COPYAS)
        .resourcesPath(servicePath  + "/" + HdesWebConfig.RESOURCES)
        .historyPath(servicePath    + "/" + HdesWebConfig.HISTORY)
        .build();
    displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(servicePath), "Hdes Composer Backend"));
    buildProducer.produce(new ComposerBuildItem(webConfig));
  }
  
  @BuildStep
  //@Record(ExecutionTime.STATIC_INIT)
  public void frontendBuildItem(
      ComposerBuildItem composerBuildItem,
      BuildProducer<ComposerFrontendBuildItem> buildProducer,
      
      BuildProducer<GeneratedResourceBuildItem> generatedResources,
      BuildProducer<NativeImageResourceBuildItem> nativeImage,
      
      NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
      CurateOutcomeBuildItem curateOutcomeBuildItem,
      
      LiveReloadBuildItem liveReloadBuildItem,
      HttpRootPathBuildItem httpRootPathBuildItem,
      BuildProducer<NotFoundPageDisplayableEndpointBuildItem> displayableEndpoints) throws Exception {

    if(!config.frontend) {
      return;
    }

    final String hash = Hex.encodeHexString(LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8), true);    
    final AppArtifact artifact = WebJarUtil.getAppArtifact(curateOutcomeBuildItem, WEBJAR_GROUP_ID, WEBJAR_ARTIFACT_ID);    
    if (launch.getLaunchMode().isDevOrTest()) {
      
      final var tempPath = WebJarUtil
          .copyResourcesForDevOrTest(liveReloadBuildItem, curateOutcomeBuildItem, launch, artifact, WEBJAR_PREFIX + artifact.getVersion(), false);
      
      // Update index.html
      final var index = tempPath.resolve("index.html");
      final String frontendPath = httpRootPathBuildItem.resolvePath(config.frontendPath);
      
      WebJarUtil.updateFile(index, IndexFactory.builder()
        .frontend(frontendPath)
        .server(httpRootPathBuildItem.resolvePath(composerBuildItem.getConfig().getServicePath()))
        .index(index)
        .build());
      
      buildProducer.produce(new ComposerFrontendBuildItem(tempPath.toAbsolutePath().toString(), frontendPath, hash));
      displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(frontendPath), "Hdes Composer Frontend"));

      // Handle live reload of branding files
      if (liveReloadBuildItem.isLiveReload() && !liveReloadBuildItem.getChangedResources().isEmpty()) {
          WebJarUtil.hotReloadBrandingChanges(
              curateOutcomeBuildItem, launch, artifact,
                  liveReloadBuildItem.getChangedResources());
      }
    
    } else {
      final var frontendPath = httpRootPathBuildItem.resolvePath(config.frontendPath);
      final var files = WebJarUtil.copyResourcesForProduction(curateOutcomeBuildItem, artifact, WEBJAR_PREFIX + artifact.getVersion());

      boolean indexReplaced = false; 
      for (final var file : files.entrySet()) {
        String fileName = file.getKey();
        byte[] content;
        if (fileName.endsWith("index.html")) {
          content = IndexFactory.builder()
              .frontend(frontendPath)
              .server(httpRootPathBuildItem.resolvePath(composerBuildItem.getConfig().getServicePath()))
              .index(file.getValue())
              .build();
          indexReplaced = true;
        } else {
          content = file.getValue();
        }
        
        fileName = FINAL_DESTINATION + "/" + fileName;
        generatedResources.produce(new GeneratedResourceBuildItem(fileName, content));
        nativeImage.produce(new NativeImageResourceBuildItem(fileName));
      }
      
      if(!indexReplaced) {
        throw new ConfigurationError(new StringBuilder("Failed to create composer frontend index.html, ")
            .append("artifact = ").append(artifact).append(System.lineSeparator()).append(",")
            .append("path = ").append(frontendPath).append("!")
            .append("final destination = ").append(FINAL_DESTINATION).append("!")
            .toString());
      }
      
      buildProducer.produce(new ComposerFrontendBuildItem(FINAL_DESTINATION, frontendPath, hash));
      displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(frontendPath), "Hdes Composer Frontend"));
    }
  }
  
  private static String cleanPath(String value) {
    return ComposerBeansProducer.cleanPath(value);
  }
}
