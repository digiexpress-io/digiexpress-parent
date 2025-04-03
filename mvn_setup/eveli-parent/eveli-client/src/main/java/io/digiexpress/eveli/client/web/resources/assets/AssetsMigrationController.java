package io.digiexpress.eveli.client.web.resources.assets;

/*-
 * #%L
 * eveli-client
 * %%
 * Copyright (C) 2015 - 2025 Copyright 2022 ReSys OÜ
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

import java.util.Collections;
import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.ImmutableList;

import io.digiexpress.eveli.client.web.resources.assets.AssetsDeploymentController.EveliDeploymentUpload;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.digiexpress.eveli.envir.spi.actions.DeploymentEnvirDialobUploader;
import io.digiexpress.eveli.envir.spi.actions.EveliDeploymentCompilerLogger;
import io.resys.hdes.client.api.HdesClient;
import io.resys.hdes.client.api.HdesComposer.ComposerState;
import io.resys.hdes.client.api.HdesStore.StoreEntity;
import io.resys.hdes.client.api.ImmutableComposerState;
import io.resys.hdes.client.api.ImmutableCreateStoreEntity;
import io.resys.hdes.client.api.ImmutableImportStoreEntity;
import io.resys.hdes.client.api.ImmutableUpdateStoreEntityWithBodyType;
import io.resys.hdes.client.spi.composer.ComposerEntityMapper;
import io.smallrye.mutiny.Uni;
import io.thestencil.client.api.StencilClient;
import io.thestencil.client.api.StencilComposer.SiteState;
import io.thestencil.client.spi.MarkdownBuilderImpl;
import io.thestencil.client.spi.SitesBuilderImpl;
import io.thestencil.client.spi.StencilComposerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequiredArgsConstructor
@RequestMapping("/worker/rest/api/assets/migration")
@Slf4j
public class AssetsMigrationController {
  
  private final StencilClient stencilClient;
  private final HdesClient wrenchClient;
  private final DialobClient dialobClient;

  
  @PostMapping
  public Uni<EveliDeploymentUpload> migrate(@RequestBody EveliDeploymentUpload body) {
    
    return Uni.combine().all().unis(
        Uni.createFrom().item(overwriteDialobAssets(body)),
        overwriteStencilAssets(body),
        overwriteWrenchAssets(body)
    ).asTuple().onItem().transform(e -> body);
    
  }
  
  private int overwriteDialobAssets(EveliDeploymentUpload body) {
    final var state = body.getSources().getStencil();
    final var markdowns = new MarkdownBuilderImpl()
      .targetDate(null)
      .json(state, true)
      .build();
    
    final var site = new SitesBuilderImpl()
      .imagePath("images")
      .created(System.currentTimeMillis())
      .source(markdowns)
      .tagName(body.getName())
      .build();
    
    final var logger = new EveliDeploymentCompilerLogger();
    return new DeploymentEnvirDialobUploader(dialobClient, body.getSources(), site, logger).accept();
  }
  
  

  private Uni<SiteState> overwriteStencilAssets(EveliDeploymentUpload body) {
    return new StencilComposerImpl(stencilClient).migration().importData(body.getSources().getStencil());
  }
  
  private Uni<List<StoreEntity>> overwriteWrenchAssets(EveliDeploymentUpload body) {
    return wrenchClient.store().query().get()
      .onItem().transform((source) -> {
        // create envir
        final var envir = ComposerEntityMapper.toEnvir(wrenchClient.envir().tagName(source.getTagName()), source).build();
        
        // map envir
        final var builder = ImmutableComposerState.builder();
        envir.getValues().values().forEach(v -> ComposerEntityMapper.toComposer(builder, v));
        return (ComposerState) builder.build(); 
      })
      .onItem().transform((state) -> {
        
        final var allIds = ImmutableList.<String>builder()
          .addAll(state.getDecisions().values().stream().map(e -> e.getId()).toList())
          .addAll(state.getServices().values().stream().map(e -> e.getId()).toList())
          .addAll(state.getFlows().values().stream().map(e -> e.getId()).toList())
          .build();
        
        final var builder = ImmutableImportStoreEntity.builder();
        
        final var wrenchTag = body.getSources().getWrench();
        
        wrenchTag.getValues().stream().forEach(entry -> {
          
          if(allIds.contains(entry.getId())) {
            builder.addUpdate(ImmutableUpdateStoreEntityWithBodyType.builder()
                .bodyType(entry.getBodyType())
                .id(entry.getId())
                .body(entry.getCommands())
                .build());
          } else {
            builder.addCreate(ImmutableCreateStoreEntity.builder()
                .bodyType(entry.getBodyType())
                .id(entry.getId())
                .body(entry.getCommands())
                .build());
          }
          
        });
        
        return builder.build();
        
      })
      .onItem().transformToUni(batch -> {
        if(batch.getCreate().isEmpty() && batch.getUpdate().isEmpty()) {
          return Uni.createFrom().item(Collections.emptyList());
        }
        return wrenchClient.store().batch(batch);
      });
  } 
}
