package io.thestencil.client.spi;

/*-
 * #%L
 * stencil-client
 * %%
 * Copyright (C) 2021 - 2022 Copyright 2021 ReSys OÜ
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
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.thestencil.client.api.StencilClient.Article;
import io.thestencil.client.api.StencilClient.Entity;
import io.thestencil.client.api.StencilClient.EntityBody;
import io.thestencil.client.api.StencilClient.EntityType;
import io.thestencil.client.api.StencilClient.Link;
import io.thestencil.client.api.StencilClient.Locale;
import io.thestencil.client.api.StencilClient.Page;
import io.thestencil.client.api.StencilClient.Release;
import io.thestencil.client.api.StencilClient.Template;
import io.thestencil.client.api.StencilClient.Workflow;
import io.vertx.core.json.JsonObject;

public class StencilDeserializer {

  private ObjectMapper objectMapper;
  
  public StencilDeserializer(ObjectMapper objectMapper) {
    super();
    this.objectMapper = objectMapper;
  }
  
  public StencilDeserializer() {  
    this.objectMapper = new ObjectMapper();
    objectMapper.registerModule(new GuavaModule());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.registerModule(new Jdk8Module());
  
  }

  @SuppressWarnings("unchecked")
  public <T extends EntityBody> Entity<T> fromString(EntityType entityType, JsonObject json) {
    final var value = json.toString();
    try {
      switch(entityType) {
        case ARTICLE: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Article>>() {});  
        }
        case LINK: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Link>>() {});  
        }
        case LOCALE: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Locale>>() {});  
        }
        case PAGE: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Page>>() {});  
        }
        case RELEASE: {
          try {
            return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Release>>() {});
          } catch(Exception e) {
            // TODO:: remove
            return (Entity<T>) fromString(json);
          }
        }
        case WORKFLOW: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Workflow>>() {});  
        }
        case TEMPLATE: {
          return (Entity<T>) objectMapper.readValue(value, new TypeReference<Entity<Template>>() {});  
        }
        default: throw new RuntimeException("can't map: " + entityType);
      }
      
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public Entity<?> fromString(JsonObject value) {
    try {
      ObjectNode node = objectMapper.readValue(value.encode(), ObjectNode.class);
      final EntityType type = EntityType.valueOf(value.getString("type"));

      switch (type) {
      case ARTICLE: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Article>>() {});
      }
      case LINK: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Link>>() {});
      }
      case LOCALE: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Locale>>() {});
      }
      case PAGE: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Page>>() {});
      }
      case RELEASE: {
        // TODO: 
        @Deprecated
        final var created = node.get("body").has("created");
        if(!created) {
          ((ObjectNode) node.get("body")).set("created", TextNode.valueOf(LocalDateTime.now().toString()));
        }
        return objectMapper.convertValue(node, new TypeReference<Entity<Release>>() {});
      }
      case WORKFLOW: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Workflow>>() {});
      }
      case TEMPLATE: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Template>>() {});
      }
      default:
        throw new RuntimeException("can't map: " + node);
      }

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage() + System.lineSeparator() + value, e);
    }
  }
}
