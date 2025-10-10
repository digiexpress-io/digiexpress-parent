package io.digiexpress.tagomi.spi;

/*-
 * #%L
 * tagomi-client
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

import java.util.Objects;
import java.util.Optional;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.tagomi.api.TagomiClient;
import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiWorld;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class TagomiClientImpl implements TagomiClient {
  private final ObjectMapper objectMapper;
  private final WorldDatasource datasource;
  private final RestTemplate restTemplate;
  private final String baseUrl;
  
  @Override
  public WorldBuilder createWorld() {
    return new WorldBuilder() {
      WorldDatasource user_datasource;  
      TagomiContainer container;
      
      @Override
      public WorldBuilder datasource(WorldDatasource user_datasource) {
        this.user_datasource = user_datasource;
        return this;
      }
      @Override
      public WorldBuilder container(TagomiContainer container) {
        Objects.requireNonNull(container, () -> "container must be defined");
        this.container = container;
        return this;
      }
      
      @Override
      public Uni<TagomiWorld> build() {
        Objects.requireNonNull(container, () -> "container must be defined");
        return Uni.createFrom().item(() -> new TagomiWorldImpl(
            objectMapper,
            container, 
            Optional.ofNullable(user_datasource).orElse(datasource), 
            restTemplate, 
            baseUrl));
      }
    };
  }
}
