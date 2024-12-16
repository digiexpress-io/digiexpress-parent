package io.digiexpress.eveli.assets.api;

/*-
 * #%L
 * stencil-persistence
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

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityType;
import io.resys.thena.api.ThenaClient;
import io.vertx.core.json.JsonObject;


@Value.Immutable
public interface EveliAssetClientConfig {
  ThenaClient getClient();
  String getRepoName();
  String getHeadName();
  AuthorProvider getAuthorProvider();
  ObjectMapper getObjectMapper();
  Serializer getSerializer();
  Deserializer getDeserializer();
  GidProvider getGidProvider();
    
  @FunctionalInterface
  interface GidProvider {
    String getNextId();
  }
  
  @FunctionalInterface
  interface AuthorProvider {
    String getAuthor();
  }
  
  @FunctionalInterface
  interface Serializer {
    JsonObject toString(Entity<?> entity);
  }
  
  interface Deserializer {
    Entity<?> fromString(String value);
    <T extends Entity<?>> T fromString(EntityType type, String value);
  }
  
}
