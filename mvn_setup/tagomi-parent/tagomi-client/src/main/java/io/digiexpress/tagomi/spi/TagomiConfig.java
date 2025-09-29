package io.digiexpress.tagomi.spi;

import java.util.List;

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

import io.digiexpress.tagomi.api.entities.TagomiContainer;
import io.digiexpress.tagomi.api.entities.TagomiContainer.TagomiDocType;
import io.resys.thena.git.api.GitClient;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;

@Value.Immutable
public interface TagomiConfig {
  GitClient getClient();
  String getRepoName();
  String getHeadName();
  AuthorProvider getAuthorProvider();
  ObjectMapper getObjectMapper();
  Serializer getSerializer();
  Deserializer getDeserializer();
  
  
  interface Commands {
    <T extends TagomiContainer.IsTagomiObject> Uni<T> delete(T toBeDeleted);
    <T extends TagomiContainer.IsTagomiObject> Uni<T> get(String blobId);
    <T extends TagomiContainer.IsTagomiObject> Uni<T> save(T toBeSaved);
    <T extends TagomiContainer.IsTagomiObject> Uni<T> create(T toBeSaved);
    Uni<List<? extends TagomiContainer.IsTagomiObject>> saveAll(List<TagomiContainer.IsTagomiObject> toBeSaved);
    Uni<List<? extends TagomiContainer.IsTagomiObject>> batch(BatchCommand batch);
  }  
    
  
  @Value.Immutable
  interface BatchCommand {
    List<TagomiContainer.IsTagomiObject> getToBeCreated();
    List<TagomiContainer.IsTagomiObject> getToBeSaved();
    List<TagomiContainer.IsTagomiObject> getToBeDeleted();
  }
  
  @FunctionalInterface
  interface AuthorProvider {
    String getAuthor();
  }
  
  @FunctionalInterface
  interface Serializer {
    JsonObject toString(TagomiContainer.IsTagomiObject entity);
  }
  
  interface Deserializer {
    <T extends TagomiContainer.IsTagomiObject> T fromString(JsonObject value);
    <T extends TagomiContainer.IsTagomiObject> T fromString(TagomiDocType type, JsonObject value);
  }
  
}
