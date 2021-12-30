package io.resys.wrench.assets.bundle.spi;

import java.time.Duration;

/*-
 * #%L
 * hdes-spring-bundle-editor
 * %%
 * Copyright (C) 2020 - 2021 Copyright 2020 ReSys OÜ
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

import java.util.Optional;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.config.GitConfig.GitCredsSupplier;
import io.resys.hdes.client.api.config.ImmutableGitCreds;
import io.resys.hdes.client.spi.store.HdesDocumentStore;
import io.resys.wrench.assets.context.config.PgConfigBean;

@ConditionalOnProperty(name = "wrench.assets.pg.enabled", havingValue = "true")
public class AssetPgConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(AssetPgConfiguration.class);
  
  @Bean
  public HdesStore hdesStore(Optional<GitCredsSupplier> authorProvider, PgConfigBean config, ObjectMapper objectMapper) {
    final GitCredsSupplier creds;
    if(authorProvider.isEmpty()) {
      creds = () -> ImmutableGitCreds.builder().user("assetManager").email("assetManager@resys.io").build();  
    } else {
      creds = authorProvider.get();
    }
    
    return HdesDocumentStore.builder()
        .pgHost(config.getPgHost())
        .pgPort(config.getPgPort())
        .pgDb(config.getPgDb())
        .pgPoolSize(config.getPgPoolSize())
        .pgUser(config.getPgUser())
        .pgPass(config.getPgPass())
        .objectMapper(objectMapper)
        .repoName(config.getRepositoryName())
        .headName(config.getBranchSpecifier())
        .authorProvider(() -> creds.get().getUser())
        .objectMapper(objectMapper)
        .build();
  }
  
  @ConditionalOnProperty(name = "wrench.assets.pg.autoCreate", havingValue = "true")
  @Bean
  public Loader autoCreate(HdesStore store) {
    return new Loader(store);
  }
  
  public static class Loader {
    private final HdesStore store;
    public Loader(HdesStore store) {
      super();
      this.store = store;
    }
    @PostConstruct
    public void doLoad() {
      final var autCreated = store.repo().createIfNot().await().atMost(Duration.ofMillis(1000));
      LOGGER.debug("REPO auto created: " + autCreated);
    }  
  }
}
