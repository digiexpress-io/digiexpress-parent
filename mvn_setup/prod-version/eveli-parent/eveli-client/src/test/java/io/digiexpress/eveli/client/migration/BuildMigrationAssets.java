package io.digiexpress.eveli.client.migration;

/*-
 * #%L
 * eveli-client
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.dialob.api.form.Form;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.Deployment;
import io.digiexpress.eveli.assets.api.ImmutableDeployment;
import io.digiexpress.eveli.assets.api.ImmutablePublication;
import io.digiexpress.eveli.assets.api.ImmutableWorkflow;
import io.digiexpress.eveli.assets.api.ImmutableWorkflowTag;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.ast.AstBody;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTag.AstTagValue;
import io.resys.hdes.client.api.ast.ImmutableAstTag;
import io.resys.hdes.client.api.ast.ImmutableHeaders;
import io.resys.hdes.client.spi.HdesClientImpl;
import io.resys.hdes.client.spi.HdesInMemoryStore;
import io.resys.hdes.client.spi.config.HdesClientConfig.DependencyInjectionContext;
import io.resys.hdes.client.spi.config.HdesClientConfig.ServiceInit;
import io.resys.hdes.client.spi.flow.validators.IdValidator;
import io.thestencil.client.api.ImmutableSiteState;
import io.thestencil.client.api.StencilComposer;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class BuildMigrationAssets {
  
  private static final String JSON_DIR = "classpath*:assets-to-migrate/**/*.json"; 
  private static final ResourcePatternResolver RESOLVER = new PathMatchingResourcePatternResolver();
  public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModules(new JavaTimeModule(), new Jdk8Module(), new GuavaModule());
  
  
  private static final HdesClient WRENCH = HdesClientImpl.builder()
      .store(HdesInMemoryStore.builder().objectMapper(OBJECT_MAPPER).build())
      .objectMapper(OBJECT_MAPPER)
      .serviceInit(new ServiceInit() { @Override public <T> T get(Class<T> type) { return null; } })
      .dependencyInjectionContext( new DependencyInjectionContext() { @Override public <T> T get(Class<T> type) { return null; } })
      .flowVisitors(new IdValidator())
      .build();
  
  
  private SiteState stencil;
  private AstTag wrench;
  private List<JsonObject> dialob = new ArrayList<>();
  private String workflowTagName;
  private List<JsonObject> workflows = new ArrayList<>();
  
  public Deployment build() {
    final var assets = list(JSON_DIR);
    assets.stream()
      .map(this::visitAnyResource)
      .forEach(this::visitAnyAsset);
    
    return visitDeployment();
  }
  
  private Deployment visitDeployment() {
    return ImmutableDeployment.builder()
        .created(LocalDateTime.now())
        
        .source(ImmutablePublication.builder()
            .name("migration")
            .created(LocalDateTime.now())
            .liveDate(LocalDateTime.now())
            .description("created by migrating all loose assets")
            .stencilTagName(Optional.ofNullable(stencil).map(s -> s.getName()).orElse("tag-not-found"))
            .wrenchTagName(Optional.ofNullable(wrench).map(s -> s.getName()).orElse("tag-not-found"))
            .workflowTagName(Optional.ofNullable(workflowTagName).orElse("tag-not-found"))
            .build())
        
        .wrenchTag(Optional.ofNullable(wrench).orElse(ImmutableAstTag.builder()
            .created(LocalDateTime.now())
            .bodyType(AstBodyType.TAG)
            .headers(ImmutableHeaders.builder().build())
            .name("tag-not-found")
            .build()))
        
        .stencilTag(Optional.ofNullable(stencil).orElse(ImmutableSiteState.builder()
            .commit("tag-not-found")
            .contentType(StencilComposer.SiteContentType.OK)
            .name("tag-not-found")
            .build()))
        
        .workflowTag(ImmutableWorkflowTag.builder()
            .parentCommit("")
            .created(LocalDateTime.now())
            .name(workflowTagName)
            .description("migrated")
            .addAllEntries(this.workflows.stream().map(oldWk -> {
              return ImmutableWorkflow.builder()
                  .name(oldWk.getString("name"))
                  .flowName(oldWk.getString("flowName"))
                  .formName(oldWk.getString("formName"))
                  .formTag(oldWk.getString("formTag"))
                  .formId(Optional.ofNullable(oldWk.getString("flowid")).orElse(""))
                  .updated(ZonedDateTime.now())
                  .build();
            }).toList())
            .build())
        
        .addAllDialobTag(dialob.stream().map(json -> {
          try {
            return OBJECT_MAPPER.readValue(json.encode(), Form.class);
          } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
          } 
        }).toList())
        
        .build();
    
  }
  
  private void visitAnyAsset(JsonObject anyAsset) {
    try {
      final var fieldNames = anyAsset.fieldNames();
  
      // dialob asset
      if(fieldNames.contains("_id") && fieldNames.contains("_rev")) {
        log.info("found dialob form: {}", anyAsset.getString("name"));
        dialob.add(anyAsset);
        return;
      } 
      
      
      // wrench
      if(fieldNames.contains("headers") && fieldNames.contains("bodyType")) {
        final var tag = OBJECT_MAPPER.readValue(anyAsset.encode(), AstTag.class);
        this.wrench = tag;
        
        log.info("found wrench tag: {}", anyAsset.getString("name"));
        
        tag.getValues().stream()
          .map(this::visitWrenchAsset)
          .forEach(value -> log.info("found {}: {}", value.getBodyType(), value.getName()));
        return;
        
      }
      
      
      // workflow
      if(fieldNames.contains("id") && fieldNames.contains("name") && fieldNames.contains("entries")) {
        this.workflowTagName = anyAsset.getString("name");
        
        log.info("found workflows tag: {}", anyAsset.getString("name"));
        final var workflows = anyAsset.getJsonArray("entries");
        workflows.forEach(smth -> {
          final var workflow = (JsonObject) smth;
          this.workflows.add(workflow);
          
          log.info("found workflow: {} - flow: {}, form: {} / {}", 
              workflow.getString("name"),
              workflow.getString("flowName"),
              workflow.getString("formName"),
              workflow.getString("formTag")
          );
          
        });
        return;
      }
      
      // stencil
      if(fieldNames.contains("pages")) {
        anyAsset.remove("releases");
        final var tag = OBJECT_MAPPER.readValue(anyAsset.encode(), ImmutableSiteState.class);
        //this.stencil = tag;
        log.info("found stencul tag: {}", tag.getName());
        
        tag.getWorkflows().values().forEach(wk -> {
          log.info("found stencil workflow: {}", wk.getBody().getValue());
        });
        return;
      }
      
      throw new IllegalArgumentException("Unexpected asset: " + anyAsset);
    
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  
  private AstBody visitWrenchAsset(AstTagValue value) {
    switch (value.getBodyType()) {
      case DT: {
        return WRENCH.ast().commands(value.getCommands()).decision();
      }
      case FLOW: {
        return WRENCH.ast().commands(value.getCommands()).flow();
      }
      case FLOW_TASK: {
        return WRENCH.ast().commands(value.getCommands()).service();
      }
      default: throw new IllegalArgumentException("Unexpected value: " + value.getBodyType());
    }
  }
  
  
  private JsonObject visitAnyResource(Resource resource) {
    try {
      final var content = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
      final var json = new JsonObject(content);
      return json;
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    } 
  }
  
  private List<Resource> list(String location) {
    try {
      List<Resource> files = new ArrayList<>();
      for (Resource resource : RESOLVER.getResources(location)) {
        files.add(resource);
        log.info("+1 to migration list: {}", resource.getFilename());
      }
      return files;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load asset from: " + location + "!" + e.getMessage(), e);
    }
  }

}
