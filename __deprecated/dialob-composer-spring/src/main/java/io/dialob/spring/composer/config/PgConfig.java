package io.dialob.spring.composer.config;

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

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.client.api.DialobStore;
import io.dialob.client.api.DialobStore.DialobCredsSupplier;
import io.dialob.client.api.ImmutableDialobCreds;
import io.dialob.client.pgsql.PgSqlDialobStore;

@ConditionalOnProperty(name = "dialob.formdb.pg.enabled", havingValue = "true")
public class PgConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(PgConfig.class);

  @Bean
  public DialobStore hdesStore(Optional<DialobCredsSupplier> authorProvider, PgConfigBean config, ObjectMapper objectMapper) {
    final DialobCredsSupplier creds;
    if(authorProvider.isEmpty()) {
      creds = () -> ImmutableDialobCreds.builder().user("assetManager").email("assetManager@resys.io").build();
    } else {
      creds = authorProvider.get();
    }

    return PgSqlDialobStore.builder()
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

  @ConditionalOnProperty(name = "dialob.formdb.pg.autoCreate", havingValue = "true")
  @Bean
  public Loader autoCreate(DialobStore store) {
    return new Loader(store);
  }

  public static class Loader {
    private final DialobStore store;
    public Loader(DialobStore store) {
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
