package io.resys.hdes.client.spi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesComposer.StoreDump;
import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableBranch;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ImmutableStoreState;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstBody.AstSource;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstTag;
import io.resys.hdes.client.api.ast.AstTag.AstTagValue;
import io.resys.hdes.client.spi.staticresources.Sha2;
import io.resys.hdes.client.spi.staticresources.StoreEntityLocation;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.smallrye.mutiny.Uni;

public class HdesInMemoryStore implements HdesStore {
  private static final Logger LOGGER = LoggerFactory.getLogger(HdesInMemoryStore.class);
  private final Map<String, StoreEntity> entities;
  private final StoreState state;
  private final Optional<String> branchName;
  
  
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
    this.state = builder.tagName("dev").build();
    this.branchName = Optional.empty();
  }

  public HdesInMemoryStore(Map<String, StoreEntity> entities, String branchName) {
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
    this.state = builder.tagName(branchName).build();
    this.branchName = Optional.ofNullable(branchName);
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
  public Uni<List<StoreEntity>> delete(DeleteAstType deleteType) {
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
    private StoreEntityLocation location = new StoreEntityLocation("classpath*:assets/");
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private final TypeReference<List<AstCommand>> ref = new TypeReference<List<AstCommand>>() {};

    
    public Builder location(StoreEntityLocation location) {
      this.location = location;
      return this;
    }
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

    private AstTag readRelease(String json) {
      try {
        return objectMapper.readValue(json, AstTag.class);
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
          .append("Loading assets from release: " + r.getFilename()).append(System.lineSeparator());
        
        final var assets = new ArrayList<>(readRelease(readContents(r)).getValues());
        assets.sort((AstTagValue o1, AstTagValue o2) -> 
          Integer.compare(order.get(o1.getBodyType()), order.get(o2.getBodyType()))
        );
        for(final var asset : assets) {
          final var id = asset.getId() == null ? UUID.randomUUID().toString() : asset.getId();
          migLog.append("  - ")
            .append(asset.getId()).append("/").append(asset.getBodyType()).append("/").append(asset.getHash())
            .append(System.lineSeparator());
          final var entity = ImmutableStoreEntity.builder()
              .id(id)
              .hash(asset.getHash())
              .body(asset.getCommands())
              .bodyType(asset.getBodyType())
              .build();
          entities.put(id, entity);
        }
      });
      

      list(location.getDumpRegex()).stream().forEach(r -> {
        
        
        final Map<AstBodyType, Integer> order = Map.of(
            AstBodyType.DT, 1,
            AstBodyType.FLOW_TASK, 2,
            AstBodyType.FLOW, 3);
        migLog
          .append("Loading assets from dump: " + r.getFilename()).append(System.lineSeparator());
        
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
  
    
    public HdesInMemoryStore build(String json) {
      return build(readRelease(json));
    }
    
    public HdesInMemoryStore build(AstTag json) {
      final var migLog = new StringBuilder();
      final var entities = new HashMap<String, StoreEntity>();
      final Map<AstBodyType, Integer> order = Map.of(
          AstBodyType.DT, 1,
          AstBodyType.FLOW_TASK, 2,
          AstBodyType.FLOW, 3);
      migLog
        .append("Loading assets from release").append(System.lineSeparator());
      
      final var assets = new ArrayList<>(json.getValues());
      assets.sort((AstTagValue o1, AstTagValue o2) -> 
        Integer.compare(order.get(o1.getBodyType()), order.get(o2.getBodyType()))
      );
      for(final var asset : assets) {
        migLog.append("  - ")
          .append(asset.getId()).append("/").append(asset.getBodyType()).append("/").append(asset.getHash())
          .append(System.lineSeparator());
      
        final var id = asset.getId() == null ? UUID.randomUUID().toString() : asset.getId();
        final var entity = ImmutableStoreEntity.builder()
            .id(id)
            .hash(asset.getHash())
            .body(asset.getCommands())
            .bodyType(asset.getBodyType())
            .build();
        entities.put(id, entity);
      }
      
      LOGGER.debug(migLog.toString());
      return new HdesInMemoryStore(entities);
    }
  }
  
  @Override
  public HistoryQuery history() {
    throw new IllegalArgumentException("not implemented");
  }
  @Override
  public StoreRepoBuilder repo() {
    throw new IllegalArgumentException("not implemented");
  }

  @Override
  public Optional<String> getBranchName() {
    return branchName;
  }

  @Override
  public HdesStore withBranch(String branchName) {
    Objects.requireNonNull(branchName, () -> "branchName can't be null!");
    return new HdesInMemoryStore(entities, branchName);
  }

  @Override
  public Uni<List<StoreEntity>> batch(ImportStoreEntity batchType) {
    throw new IllegalArgumentException("not implemented");
  }
  @Override
  public String getRepoName() {
    return "in-memory";
  }
  @Override
  public String getHeadName() {
    return "in-memory";
  }
  @Override
  public BranchQuery queryBranches() {
    return new BranchQuery() {
      
      @Override
      public Uni<List<Branch>> findAll() {
        return Uni.createFrom().item(Arrays.asList(ImmutableBranch.builder().name(getHeadName()).commitId("in-memory").build()));
      }
    };
  }
}
