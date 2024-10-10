package io.thestencil.quarkus.ide;

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

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.codec.binary.Hex;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
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
import io.quarkus.deployment.builditem.nativeimage.NativeImageResourceBuildItem;
import io.quarkus.deployment.configuration.ConfigurationError;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.deployment.util.WebJarUtil;
import io.quarkus.vertx.http.deployment.BodyHandlerBuildItem;
import io.quarkus.vertx.http.deployment.HttpRootPathBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.deployment.devmode.NotFoundPageDisplayableEndpointBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class FrontendProcessor {
  private static final String WEBJAR_GROUP_ID = "io.thestencil";
  private static final String WEBJAR_ARTIFACT_ID = "stencil-composer-integration";
  private static final String WEBJAR_PREFIX = "META-INF/resources/webjars/" + WEBJAR_ARTIFACT_ID + "/";
  private static final String FINAL_DESTINATION = "META-INF/portal-files";

  
  @Inject
  private LaunchModeBuildItem launch;
  
  FrontendConfig config;
  
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
    FrontendRecorder recorder,
    HttpRootPathBuildItem httpRoot,
    BuildProducer<RouteBuildItem> routes,
    HdesUIBuildItem buildItem,
    FrontendConfig uiConfig,
    BodyHandlerBuildItem body) throws Exception {
    
    Handler<RoutingContext> handler = recorder.uiHandler(buildItem.getUiFinalDestination(), buildItem.getUiPath(), buildItem.getHash());
        routes.produce(httpRoot.routeBuilder()
        .route(uiConfig.servicePath)
        .handler(handler)
        .build());
    routes.produce(httpRoot.routeBuilder()
        .route(uiConfig.servicePath + "/*")
        .handler(handler)
        .build());
  }
  
  @BuildStep
  @Record(ExecutionTime.STATIC_INIT)
  public void frontendBeans(
      FrontendRecorder recorder,
      BuildProducer<HdesUIBuildItem> buildProducer,
      
      BuildProducer<GeneratedResourceBuildItem> generatedResources,
      BuildProducer<NativeImageResourceBuildItem> nativeImage,
      
      NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
      CurateOutcomeBuildItem curateOutcomeBuildItem,
      
      LiveReloadBuildItem liveReloadBuildItem,
      HttpRootPathBuildItem httpRootPathBuildItem,
      BuildProducer<NotFoundPageDisplayableEndpointBuildItem> displayableEndpoints) throws Exception {

    final String hash = Hex.encodeHexString(LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8), true);
    
    final var artifact = WebJarUtil.getAppArtifact(curateOutcomeBuildItem, WEBJAR_GROUP_ID, WEBJAR_ARTIFACT_ID);    
    if (launch.getLaunchMode().isDevOrTest()) {
      
      Path tempPath = WebJarUtil
          .copyResourcesForDevOrTest(liveReloadBuildItem, curateOutcomeBuildItem, launch, artifact, WEBJAR_PREFIX + artifact.getVersion(), false);
      
      // Update index.html
      Path index = tempPath.resolve("index.html");
      final String frontendPath = httpRootPathBuildItem.resolvePath(config.servicePath);
      
      WebJarUtil.updateFile(index, IndexFactory.builder()
        .frontend(frontendPath)
        .locked(config.locked)
        .oidc(config.oidcPath.orElse(null))
        .status(config.statusPath.orElse(null))
        .server(httpRootPathBuildItem.resolvePath(config.serverPath))
        .index(index)
        .build());
      
      buildProducer.produce(new HdesUIBuildItem(tempPath.toAbsolutePath().toString(), frontendPath, hash));
      displayableEndpoints.produce(new NotFoundPageDisplayableEndpointBuildItem(httpRootPathBuildItem.resolvePath(frontendPath + "/"), "Stencil Composer"));

      // Handle live reload of branding files
      if (liveReloadBuildItem.isLiveReload() && !liveReloadBuildItem.getChangedResources().isEmpty()) {
          WebJarUtil.hotReloadBrandingChanges(
              curateOutcomeBuildItem, launch, artifact,
                  liveReloadBuildItem.getChangedResources());
      }
    
    } else {
      final String frontendPath = httpRootPathBuildItem.resolvePath(config.servicePath);
      Map<String, byte[]> files = WebJarUtil.copyResourcesForProduction(curateOutcomeBuildItem, artifact, WEBJAR_PREFIX + artifact.getVersion());

      boolean indexReplaced = false; 
      for (Map.Entry<String, byte[]> file : files.entrySet()) {
        String fileName = file.getKey();
        byte[] content;
        if (fileName.endsWith("index.html")) {
          content = IndexFactory.builder()
              .frontend(frontendPath)
              .locked(config.locked)
              .server(config.serverPath)
              .oidc(config.oidcPath.orElse(null))
              .status(config.statusPath.orElse(null))
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
        throw new ConfigurationError(new StringBuilder("Failed to create stencil-ide index.html, ")
            .append("artifact = ").append(artifact).append(System.lineSeparator()).append(",")
            .append("path = ").append(frontendPath).append("!")
            .append("final destination = ").append(FINAL_DESTINATION).append("!")
            .toString());
      }
      
      buildProducer.produce(new HdesUIBuildItem(FINAL_DESTINATION, frontendPath, hash));
    }
  }
}
