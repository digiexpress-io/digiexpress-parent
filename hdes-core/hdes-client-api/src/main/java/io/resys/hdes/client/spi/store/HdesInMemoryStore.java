package io.resys.hdes.client.spi.store;

/*-
 * #%L
 * hdes-client-api
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesComposer.StoreDump;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ImmutableStoreState;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstBody.AstSource;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.spi.staticresources.Sha2;
import io.resys.hdes.client.spi.staticresources.StoreEntityLocation;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.smallrye.mutiny.Uni;

public class HdesInMemoryStore implements HdesStore {
  private static final Logger LOGGER = LoggerFactory.getLogger(HdesInMemoryStore.class);
  private final Map<String, StoreEntity> entities;
  private final StoreState state;
  
  
  public HdesInMemoryStore(Map<String, StoreEntity> entities) {
    super();
    this.entities = entities;
    final var builder = ImmutableStoreState.builder();
    for(final var entity : entities.values()) {
      switch (entity.getBodyType()) {
      case DT: builder.putDecisions(entity.getId(), entity); break;
      case FLOW: builder.putFlows(entity.getId(), entity); break;
      case FLOW_TASK: builder.putServices(entity.getId(), entity); break;
      default: continue;
      }
    }
    this.state = builder.build();
  }

  @Override
  public Uni<StoreEntity> create(CreateStoreEntity newType) {
    throw new RuntimeException("read only store!");
  }

  @Override
  public Uni<StoreEntity> update(UpdateStoreEntity updateType) {
    throw new RuntimeException("read only store!");
  }

  @Override
  public Uni<StoreEntity> delete(DeleteAstType deleteType) {
    throw new RuntimeException("read only store!");
  }
  
  @Override
  public HistoryQuery history() {
    throw new RuntimeException("read only store!");
  }

  @Override
  public QueryBuilder query() {
    return new QueryBuilder() {
      @Override
      public Uni<StoreEntity> get(String id) {
        return Uni.createFrom().item(() -> entities.get(id));
      }
      @Override
      public Uni<StoreState> get() {
        return Uni.createFrom().item(() -> state);
      }
    };
  }

  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private ObjectMapper objectMapper;
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final StoreEntityLocation location = new StoreEntityLocation("classpath*:assets/");
    private final TypeReference<List<AstCommand>> ref = new TypeReference<List<AstCommand>>() {};

    private List<AstCommand> readCommands(String commands) {
      try {
        if(commands.startsWith("{")) {
          final var tree = objectMapper.readTree(commands);
          return objectMapper.convertValue(tree.get("commands"), ref);
        }
        
        return objectMapper.readValue(commands, ref);
      } catch (IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
  
    private List<Resource> list(String location) {
      try {

        LOGGER.debug("Loading assets from: " + location + "!");
        List<Resource> files = new ArrayList<>();
        for (Resource resource : resolver.getResources(location)) {
          files.add(resource);
        }
        return files;
      } catch (Exception e) {
        throw new RuntimeException("Failed to load asset from: " + location + "!" + e.getMessage(), e);
      }
    }
    
    
    private StoreDump readDump(String json) {
      try {
        return objectMapper.readValue(json, StoreDump.class);
      } catch (Exception e) {
        throw new RuntimeException("Failed to load asset from: " + location + "!" + e.getMessage(), e);
      }
    }
    
    private ImmutableStoreEntity.Builder readStoreEntity(Resource resource) {
      final var content = readContents(resource);
      return ImmutableStoreEntity.builder()
          .id(resource.getFilename())
          .hash(Sha2.blob(content))
          .body(readCommands(content));
    }
    
    private String readContents(Resource entry) {
      try {
        return IOUtils.toString(entry.getInputStream(), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new RuntimeException("Failed to load asset content from: " + entry.getFilename() + "!" + e.getMessage(), e);
      }
    }
    public Builder objectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }
    
    public HdesInMemoryStore build() {
      HdesAssert.notNull(objectMapper, () -> "objectMapper must be defined!");
      final var migLog = new StringBuilder();
      final var entities = new HashMap<String, StoreEntity>();
      
      
      list(location.getMigrationRegex()).stream().forEach(r -> {
        final Map<AstBodyType, Integer> order = Map.of(
            AstBodyType.DT, 1,
            AstBodyType.FLOW_TASK, 2,
            AstBodyType.FLOW, 3);
        
        migLog
          .append("Loading assets from: " + r.getFilename()).append(System.lineSeparator())
          .append(" Migrations hash: " + r.getFilename()).append(System.lineSeparator());
        
        final var assets = new ArrayList<>(readDump(readContents(r)).getValue());
        assets.sort((AstSource o1, AstSource o2) -> 
          Integer.compare(order.get(o1.getBodyType()), order.get(o2.getBodyType()))
        );
        
        for(final var asset : assets) {
          migLog.append("  - ")
            .append(asset.getId()).append("/").append(asset.getBodyType()).append("/").append(asset.getHash())
            .append(System.lineSeparator());
        
          final var entity = ImmutableStoreEntity.builder()
              .id(asset.getId())
              .hash(asset.getHash())
              .body(asset.getCommands())
              .bodyType(asset.getBodyType())
              .build();
          entities.put(entity.getId(), entity);
        }
        
      });
      migLog.append(System.lineSeparator());
      
      // Decision tables
      list(location.getDtRegex()).stream().forEach(r -> {
        final var entity = readStoreEntity(r).bodyType(AstBodyType.DT).build();    
        migLog.append("  - ")
          .append(entity.getId()).append("/").append(entity.getBodyType()).append("/").append(entity.getHash())
          .append(System.lineSeparator());
        entities.put(entity.getId(), entity);
        
      });

      // Flow tasks
      list(location.getFlowTaskRegex()).stream().forEach(r -> {
        final var entity = readStoreEntity(r).bodyType(AstBodyType.FLOW_TASK).build();    
        migLog.append("  - ")
          .append(entity.getId()).append("/").append(entity.getBodyType()).append("/").append(entity.getHash())
          .append(System.lineSeparator());
        entities.put(entity.getId(), entity);
      });

      // Flow
      list(location.getFlowRegex()).stream().forEach(r -> {
        final var entity = readStoreEntity(r).bodyType(AstBodyType.FLOW).build();    
        migLog.append("  - ")
          .append(entity.getId()).append("/").append(entity.getBodyType()).append("/").append(entity.getHash())
          .append(System.lineSeparator());
        entities.put(entity.getId(), entity);
      });
      LOGGER.debug(migLog.toString());
      return new HdesInMemoryStore(entities);
    }
  }
}
