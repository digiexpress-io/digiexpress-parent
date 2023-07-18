package io.resys.hdes.migration;

/*-
 * #%L
 * hdes-migration
 * %%
 * Copyright (C) 2020 - 2023 Copyright 2020 ReSys OÜ
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

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.ImmutableCreateStoreEntity;
import io.resys.hdes.client.api.ImmutableImportStoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.api.ast.ImmutableAstCommand;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.staticresources.StoreEntityLocation;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MigratorConfigs {
  public static ObjectMapper objectMapper = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
  
  public static StoreEntityLocation getAssetLocation() {
    return new StoreEntityLocation("classpath*:assets-tobe-migrated/");
  }
  
  public static String getRepoName() {
    return "migration-repo";
  }
  
  public static String getRootName() {
    return "assets-tobe-migrated";
  }
  
  public static String getFolderFromClasspath() {
    try {
      final var url = MigratorConfigs.class.getClassLoader().getResource("howto-mig.md");
      final var file = new File(url.toURI());
      final var target = new File(file.getParent(), getRootName());
      FileUtils.deleteDirectory(new File(target, getRepoName()));
      return target.getAbsolutePath();
    } catch(Exception e) {
      throw new RuntimeException("Can't read asset folder! " + e.getMessage(), e);
    }
  }
  
  public static HdesClient createClient(HdesStore store) {
    return HdesClientImpl.builder()
        .objectMapper(objectMapper)
        .store(store)
        .dependencyInjectionContext(new DependencyInjectionContext() {
          @Override
          public <T> T get(Class<T> type) {
            return null;
          }
        })
        .serviceInit(new ServiceInit() {
          @Override
          public <T> T get(Class<T> type) {
            try {
              return type.getDeclaredConstructor().newInstance();
            } catch(Exception e) {
              throw new RuntimeException(e.getMessage(), e);
            }
          }
        })
        .build();
  };
  

  
  public static ImmutableImportStoreEntity createBatch() {
    final var loadFrom = HdesInMemoryStore.builder()
        .objectMapper(MigratorConfigs.objectMapper)
        .location(MigratorConfigs.getAssetLocation())
        .build();
    final var state = loadFrom.query().get().await().atMost(Duration.ofMillis(10000));
    final var batch = ImmutableImportStoreEntity.builder();
    addToBatch(state.getDecisions(), batch);
    addToBatch(state.getServices(), batch);
    addToBatch(state.getFlows(), batch); 
    addYamlFlows(batch);
    addGroovyFiles(batch);
    return batch.build();
  }
  
  
  private static void addYamlFlows(ImmutableImportStoreEntity.Builder batch) {
    final var location  = MigratorConfigs.getAssetLocation().getFlowRegex().replace("json", "yaml");
    log.debug("Loading assets from: " + location + "!");
    final var resolver = new PathMatchingResourcePatternResolver();
    try {
      
      for (final var resource : resolver.getResources(location)) {
        log.debug("Loading yaml from: " + resource.getFilename() + "!");
        
        final var content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        final var command = ImmutableCreateStoreEntity.builder()
          .addBody(ImmutableAstCommand.builder().type(AstCommandValue.SET_BODY).value(content).build())
          .bodyType(AstBodyType.FLOW)
          .build();
        batch.addCreate(command);
      }

    } catch (Exception e) {
      throw new RuntimeException("Failed to load asset from: " + location + "!" + e.getMessage(), e);
    }
  }
  
  
  private static void addGroovyFiles(ImmutableImportStoreEntity.Builder batch) {
    final var location  = MigratorConfigs.getAssetLocation().getFlowTaskRegex().replace("json", "groovy");
    log.debug("Loading assets from: " + location + "!");
    final var resolver = new PathMatchingResourcePatternResolver();
    try {
      
      for (final var resource : resolver.getResources(location)) {
        log.debug("Loading groovy from: " + resource.getFilename() + "!");
        
        final var content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8)
            .replaceAll("io.resys.wrench.assets.context.stereotypes.WrenchFlowParameter", "io.resys.hdes.client.api.programs.ServiceData")
            .replaceAll("WrenchFlowParameter", "ServiceData");
        final var command = ImmutableCreateStoreEntity.builder()
          .addBody(ImmutableAstCommand.builder().type(AstCommandValue.SET_BODY).value(content).build())
          .bodyType(AstBodyType.FLOW_TASK)
          .build();
        batch.addCreate(command);
      }

    } catch (Exception e) {
      throw new RuntimeException("Failed to load asset from: " + location + "!" + e.getMessage(), e);
    }
  }
  
  
  private static void addToBatch(Map<String, StoreEntity> data, ImmutableImportStoreEntity.Builder batch) {
    data.values().forEach(v -> batch.addCreate(
        ImmutableCreateStoreEntity.builder()
        .addAllBody(v.getBody())
        .bodyType(v.getBodyType())
        .build()
      ));
  }
}
