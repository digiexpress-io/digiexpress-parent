package io.resys.hdes.client.spi;

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

import java.nio.file.Path;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.ehcache.CacheManager;
import org.immutables.value.Value;

import io.resys.hdes.client.api.HdesStore.HdesCredsSupplier;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.spi.staticresources.StoreEntityLocation;

@Value.Immutable
public interface GitConfig {
  GitInit getInit();
  
  StoreEntityLocation getLocation();
  GitSerializer getSerializer();
  
  HdesCredsSupplier getCreds();
  CacheManager getCacheManager();
  String getCacheName();
  Integer getCacheHeap();
  
  String getAssetsPath();          // relative path starts from repository root
  Path getParentPath();           // path where git repository is cloned
  String getAbsolutePath();       // git working directory path
  String getAbsoluteAssetsPath(); // absolute path for assets in the git working directory 
  TransportConfigCallback getCallback();
  Git getClient();
  
  @Value.Immutable
  interface GitInit {
    String getBranch();
    String getRemote();
    String getSshPath();
    String getStorage();
  }

  @Value.Immutable
  interface GitEntry {
    String getId();
    Timestamp getCreated();
    Timestamp getModified();
    AstBodyType getBodyType();
    String getRevision();
    String getBlobHash();
    String getTreeValue();
    String getBlobValue();
    List<AstCommand> getCommands();
  }
  
  interface GitSerializer {
    List<AstCommand> read(String commands);
    String write(List<AstCommand> commands);
  }

 
  @Value.Immutable
  interface GitFileReload {
    String getTreeValue();
    String getId();
    AstBodyType getBodyType();
    Optional<GitFile> getFile();
  }
  
  @Value.Immutable
  interface GitFile {
    String getId();
    String getTreeValue();
    String getBlobValue();
    String getBlobHash();
    AstBodyType getBodyType();
  }
  
}
