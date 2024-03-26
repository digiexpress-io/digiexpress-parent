package io.resys.thena.storefile.spi;

/*-
 * #%L
 * thena-docdb-api
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.resys.thena.api.LogConstants;
import io.resys.thena.spi.DbCollections;
import io.resys.thena.storefile.tables.RepoTable;
import io.resys.thena.storefile.tables.Table;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = LogConstants.SHOW_SQL)
public class FileConnection implements Table.Connection {
  private final File rootDir;
  private final ObjectMapper objectMapper;
  private final Map<String, RepoTable> cache = new HashMap<>();
  public FileConnection(File rootDir, ObjectMapper objectMapper) {
    super();
    this.rootDir = rootDir;
    this.objectMapper = objectMapper;
  }

  @Override
  public RepoTable getRepoTable(DbCollections ctx) {
    
    if(cache.containsKey(ctx.getDb())) {
      final var cached = cache.get(ctx.getDb());
      final var cachedCtx = cached.getContext();
      
      // same ctx
      if(cachedCtx.getBlobs().equals(ctx.getBlobs())) {
        return cached;
      }
      
      final var ctxKey = ctx.getDb() + "/" + ctx.getBlobs();
      final var cachedBlobCtx = cache.get(ctxKey);
      if(cachedBlobCtx == null) {
        final var repo = cached.withContext(ctx);
        cache.put(ctxKey, repo);
        return repo;
      }
      
      return cachedBlobCtx;
    }
    
    final var db = new File(rootDir, ctx.getDb());
    log.info("Started local file pool in directory: " + db.getAbsolutePath());
    if(!db.exists()) {
      db.mkdir();
    }
    final var repo = new RepoTableImpl(db, ctx, objectMapper);
    cache.put(ctx.getDb(), repo);
    return repo;
  }

  @Slf4j(topic = LogConstants.SHOW_SQL)
  public static abstract class FileTable<T extends Table.Row> {
    private final Class<T> type;
    protected final File db;
    private final File asset;
    protected final ObjectMapper objectMapper;
    private final String assetName;
    private final TypeReference<List<T>> ref;
    private boolean cache_created;
    private List<T> cache_rows;
    
    public FileTable(File db, String asset, ObjectMapper objectMapper, 
        Class<T> type, TypeReference<List<T>> ref) {
      
      super();
      this.ref = ref;
      this.db = db;
      this.type = type;
      this.assetName = asset;
      this.asset = new File(db, asset); 
      this.cache_created = this.asset.exists();
      this.objectMapper = objectMapper;
      log.info("Started local table: " + asset + ", in: " + this.asset.getAbsolutePath());
    }
    
    public String getTableName() {
      return assetName;
    }
    
    public T update(T oldState, T newState) {
      final var criteria = writeJson(oldState);
      final var oldRow = getRows().stream()
        .filter(s -> writeJson(s).equals(criteria))
        .findFirst();
      if(oldRow.isEmpty()) {
        throw new IllegalArgumentException("can't find old state: " + criteria);
      }

      try {
        final var nextState = new ArrayList<>(getRows());
        nextState.remove(oldRow.get());
        nextState.add(newState);
        write(objectMapper.writeValueAsString(nextState));
        

        
        this.cache_rows = nextState;
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
      
      return newState;
    }
    
    public T delete(T entry) {
      final var criteria = writeJson(entry);
      final var old = getRows().stream()
        .filter(s -> this.writeJson(s).equals(criteria))
        .findFirst();
      if(old.isEmpty()) {
        throw new IllegalArgumentException("can't find old state: " + criteria);
      }
      
      try {
        final var nextState = new ArrayList<>(getRows());
        nextState.remove(old.get());
        write(objectMapper.writeValueAsString(nextState));
        
        this.cache_rows = nextState;
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
      
      return entry;
    }

    public List<T> getRows() {
      if(cache_rows == null) {
        cache_rows = this.read(this.type);
      }
      return Collections.unmodifiableList(cache_rows);
    }
    
    public boolean create() {
      try {
        if(this.cache_created) {
          final var isEmpty = read().trim().isEmpty();
          if(isEmpty) {
            write(objectMapper.writeValueAsString(new ArrayList<>()));
          }
          return false;
        }
        
        write(objectMapper.writeValueAsString(new ArrayList<>()));
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
      return true;
    }
    
    public Boolean getExists() {
      return asset.exists();
    }
    
    public List<T> insertAll(List<T> entry) {

      try {
        log.debug("Writing into local table: {}", asset.getAbsolutePath());
        final var next = new ArrayList<>(getRows());
        next.addAll(entry);
        write(objectMapper.writeValueAsString(next));
        
        this.cache_rows = next;
        return entry;
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    
    public T insert(T type) {
      try {
        log.debug("Writing into local table: {}", asset.getAbsolutePath());
        final var next = new ArrayList<>(getRows());
        next.add(type);
        write(objectMapper.writeValueAsString(next));
        
        this.cache_rows = next;
        return type;
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    
    private List<T> read(Class<T> type) {
      if(!asset.exists()) {
        return Collections.emptyList();
      }
      try {
        final var jsonString = read();
        return objectMapper.readValue(jsonString, ref);
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }

    protected String writeJson(T type) {
      try {
        return objectMapper.writeValueAsString(type);
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }
    
    private String read() {
      FileInputStream out = null;
      try {
        out = new FileInputStream(asset);
        return new String(out.readAllBytes(), StandardCharsets.UTF_8);
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      } finally {
        try {
          if(out != null) {
            out.close();
          }
        } catch(IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    }
    
    private String write(String content) {
      FileOutputStream out = null;
      try {
        out = new FileOutputStream(asset);
        out.write(content.getBytes(StandardCharsets.UTF_8));
        return content;
      } catch(IOException e) {
        throw new RuntimeException(e.getMessage(), e);
      } finally {
        try {
          if(out != null) {
            out.close();
          }
        } catch(IOException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      }
    }
  }
}
