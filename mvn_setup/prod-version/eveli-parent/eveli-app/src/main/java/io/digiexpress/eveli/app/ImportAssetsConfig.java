package io.digiexpress.eveli.app;

/*-
 * #%L
 * eveli-app
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

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.assets.api.EveliAssetClient;
import io.digiexpress.eveli.assets.api.EveliAssetComposer.Deployment;
import io.digiexpress.eveli.assets.spi.builders.DeploymentImporter;
import io.digiexpress.eveli.client.config.EveliContext;
import io.digiexpress.eveli.client.config.EveliPropsAssets;
import io.digiexpress.eveli.dialob.api.DialobClient;
import io.resys.hdes.client.api.HdesClient;
import io.thestencil.client.api.StencilClient;

@RestController
@RequestMapping("init")
public class ImportAssetsConfig {

  @Autowired EveliPropsAssets config;
  @Autowired ObjectMapper objectMapper;
  @Autowired DialobClient dialobClient;
  @Autowired EveliContext eveliContext;
  
  @GetMapping
  public ResponseEntity<String> initAssets() throws StreamReadException, DatabindException, IOException {
    
    final var importData = objectMapper.readValue(new File(config.getImportDeployment()), Deployment.class);
    
    
    final HdesClient wrench = eveliContext.getWrench();
    final StencilClient stencil = eveliContext.getStencil();
    final EveliAssetClient eveliAssets = eveliContext.getAssets();
    new DeploymentImporter(dialobClient, wrench, stencil, eveliAssets).importData(importData).await().atMost(Duration.ofMinutes(5));
    
    return ResponseEntity.ok("imported");
  }

}
