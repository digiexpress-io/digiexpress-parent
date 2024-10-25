package io.digiexpress.eveli.assets.spi;

/*-
 * #%L
 * eveli-assets
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.digiexpress.eveli.assets.api.EveliAssetClient.Entity;
import io.digiexpress.eveli.assets.api.EveliAssetClient.EntityType;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Publication;
import io.digiexpress.eveli.assets.api.EveliAssetClient.Workflow;
import io.digiexpress.eveli.assets.api.EveliAssetClient.WorkflowTag;
import io.digiexpress.eveli.assets.api.EveliAssetClientConfig;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class EveliAssetsDeserializer implements EveliAssetClientConfig.Deserializer {
  private final ObjectMapper objectMapper;

  @SuppressWarnings("unchecked")
  @Override
  public  <T extends Entity<?>> T fromString(EntityType entityType, String value) {
    try {
      switch(entityType) {
        case PUBLICATION: {
          return (T) objectMapper.readValue(value, new TypeReference<Entity<Publication>>() {});  
        }
        case WORKFLOW: {
          return (T) objectMapper.readValue(value, new TypeReference<Entity<Workflow>>() {});  
        }
        case WORKFLOW_TAG: {
          return (T) objectMapper.readValue(value, new TypeReference<Entity<WorkflowTag>>() {});  
        }
        default: throw new RuntimeException("can't map: " + entityType);
      }
      
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  @Override
  public Entity<?> fromString(String value) {
    try {
      ObjectNode node = objectMapper.readValue(value, ObjectNode.class);
      final EntityType type = EntityType.valueOf(node.get("type").textValue());

      switch (type) {
      case PUBLICATION: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Publication>>() {});
      }
      case WORKFLOW: {
        return objectMapper.convertValue(node, new TypeReference<Entity<Workflow>>() {});
      }
      case WORKFLOW_TAG: {
        return objectMapper.convertValue(node, new TypeReference<Entity<WorkflowTag>>() {});
      }


      default:
        throw new RuntimeException("can't map: " + node);
      }

    } catch (Exception e) {
      throw new RuntimeException(e.getMessage() + System.lineSeparator() + value, e);
    }
  }
}
