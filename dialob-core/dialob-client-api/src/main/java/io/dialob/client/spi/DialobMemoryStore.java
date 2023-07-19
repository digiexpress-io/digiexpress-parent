package io.dialob.client.spi;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.dialob.client.api.DialobDocument.DocumentType;
import io.dialob.client.api.DialobStore;
import io.dialob.client.api.ImmutableStoreEntity;
import io.dialob.client.api.ImmutableStoreState;
import io.dialob.client.spi.composer.ReleaseDumpToStoreEntityVisitor;
import io.dialob.client.spi.store.StoreEntityLocation;
import io.dialob.client.spi.support.DialobAssert;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DialobMemoryStore implements DialobStore {

  private final Map<String, StoreEntity> entities;
  private final StoreState state;
  
  
  public DialobMemoryStore(Map<String, StoreEntity> entities) {
    super();
    this.entities = entities;
    final var builder = ImmutableStoreState.builder();
    for(final var entity : entities.values()) {
      switch (entity.getBodyType()) {
      case FORM_REV: builder.putRevs(entity.getId(), entity); break;
      case FORM: builder.putForms(entity.getId(), entity); break;
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
  public Uni<StoreEntity> delete(DeleteStoreEntity deleteType) {
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
    private String path;
    private final ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    private StoreEntityLocation location;
    private List<Resource> list(String location) {
      try {

        LOGGER.info("Loading assets from: " + location + "!");
        List<Resource> files = new ArrayList<>();
        for (Resource resource : resolver.getResources(location)) {
          files.add(resource);
        }
        return files;
      } catch (Exception e) {
        throw new RuntimeException("Failed to load asset from: " + location + "!" + e.getMessage(), e);
      }
    }
    
    private ImmutableStoreEntity.Builder readStoreEntity(Resource resource) {
      final var content = readContents(resource);
      return ImmutableStoreEntity.builder()
          .id(resource.getFilename())
          .version("static_location")
          .body(content);
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
    public Builder path(String path) {
      this.path = path;
      return this;
    } 

    public DialobMemoryStore build() {
      DialobAssert.notNull(objectMapper, () -> "objectMapper must be defined!");
      this.location = new StoreEntityLocation(path == null ? "classpath*:assets/" : path);
      LOGGER.info("Dialob, starting in memory read-only store from: '" + path + "'");
      
      final var migLog = new StringBuilder();
      final var entities = new HashMap<String, StoreEntity>();
      final var migration = list(location.getMigrationRegex());
      DialobAssert.isTrue(migration.size() < 2, () -> "Only one migration dump can be defined in: '"+ location.getMigrationRegex() + "'!");
      
      migration.stream().forEach(r -> {
        
        migLog.append("Loading assets from migration: " + r.getFilename()).append(System.lineSeparator());
        
        new ReleaseDumpToStoreEntityVisitor(r, objectMapper).visit(entity -> {
          migLog.append("  - ")
          .append(entity.getId()).append("/").append(entity.getBodyType())
          .append(System.lineSeparator());
          entities.put(entity.getId(), entity);  
        });
        
        
      });
      
      migLog.append(System.lineSeparator());

      // form tags
      list(location.getFormTagRegex()).stream().forEach(r -> {
        final var entity = readStoreEntity(r).bodyType(DocumentType.FORM_REV).build();    
        migLog.append("  - ")
          .append(entity.getId()).append("/").append(entity.getBodyType()).append("/")
          .append(System.lineSeparator());
        entities.put(entity.getId(), entity);
      });

      // forms
      list(location.getFormRegex()).stream().forEach(r -> {
        final var entity = readStoreEntity(r).bodyType(DocumentType.FORM).build();    
        migLog.append("  - ")
          .append(entity.getId()).append("/").append(entity.getBodyType()).append("/")
          .append(System.lineSeparator());
        entities.put(entity.getId(), entity);
      });
      LOGGER.info(migLog.toString());
      return new DialobMemoryStore(entities);
    }
  }
  @Override
  public StoreRepoBuilder repo() {
    throw new IllegalArgumentException("not implemented");
  }
  @Override
  public Uni<List<StoreEntity>> batch(List<StoreCommand> batchType) {
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
}
