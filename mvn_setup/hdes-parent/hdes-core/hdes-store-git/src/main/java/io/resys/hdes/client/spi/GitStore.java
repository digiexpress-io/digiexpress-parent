package io.resys.hdes.client.spi;

import java.io.IOException;
import java.util.ArrayList;

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

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.ehcache.Cache;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.hdes.client.api.HdesStore;
import io.resys.hdes.client.api.ImmutableStoreEntity;
import io.resys.hdes.client.api.ImmutableStoreState;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.spi.GitConfig.GitEntry;
import io.resys.hdes.client.spi.GitConfig.GitFile;
import io.resys.hdes.client.spi.GitConfig.GitFileReload;
import io.resys.hdes.client.spi.git.GitConnectionFactory;
import io.resys.hdes.client.spi.git.GitDataSourceLoader;
import io.resys.hdes.client.spi.git.GitFiles;
import io.resys.hdes.client.spi.util.HdesAssert;
import io.smallrye.mutiny.Uni;


public class GitStore implements HdesStore {
  private static final Logger LOGGER = LoggerFactory.getLogger(GitStore.class);

  private final GitConfig conn;
  
  interface FileMarker {
    String getAbsolutePath();
  }
  
  @Value.Immutable
  interface BatchEntry {
    GitFile getFile();
    List<AstCommand> getBody();
  }
  
  public GitStore(GitConfig conn) {
    super();
    this.conn = conn;
  }
  @Override
  public String getRepoName() {
    return conn.getInit().getRemote();
  }
  @Override
  public String getHeadName() {
    return conn.getInit().getBranch();
  }
  @Override
  public Uni<List<StoreEntity>> batch(ImportStoreEntity batchType) {
    return Uni.createFrom().item(() -> {
      try {
        final List<GitFile> gitFiles = new ArrayList<>();
        final List<BatchEntry> entries = new ArrayList<>();
        final var git = GitFiles.builder().git(conn).build();
        
        // create
        for(final var create : batchType.getCreate()) {
          final var created = git.create(create.getBodyType(), create.getBody());
          gitFiles.add(created);
          entries.add(ImmutableBatchEntry.builder().file(created).body(create.getBody()).build());
        }
        
        // update 
        for(final var update : batchType.getUpdate()) {
          final var updated = git.update(update.getId(), update.getBodyType(), update.getBody());
          gitFiles.add(updated);
          entries.add(ImmutableBatchEntry.builder().file(updated).body(update.getBody()).build());
        }
        
        final var refresh = git.push(gitFiles);
        cache(refresh);
        
        return entries.stream().map(entry -> (StoreEntity) ImmutableStoreEntity.builder()
              .id(entry.getFile().getId())
              .hash(entry.getFile().getBlobHash())
              .body(entry.getBody())
              .bodyType(entry.getFile().getBodyType())
              .build())
            .collect(Collectors.toList());
      } catch(Exception e) {
        LOGGER.error(new StringBuilder()
            .append("Failed to run batch:").append(System.lineSeparator()) 
            .append("  - because: ").append(e.getMessage()).toString(), e);
        throw new RuntimeException(e.getMessage(), e);
      }      
    });
  }
  @Override
  public Uni<StoreEntity> create(CreateStoreEntity newType) {
    return Uni.createFrom().item(() -> {
      try {
        final var git = GitFiles.builder().git(conn).build();
        if(newType.getBodyType() == AstBodyType.TAG) {
          final var newTag = git.tag(newType);
          if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Hdes git store, new tag created: '" + newTag.getKey() + "'");
          }
        }
        final var file = git.create(newType.getBodyType(), newType.getBody());
        final var refresh = git.push(file);
        cache(refresh);
        return (StoreEntity) ImmutableStoreEntity.builder()
            .id(file.getId())
            .hash(file.getBlobHash())
            .body(newType.getBody())
            .bodyType(newType.getBodyType())
            .build();        
      } catch(Exception e) {
        LOGGER.error(new StringBuilder()
            .append("Failed to create store entity: '").append(newType.getBodyType()).append("'").append(System.lineSeparator())
            .append("  - with commands: ").append(conn.getSerializer().write(newType.getBody())).append(System.lineSeparator()) 
            .append("  - because: ").append(e.getMessage()).toString(), e);
        throw new RuntimeException(e.getMessage(), e);
      }
    });
  }
  @Override
  public Uni<StoreEntity> update(UpdateStoreEntity updateType) {
    return query().get(updateType.getId()).onItem().transform((oldState) -> {
      try {
        HdesAssert.isTrue(oldState.getBodyType() != AstBodyType.TAG, () -> "Tags can't be updated!" );
        final var git = GitFiles.builder().git(conn).build();
        final var file = git.update(updateType.getId(), oldState.getBodyType(), updateType.getBody());
        final var refresh = git.push(file);
        cache(refresh);
        return (StoreEntity) ImmutableStoreEntity.builder()
            .id(file.getId())
            .hash(file.getBlobHash())
            .body(updateType.getBody()).bodyType(oldState.getBodyType())
            .build();        
      } catch(Exception e) {
        LOGGER.error(new StringBuilder()
            .append("Failed to update store entity: '").append(oldState.getBodyType()).append("'").append(System.lineSeparator())
            .append("  - with commands: ").append(conn.getSerializer().write(updateType.getBody())).append(System.lineSeparator()) 
            .append("  - because: ").append(e.getMessage()).toString(), e);
        throw new RuntimeException(e.getMessage(), e);
      }
    });
  }
  @Override
  public Uni<StoreEntity> delete(DeleteAstType deleteType) {
    return Uni.createFrom().item(() -> {
      final Cache<String, GitEntry> cache = conn.getCacheManager().getCache(conn.getCacheName(), String.class, GitEntry.class);
      final GitEntry gitFile = cache.get(deleteType.getId());
      try {
        final var git = GitFiles.builder().git(conn).build();
        final var refresh = git.delete(deleteType.getId());
        cache(refresh);
        return (StoreEntity) ImmutableStoreEntity.builder().id(deleteType.getId())
            .body(gitFile.getCommands())
            .bodyType(gitFile.getBodyType())
            .hash(gitFile.getBlobHash())
            .build();        
      } catch(Exception e) {
        LOGGER.error(new StringBuilder()
            .append("Failed to delete store entity: '").append(gitFile.getBodyType()).append("'").append(System.lineSeparator())
            .append("  - with commands: ").append(gitFile.getBlobValue()).append(System.lineSeparator()) 
            .append("  - because: ").append(e.getMessage()).toString(), e);
        throw new RuntimeException(e.getMessage(), e);
      }
    });
  }
  
  private void cache(List<GitFileReload> reloads) {
    final var files = GitFiles.builder().git(conn).build();
    final ObjectId head;
    try {
      final var git = conn.getClient();
      final var repo = git.getRepository();
      head = repo.resolve(Constants.HEAD);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load head!" + System.lineSeparator() + e.getMessage(), e);
    }
    
    final var cache = conn.getCacheManager().getCache(conn.getCacheName(), String.class, GitEntry.class);
    for(final var reload : reloads) {
      if(reload.getFile().isEmpty()) {
        cache.remove(reload.getId());  
      } else {
        final var entry = files.readEntry(reload.getFile().get(), head);
        cache.put(entry.getId(), entry);  
      }
    }
  }
  
//  
//  createStoreEntity(CreateStoreEntity newType) {
//    try {
//      final var id = UUID.randomUUID().toString();
//      final var resourceName = location.getAbsolutePath(newType.getBodyType(), id);
//      final var assetName = resourceName.startsWith("file:") ? resourceName.substring(5) : resourceName;
//      final var outputFile = new File(assetName);
//      
//      if(outputFile.exists()) {
//        throw new RuntimeException("Can't create asset: '" + assetName + "' because it's already created!");
//      } else {
//        outputFile.getParentFile().mkdirs();
//        boolean created = outputFile.createNewFile();
//        HdesAssert.isTrue(created, () -> "Failed to create new file: " + assetName);
//      }
//      
//      if(LOGGER.isDebugEnabled()) {
//        LOGGER.debug("Created new file: " + outputFile.getCanonicalPath());
//      }
//  
//      // copy data to file
//      final StoreEntity src = ImmutableStoreEntity.builder()
//          .id(id)
//          .body(newType.getBody())
//          .bodyType(newType.getBodyType())
//          .build();
//      
//      
//      final var blob = objectMapper.writeValueAsString(src);
//      final var fileOutputStream = new FileOutputStream(outputFile);
//      try {
//        IOUtils.copy(new ByteArrayInputStream(blob.getBytes(StandardCharsets.UTF_8)), fileOutputStream);
//      } catch(Exception e) {
//        throw new RuntimeException(e.getMessage(), e);
//      } finally {
//        fileOutputStream.close();          
//      }
//      
//      final var created = Timestamp.valueOf(LocalDateTime.now());
//      final var gitEntry = ImmutableGitEntry.builder()
//        .id(id)
//        .modified(created)
//        .created(created)
//        .revision("")
//        .bodyType(newType.getBodyType())
//        .treeValue(conn.getAssetsPath() + outputFile.getName())
//        .blobValue(blob)
//        .commands(newType.getBody())
//        .build();
//      
//      push(gitEntry);
//      return src;
//    } catch(Exception e) {
//      LOGGER.error("Failed to create new asset because:" + e.getMessage(), e);
//      throw new RuntimeException(e.getMessage(), e);
//    }
//  }
  
  @Override
  public QueryBuilder query() {
    return new QueryBuilder() {
      private StoreEntity map(GitEntry entry) {
        return ImmutableStoreEntity.builder()
            .id(entry.getId())
            .bodyType(entry.getBodyType())
            .body(entry.getCommands())
            .hash(entry.getBlobHash())
            .build();
      }
      @Override
      public Uni<StoreEntity> get(String id) {
        return Uni.createFrom().item(() -> {
          final var cache = conn.getCacheManager().getCache(conn.getCacheName(), String.class, GitEntry.class);
          final var entity = cache.get(id);
          HdesAssert.notFound(entity, () -> "Entity was not found by id: '"  + id + "'!");
          return map(entity);
        });
      }
      
      @Override
      public Uni<StoreState> get() {
        return Uni.createFrom().item(() -> {
          final var state = ImmutableStoreState.builder();
          final var cache = conn.getCacheManager().getCache(conn.getCacheName(), String.class, GitEntry.class);
          final var iterator = cache.iterator();
          while(iterator.hasNext()) {
            final var entry = iterator.next();
            final var mapped = map(entry.getValue());
            switch (mapped.getBodyType()) {
            case FLOW: state.putFlows(entry.getKey(), map(entry.getValue())); break;
            case FLOW_TASK: state.putServices(entry.getKey(), map(entry.getValue())); break;
            case DT: state.putDecisions(entry.getKey(), map(entry.getValue())); break;
            case TAG: state.putTags(entry.getKey(), map(entry.getValue())); break;
            default: throw new RuntimeException("Unknown body type: '" + mapped.getBodyType() + "'!");
            }
          }
          return state.build();
        });
      }
    };
  }
  
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private String remote;
    private String branch;
    private String storage;
    private String sshPath;
    private ObjectMapper objectMapper;
    private HdesCredsSupplier creds;
    
    public Builder remote(String remote) {
      this.remote = remote;
      return this;
    }
    public Builder branch(String branch) {
      this.branch = branch;
      return this;
    }
    public Builder storage(String storage) {
      this.storage = storage;
      return this;
    }
    public Builder sshPath(String sshPath) {
      this.sshPath = sshPath;
      return this;
    }
    public Builder objectMapper(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
      return this;
    }
    public Builder creds(HdesCredsSupplier creds) {
      this.creds = creds;
      return this;
    }
    
    public GitStore build() {
      HdesAssert.notNull(objectMapper, () -> "objectMapper must be defined!");
      HdesAssert.notNull(creds, () -> "creds must be defined!");
      
      if(LOGGER.isDebugEnabled()) {
        final var log = new StringBuilder()
            .append(System.lineSeparator())
            .append("Configuring GIT: ").append(System.lineSeparator())
            .append("  remote: '").append(remote).append("'").append(System.lineSeparator())
            .append("  branch: '").append(branch).append("'").append(System.lineSeparator())
            .append("  storage: '").append(storage).append("'").append(System.lineSeparator())
            .append("  sshPath: '").append(sshPath).append("'").append(System.lineSeparator());
        LOGGER.debug(log.toString());
      }

      HdesAssert.notEmpty(remote, () -> "remote must be defined! Example of the remote: 'ssh://git@git.resys.io:22222/wrench/wrench-demo-assets.git'");
      HdesAssert.notEmpty(branch, () -> "branch must be defined! Example of the branch: 'main'");
      HdesAssert.notEmpty(storage, () -> "storage must be defined! Storage is the path to what to clone the repository, example: '/home/olev/Development/wrench-demo-assets'");
      HdesAssert.notEmpty(sshPath, () -> "sshPath must be defined! SshPath must contain 2 files(classpath or folder), private key and known host: 'id_rsa', 'id_rsa.known_hosts'. Classpath example: 'classpath:ssh/id_rsa'");
      
      final var init = ImmutableGitInit.builder().branch(branch).remote(remote).storage(storage).sshPath(sshPath).build();
      final GitConfig conn;
      try {
        conn = GitConnectionFactory.create(init, creds, objectMapper);
      } catch(Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
      

      if(LOGGER.isDebugEnabled()) {
        final var log = new StringBuilder()
            .append(System.lineSeparator())
            .append("Configured GIT: ").append(System.lineSeparator())
            .append("  cloned into directory: '").append(conn.getParentPath()).append("'").append(System.lineSeparator())
            .append("  working directory: '").append(conn.getAbsolutePath()).append("'").append(System.lineSeparator())
            .append("  assets directory: '").append(conn.getAbsoluteAssetsPath()).append("'").append(System.lineSeparator())
            .append("  relative assets directory: '").append(conn.getAssetsPath()).append("'").append(System.lineSeparator())
            .append("  cache name: '").append(conn.getCacheName()).append("'").append(System.lineSeparator())
            .append("  cache size/heap: '").append(conn.getCacheHeap()).append("'").append(System.lineSeparator());
        LOGGER.debug(log.toString());
      }

      // load assets
      final var cache = conn.getCacheManager().getCache(conn.getCacheName(), String.class, GitEntry.class);
      try(final var loader = new GitDataSourceLoader(conn)) {
        loader.read().forEach(e -> cache.put(e.getId(), e));
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      };
      
      return new GitStore(conn);
    }
  }

  @Override
  public HistoryQuery history() {
    throw new RuntimeException("not implemented");
  }
  @Override
  public StoreRepoBuilder repo() {
    throw new RuntimeException("not implemented");
  }
}
