package io.resys.hdes.client.spi.git;

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

import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.spi.GitConfig;
import io.resys.hdes.client.spi.GitConfig.GitEntry;
import io.resys.hdes.client.spi.GitConfig.GitFile;
import io.resys.hdes.client.spi.ImmutableGitFile;
import io.resys.hdes.client.spi.staticresources.Sha2;
import io.resys.hdes.client.spi.staticresources.StoreEntityLocation;
import io.resys.hdes.client.spi.util.HdesAssert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class GitDataSourceLoader implements AutoCloseable {
  private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
  private final GitConfig conn;
  private final Repository repo;
  private final ObjectId start;
  private final StoreEntityLocation location;

  public GitDataSourceLoader(GitConfig conn) throws IOException {
    super();
    this.repo = conn.getClient().getRepository();
    this.conn = conn;
    this.start = repo.resolve(Constants.HEAD);
    this.location = conn.getLocation();
    
  }

  public List<GitEntry> read() {
    try {      
      conn.getClient().pull().setTransportConfigCallback(conn.getCallback()).call();
    } catch (GitAPIException e) {
      log.error("Can't pull repository! " + System.lineSeparator() + e.getMessage(), e);
      throw new RuntimeException(e.getMessage(), e);
    }
    
    final var load = Arrays.asList(
        Map.entry(AstBodyType.DT, this.location.getDtRegex()),
        Map.entry(AstBodyType.FLOW_TASK, this.location.getFlowTaskRegex()),
        Map.entry(AstBodyType.FLOW, this.location.getFlowRegex()),
        Map.entry(AstBodyType.TAG, this.location.getTagRegex())
    ).parallelStream().map(this::readFile).collect(Collectors.toList());
    
    final var files = GitFiles.builder().git(conn).build();
    final var result = new ArrayList<GitEntry>();
    load.stream().forEach(loaded -> loaded.stream().map(file -> files.readEntry(file, start)).forEach(result::add));
    
    return Collections.unmodifiableList(result);
  }
  
  private List<GitFile> readFile(Map.Entry<AstBodyType, String> src) {
    final AstBodyType bodyType = src.getKey();
    final String location = src.getValue();
    
    if(log.isDebugEnabled()) {
      log.debug("Loading assets from: " + location + "!");
    }

    final var files = new ArrayList<GitFile>();
    try {
      for (final var resource : resolver.getResources("file:" + location)) {
        final var content = getContent(resource);
        final var fileName = resource.getFilename();
        final var id = fileName.substring(0, fileName.indexOf("."));
        
        // src/main/resources/assets/flow/06311dd7-b895-4f94-b43d-6903de74fcf5.json
        final var treeValue = conn.getAssetsPath() +  this.location.getFileName(bodyType, id);
        final var gitFile = ImmutableGitFile.builder()
          .id(id)
          .treeValue(treeValue)
          .blobValue(content)
          .bodyType(bodyType)
          .blobHash(Sha2.blob(content))
          .build();
        files.add(gitFile);
        
        HdesAssert.isTrue(resource.getFile().getAbsolutePath().endsWith(gitFile.getTreeValue()), () -> "Failed to create correct treeValue for: " + fileName);
      }
      
      return files;
    } catch (Exception e) {
      throw new RuntimeException("Failed to load asset from: '" + location + "'!" + System.lineSeparator() + e.getMessage(), e);
    }
  }
  
  private String getContent(Resource entry) {
    try {
      return IOUtils.toString(entry.getInputStream(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load asset content from: " + entry.getFilename() + "!" + e.getMessage(), e);
    }
  }
  
  @Override
  public void close() throws Exception {}
  
  /*
  private static final String TAG_PREFIX = "refs/tags/";
  private List<GitEntry> readTags() {
    try {
      final var result = new ArrayList<GitEntry>();
      for(Ref ref : conn.getClient().tagList().call()) {        
        final String name = ref.getName().startsWith(TAG_PREFIX) ? 
            ref.getName().substring(TAG_PREFIX.length()) : 
            ref.getName();
        
        final var commands = Arrays.asList((AstCommand) ImmutableAstCommand.builder().type(AstCommandValue.SET_BODY).value(name).build());
        final var blobValue = conn.getSerializer().write(commands);
        final var id = ref.getObjectId().getName();
        
        RevWalk revWalk = new RevWalk(repo);
        try {
          final RevCommit commit = revWalk.parseCommit(ref.getObjectId());      
          final var created = new Timestamp(commit.getCommitTime() * 1000L);
          final var entry = ImmutableGitEntry.builder()
              .id(id)
              .revision(id)
              .bodyType(AstBodyType.TAG)
              .treeValue(ref.getName())
              .blobValue(blobValue)
              .created(created)
              .modified(created)
              .blobHash(Sha2.blob(blobValue))
              .commands(commands)
              .build();
          result.add(entry);
        } catch (Exception e) {
          log.error("Can't resolve timestamps for tag: " + name + System.lineSeparator() + e.getMessage(), e);
          throw new RuntimeException(e.getMessage(), e);
        } finally {
          revWalk.close();
        }
      }
      
      return result;
    } catch (GitAPIException e) {
      log.error("Can't read tags for repository! " + System.lineSeparator() + e.getMessage(), e);
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  */
  
}
