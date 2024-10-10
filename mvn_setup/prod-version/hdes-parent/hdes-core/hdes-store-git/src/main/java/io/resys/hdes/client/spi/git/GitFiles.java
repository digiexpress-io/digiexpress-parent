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

import io.resys.hdes.client.api.HdesStore.CreateStoreEntity;
import io.resys.hdes.client.api.ast.AstBody.AstBodyType;
import io.resys.hdes.client.api.ast.AstCommand;
import io.resys.hdes.client.api.ast.AstCommand.AstCommandValue;
import io.resys.hdes.client.spi.GitConfig;
import io.resys.hdes.client.spi.GitConfig.GitEntry;
import io.resys.hdes.client.spi.GitConfig.GitFile;
import io.resys.hdes.client.spi.GitConfig.GitFileReload;
import io.resys.hdes.client.spi.ImmutableGitEntry;
import io.resys.hdes.client.spi.ImmutableGitFile;
import io.resys.hdes.client.spi.ImmutableGitFileReload;
import io.resys.hdes.client.spi.staticresources.Sha2;
import io.resys.hdes.client.spi.util.HdesAssert;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GitFiles {
  private static final Logger LOGGER = LoggerFactory.getLogger(GitFiles.class);
  private static final String SEPARATOR = "/";
  private final GitConfig conn;
  
  public GitFiles(GitConfig connection) {
    super();
    this.conn = connection;
  }
  
  public List<GitFileReload> push(List<GitFile> gitFiles) {
    final var git = conn.getClient();
    final var repo = git.getRepository();    
    final var callback = conn.getCallback();
    final var creds = conn.getCreds().get();
    try {
      final var start = repo.resolve(Constants.HEAD);
      
      // pull
      git.pull().setTransportConfigCallback(callback).call().getFetchResult();
      
      // add new files
      var add = git.add();
      for(final var gitFile : gitFiles) {
        add = add.addFilepattern(gitFile.getTreeValue());
      }
      add.call();
      
      // commit changes
      git.commit()
      .setAll(true)
      .setAllowEmpty(false)
      .setMessage("Batch import")
      .setAuthor(creds.getUser(), creds.getEmail())
      .setCommitter(creds.getUser(), creds.getEmail())
      .call();

      // push
      git.push().setTransportConfigCallback(callback).call();
      final var end = repo.resolve(Constants.HEAD);
      
      LOGGER.debug("Pushing changes...");
      return diff(start, end);
      
    } catch(CheckoutConflictException e) {
      LOGGER.error("Conflict, resetting... " +  e.getMessage(), e);
      try {
        git.reset().setMode(ResetType.HARD).call();
        git.pull().setTransportConfigCallback(callback).call();
      } catch(Exception ex) {
        LOGGER.error(e.getMessage(), e);
        throw new RuntimeException(e.getMessage(), e);
      }
      throw new RuntimeException(e.getMessage(), e);
    } catch(EmptyCommitException e) {
      LOGGER.debug("nothing to commit");
    } catch(Exception e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage(), e);
    }
    
    return Collections.emptyList();
  }
  
  public List<GitFileReload> push(GitFile gitFile) {
    final var git = conn.getClient();
    final var repo = git.getRepository();    
    final var callback = conn.getCallback();
    final var creds = conn.getCreds().get();
    try {
      final var start = repo.resolve(Constants.HEAD);
      
      // pull
      git.pull().setTransportConfigCallback(callback).call().getFetchResult();
      
      // add new files
      git.add().addFilepattern(gitFile.getTreeValue()).call();

      // commit changes
      git.commit()
      .setAll(true)
      .setAllowEmpty(false)
      .setMessage("Changes to: " + gitFile.getBodyType() + " file: " + gitFile.getTreeValue())
      .setAuthor(creds.getUser(), creds.getEmail())
      .setCommitter(creds.getUser(), creds.getEmail())
      .call();

      // push
      git.push().setTransportConfigCallback(callback).call();
      final var end = repo.resolve(Constants.HEAD);
      
      LOGGER.debug("Pushing changes...");
      return diff(start, end);
      
    } catch(CheckoutConflictException e) {
      LOGGER.error("Conflict, resetting... " +  e.getMessage(), e);
      try {
        git.reset().setMode(ResetType.HARD).call();
        git.pull().setTransportConfigCallback(callback).call();
      } catch(Exception ex) {
        LOGGER.error(e.getMessage(), e);
        throw new RuntimeException(e.getMessage(), e);
      }
      throw new RuntimeException(e.getMessage(), e);
    } catch(EmptyCommitException e) {
      LOGGER.debug("nothing to commit");
    } catch(Exception e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage(), e);
    }
    
    return Collections.emptyList();
  }
  
  public GitEntry readEntry(GitFile entry) {
    final var git = conn.getClient();
    final var repo = git.getRepository();
    
    try {
      final var start = repo.resolve(Constants.HEAD);
      return readEntry(entry, start);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load timestamps for: " + entry.getTreeValue() + "!" + e.getMessage(), e);
    }
  }
  
  public GitEntry readEntry(GitFile entry, ObjectId start) {

    final var git = conn.getClient();
    final var repo = git.getRepository();
    try(final var revWalk = new RevWalk(repo)) {
      
      final TreeFilter treeFilter = AndTreeFilter.create(
          PathFilterGroup.createFromStrings(entry.getTreeValue()), 
          TreeFilter.ANY_DIFF);

      String revision = start.getName();
      Timestamp created = Timestamp.valueOf(LocalDateTime.MIN);
      Timestamp modified = Timestamp.valueOf(LocalDateTime.MIN);
      try {
        final var commit = revWalk.parseCommit(start);
        
        revWalk.reset();
        revWalk.markStart(commit);
        revWalk.setTreeFilter(treeFilter);
        revWalk.sort(RevSort.COMMIT_TIME_DESC);
        final var modTree = revWalk.next();
        modified = (modTree != null ? new Timestamp(modTree.getCommitTime() * 1000L) : new Timestamp(System.currentTimeMillis()));
        revision = modTree != null ? modTree.getName() : start.getName();
        
        revWalk.reset();
        revWalk.markStart(commit);
        revWalk.setTreeFilter(treeFilter);
        revWalk.sort(RevSort.COMMIT_TIME_DESC);
        revWalk.sort(RevSort.REVERSE, true);
        created = new Timestamp(System.currentTimeMillis());

      } catch(Exception e) {
        LOGGER.error(
            "Failed to create asset from file: '" + entry.getTreeValue() + "'" + System.lineSeparator() +
            "because of: " + e.getMessage() + System.lineSeparator() +
            "with content: " + entry.getBlobValue() 
            , e);
      }
      
      
      final var commands = conn.getSerializer().read(entry.getBlobValue());      
      final var result = ImmutableGitEntry.builder()
          .id(entry.getId())
          .revision(revision)
          .bodyType(entry.getBodyType())
          .treeValue(entry.getTreeValue())
          .blobValue(entry.getBlobValue())
          .created(created)
          .modified(modified)
          .blobHash(Sha2.blob(entry.getBlobValue()))
          .commands(commands)
          .build();

      if(LOGGER.isDebugEnabled()) {
        final var msg = new StringBuilder()
            .append("Loading path: ").append(result.getTreeValue()).append(System.lineSeparator())
            .append("  - blob murmur3_128: ").append(result.getBlobHash()).append(System.lineSeparator())
            .append("  - body type: ").append(result.getBodyType()).append(System.lineSeparator())
            .append("  - created: ").append(result.getCreated()).append(System.lineSeparator())
            .append("  - modified: ").append(result.getModified()).append(System.lineSeparator())
            .append("  - revision: ").append(result.getRevision()).append(System.lineSeparator());
        LOGGER.debug(msg.toString());
      }
      
      return result;
      
    } catch (Exception e) {
      throw new RuntimeException("Failed to load timestamps for: " + entry.getTreeValue() + "!" + System.lineSeparator() + e.getMessage(), e);
    }
  }
  
  public Map.Entry<String, List<GitFileReload>> tag(CreateStoreEntity entity) {
    final var git = conn.getClient();
    final var callback = conn.getCallback();
    final var creds = conn.getCreds().get();
    final var repo = git.getRepository();
    
    try {
      final var start = repo.resolve(Constants.HEAD);
      git.pull().setTransportConfigCallback(callback).call().getFetchResult();
      final var name = entity.getBody().stream().filter(e -> e.getType() == AstCommandValue.SET_TAG_NAME).findFirst().get().getValue();
      
      Ref ref = git.tag().setName(name).setAnnotated(true)
          .setMessage("tag created")
          .setTagger(new PersonIdent(creds.getUser(), creds.getEmail()))
          .call();
      git.push().setPushTags().setTransportConfigCallback(callback).call();
      final var end = repo.resolve(Constants.HEAD);
      
      final var result = new ArrayList<>(diff(start, end));
      result.add(ImmutableGitFileReload.builder().treeValue(ref.getName()).id(name).bodyType(AstBodyType.TAG).build());
      return Map.entry(ref.getObjectId().getName(), result);
    } catch(Exception e) {
      LOGGER.error(e.getMessage(), e);
      throw new RuntimeException(e.getMessage(), e);
    }
  }
  
  private List<GitFileReload> diff(ObjectId oldHead, ObjectId newHead) {
    final var git = conn.getClient();
    final var repo = git.getRepository();
    
    try (final var reader = repo.newObjectReader()) {
      final var result = new ArrayList<GitFileReload>();
      
      final var oldTreeIter = new CanonicalTreeParser();
      oldTreeIter.reset(reader, repo.parseCommit(oldHead).getTree());
      
      final var newTreeIter = new CanonicalTreeParser();
      newTreeIter.reset(reader, repo.parseCommit(newHead).getTree());

      // finally get the list of changed files
      final List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
      for (final DiffEntry entry : diffs) {
        // example: src/main/resources/assets/flow/2d8958a2-44eb-4020-9c17-9bb83daa7434.json

        final var oldId = getId(entry.getOldPath());
        final var id = getId(entry.getNewPath());
        
        if(id.isEmpty()) {
          final var bodyType = getBodyType(entry.getOldPath());
          result.add(ImmutableGitFileReload.builder()
              .id(oldId.get())
              .treeValue(entry.getOldPath())
              .bodyType(bodyType)
              .build());  
        } else {
          final var content = getContent(entry.getNewPath());
          final var bodyType = getBodyType(entry.getNewPath());
          final var treeValue = entry.getNewPath();
          final var gitFile = ImmutableGitFile.builder()
            .id(id.get())
            .treeValue(treeValue)
            .blobValue(content)
            .bodyType(bodyType)
            .blobHash(Sha2.blob(content))
            .build();
          result.add(ImmutableGitFileReload.builder()
              .id(id.get())
              .treeValue(treeValue)
              .file(gitFile)
              .bodyType(bodyType)
              .build());
          
        }        
      }
      return result;
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }    
  }
  
  private Optional<String> getId(String path) {
    if(path.indexOf(".json") < 0) {
      return Optional.empty();
    }

    final var paths = path.split(SEPARATOR);
    final var fileName = paths[paths.length -1];
    final var id = fileName.substring(0, fileName.indexOf("."));
    
    return Optional.of(id);
  }
  
  private AstBodyType getBodyType(String path) {
    if(path.contains(SEPARATOR + "dt" + SEPARATOR)) {
      return AstBodyType.DT;
    } else if(path.contains(SEPARATOR + "flow" + SEPARATOR)) {
      return AstBodyType.FLOW;
    } else if(path.contains(SEPARATOR + "flowtask" + SEPARATOR)) {
      return AstBodyType.FLOW_TASK;
    } else if(path.contains(SEPARATOR + "tag" + SEPARATOR)) {
      return AstBodyType.TAG;
    } else if(path.contains(SEPARATOR + "branch" + SEPARATOR)) {
      return AstBodyType.BRANCH;
    }
    
    throw new RuntimeException("Failed to load asset body type: " + path + "!");
  }
  
  private String getContent(String path) {
    try {
      return IOUtils.toString(new FileInputStream(conn.getAbsolutePath() + SEPARATOR + path), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load asset content from: " + path + "!" + e.getMessage(), e);
    }
  }
  
  
  public GitFile create(AstBodyType bodyType, List<AstCommand> body) throws IOException {
    final var location = conn.getLocation();
    final var id = UUID.randomUUID().toString();
    final var resourceName = location.getAbsolutePath(bodyType, id);
    final var assetName = resourceName.startsWith("file:") ? resourceName.substring(5) : resourceName;
    final var outputFile = new File(assetName);
    
    if(outputFile.exists()) {
      throw new RuntimeException("Can't create asset: '" + assetName + "' because it's already created!");
    } else {
      outputFile.getParentFile().mkdirs();
      boolean created = outputFile.createNewFile();
      HdesAssert.isTrue(created, () -> "Failed to create new file: " + assetName);
    }
    
    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("Created new file: " + outputFile.getCanonicalPath());
    }
    
    final var blob = conn.getSerializer().write(body);
    final var fileOutputStream = new FileOutputStream(outputFile);
    try {
      IOUtils.copy(new ByteArrayInputStream(blob.getBytes(StandardCharsets.UTF_8)), fileOutputStream);
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      fileOutputStream.close();          
    }
    
    return ImmutableGitFile.builder()
        .id(id)
        .treeValue(location.resolveTreeValue(conn.getAssetsPath(), bodyType, id))
        .blobValue(blob)
        .blobHash(Sha2.blob(blob))
        .bodyType(bodyType)
        .build();
  }
  
  public GitFile update(String id, AstBodyType bodyType, List<AstCommand> body) throws IOException {
    final var location = conn.getLocation();
    final var resourceName = location.getAbsolutePath(bodyType, id);
    final var assetName = resourceName.startsWith("file:") ? resourceName.substring(5) : resourceName;
    final var outputFile = new File(assetName);
    
    if(!outputFile.exists()) {
      throw new RuntimeException("Can't update asset: '" + assetName + "' because it does not exist!"); 
    }
     
    final var blob = conn.getSerializer().write(body);
    final var fileOutputStream = new FileOutputStream(outputFile);
    try {
      IOUtils.copy(new ByteArrayInputStream(blob.getBytes(StandardCharsets.UTF_8)), fileOutputStream);
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      fileOutputStream.close();          
    }
    
    return ImmutableGitFile.builder()
        .id(id)
        .treeValue(location.resolveTreeValue(conn.getAssetsPath(), bodyType, id))
        .blobValue(blob)
        .bodyType(bodyType)
        .blobHash(Sha2.blob(blob))
        .build();
  }

  private String getCommitMessage(List<GitEntry> gitFiles) {
    final var branchFile = gitFiles.stream().filter(f -> f.getBodyType().equals(AstBodyType.BRANCH)).findFirst();
    if (gitFiles.size() == 1 && branchFile.isEmpty()) {
      return "Delete: " + gitFiles.get(0).getBodyType() + " file: " + gitFiles.get(0).getTreeValue();
    } else {
      final var branchName = branchFile.get().getCommands().stream().
          filter(c -> c.getType().equals(AstCommand.AstCommandValue.SET_BRANCH_NAME))
          .collect(Collectors.toList())
          .get(0).getValue();
      return "Delete: BRANCH " + branchName + " and its assets";
    }
  }

  public List<GitFileReload> delete(List<GitEntry> gitFiles) throws IOException {
    final Cache<String, GitEntry> cache = conn.getCacheManager().getCache(conn.getCacheName(), String.class, GitEntry.class);
    final var git = conn.getClient();
    final var callback = conn.getCallback();
    final var creds = conn.getCreds().get();
    final var repo = git.getRepository();
    final var start = repo.resolve(Constants.HEAD);
    final var result = new ArrayList<GitFileReload>();

    try {
      for (final var gitFile : gitFiles) {
        if (gitFile.getBodyType() == AstBodyType.TAG) {
          final String filter = "refs/tags/" + gitFile.getCommands().stream()
              .filter(c -> c.getType() == AstCommandValue.SET_TAG_NAME)
              .findFirst().get().getValue();
          final Optional<Ref> target = git.tagList().call().stream()
              .filter(ref -> ref.getName().equals(filter))
              .findFirst();

          if (target.isPresent()) {

            git.tagDelete().setTags(target.get().getName()).call();

            //delete branch 'branchToDelete' on remote 'origin'
            RefSpec refSpec = new RefSpec()
                .setSource(null)
                .setDestination(target.get().getName());
            git.push().setRefSpecs(refSpec).setRemote("origin").setTransportConfigCallback(conn.getCallback()).call();
            result.add(
                ImmutableGitFileReload.builder()
                    .id(gitFile.getId())
                    .bodyType(AstBodyType.TAG)
                    .treeValue(gitFile.getTreeValue()).build());
          } else {
            final String msg = "Can't find tag: " + filter + "!";
            LOGGER.error(msg);
          }
        }

        // pull
        git.pull().setTransportConfigCallback(callback).call().getFetchResult();

        final var treeValue = gitFile.getTreeValue().startsWith("/") ? gitFile.getTreeValue() : "/" + gitFile.getTreeValue();
        final var absolutePath = conn.getAbsolutePath().startsWith("/") ? conn.getAbsolutePath() : "/" + conn.getAbsolutePath();
        final var fullPath = absolutePath + treeValue;

        LOGGER.debug("Removing assets from git: " + fullPath + "");
        final var file = new File(URI.create("file:" + fullPath));

        boolean deleted = file.delete();
        if (!deleted) {
          throw new RuntimeException("Cant delete assets from git: " + fullPath + "");
        }

        // add new files
        git.add().addFilepattern(gitFile.getTreeValue()).call();
      }

      // commit changes
      git.commit()
          .setAll(true)
          .setAllowEmpty(false)
          .setMessage(getCommitMessage(gitFiles))
          .setAuthor(creds.getUser(), creds.getEmail())
          .setCommitter(creds.getUser(), creds.getEmail())
          .call();

      // push
      git.push().setTransportConfigCallback(callback).call();

    } catch (Exception e) {
      LOGGER.error("Failed to delete assets: '" + gitFiles.stream().map(GitEntry::getId).reduce((a, b) -> a + "," + b).get() + "'!" + System.lineSeparator() + e.getMessage(), e);
      try {
        git.reset().setMode(ResetType.HARD).call();
      } catch (GitAPIException e1) {
        throw new RuntimeException(e.getMessage(), e);
      }
    }

    final var end = repo.resolve(Constants.HEAD);
    result.addAll(diff(start, end));

    return result;
  }
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private GitConfig conn;
    
    public Builder git(GitConfig conn) {
      this.conn = conn;
      return this;
    }
    
    public GitFiles build() {
      HdesAssert.notNull(conn, () -> "git connection must be defined!");
      return new GitFiles(conn);
    }
  }
}
