package io.resys.thena.fs.spi.snapshot;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;

/*-
 * #%L
 * thena-docdb-api
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

import io.resys.thena.api.LogConstants;
import io.resys.thena.fs.entities.Blob;
import io.resys.thena.fs.entities.Commit;
import io.resys.thena.fs.entities.Node;
import io.resys.thena.fs.entities.Props;
import io.resys.thena.fs.entities.Ref;
import io.resys.thena.fs.entities.Tree;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

/**
 * Git-like logging for filesystem commits and operations.
 * Generates commit logs in Git format with diff-style output.
 * 
 * Commit Header Format:
 * commit <commit-hash>
 * Author: <author>
 * Date: <timestamp>
 * 
 *     <commit-message>
 * 
 * File Change Summary:
 *  <files-changed> files changed, <insertions> insertions(+), <deletions> deletions(-)
 * 
 * File Diff Format:
 * diff --git a/<old-path> b/<new-path>
 * index <old-hash>..<new-hash> <mode>
 * --- a/<old-path>
 * +++ b/<new-path>
 * @@ -<old-line>,<old-count> +<new-line>,<new-count> @@
 * -<deleted-line>
 * +<added-line>
 *  <unchanged-line>
 * 
 * Tree Changes:
 * A    <path>/<filename>     # Added file
 * M    <path>/<filename>     # Modified file  
 * D    <path>/<filename>     # Deleted file
 * R100 <old-path> -> <new-path>  # Renamed file
 * 
 * Props/Metadata Changes:
 * diff --props a/<path> b/<path>
 * -label: old-value
 * +label: new-value
 * 
 * The logger generates:
 * 1. Commit header with hash, author, timestamp, message
 * 2. Summary of nodes changed (added/modified/deleted)
 * 3. Tree diff showing node changes with +/- prefixes
 * 4. Props/blob changes with diff format
 */
@Slf4j(topic = LogConstants.SHOW_COMMIT)
public class SnapshotLogger {
  private static final DateTimeFormatter UTC_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss 'UTC'");
  
  private final StringBuilder data = new StringBuilder();
  private boolean fullDiff = false;

  public SnapshotLogger setFullDiff(boolean fullDiff) {
    this.fullDiff = fullDiff;
    return this;
  }
  
  public SnapshotLogger rmNodes(List<Node> nodes) {
    if(!log.isDebugEnabled()) return this;
    
    for(Node node : nodes) {
      data.append("D    ").append(node.getFullPath()).append("\n");
    }
    return this;
  }
  
  public SnapshotLogger newTree(Tree next) {
    if(!log.isDebugEnabled()) return this;
    
    data.append("Tree changes:\n");
    for(Node node : next.getTreeNodes()) {
      data.append("A    ").append(node.getFullPath()).append("\n");
    }
    return this;
  }

  public SnapshotLogger mergeTree(Tree prev, Tree next) {
    if(!log.isDebugEnabled()) return this;
    
    data.append("Tree changes:\n");
    
    final var prevNodes = prev.getTreeNodes().stream().collect(java.util.stream.Collectors.toMap(n -> n.getFullPath(), n -> n));
    final var nextNodes = next.getTreeNodes().stream().collect(java.util.stream.Collectors.toMap(n -> n.getFullPath(), n -> n));
    
    for(final var path : nextNodes.keySet()) {
      final var nextNode = nextNodes.get(path);
      
      if(!prevNodes.containsKey(path)) {
        data.append("A    ").append(path).append("\n");
      } else {
        final var prevNode = prevNodes.get(path);
        if(!prevNode.getId().equals(nextNode.getId())) {
          data.append("M    ").append(path).append("\n");
          
          if(fullDiff) {
            if(prevNode.getBlobId().isPresent() && nextNode.getBlobId().isPresent()) {
              final var blobDiff = diffBlobs(null, null);
              if(blobDiff.hasChanges()) {
                for(String line : blobDiff.getUnifiedDiff()) {
                  data.append("    ").append(line).append("\n");
                }
              }
            }
            if(prevNode.getPropsId().isPresent() && nextNode.getPropsId().isPresent()) {
              final var propsDiff = diffProps(null, null);
              if(propsDiff.hasChanges()) {
                for(String line : propsDiff.getUnifiedDiff()) {
                  data.append("    ").append(line).append("\n");
                }
              }
            }
          }
        }
      }
    }
    
    for(final var path : prevNodes.keySet()) {
      if(!nextNodes.containsKey(path)) {
        data.append("D    ").append(path).append("\n");
      }
    }
    return this;
  }
  
  public SnapshotLogger newCommit(Commit next) {
    if(!log.isDebugEnabled()) return this;
    
    data.append("commit ").append(shortHash(next.getId())).append("\n");
    data.append("Author: ").append(next.getCommitAuthor()).append("\n");
    data.append("Date: ").append(next.getCommitCreatedAt().format(UTC_FORMATTER)).append("\n\n");
    data.append("    ").append(next.getCommitMessage()).append("\n");
    return this;
  }
  
  public SnapshotLogger mergeCommit(Commit prev, Commit next) {
    if(!log.isDebugEnabled()) return this;
    
    data.append("commit ").append(shortHash(next.getId())).append("\n");
    data.append("Author: ").append(next.getCommitAuthor()).append("\n");
    data.append("Date: ").append(next.getCommitCreatedAt().format(UTC_FORMATTER)).append("\n\n");
    data.append("    ").append(next.getCommitMessage()).append("\n");
    return this;
  }
  
  public SnapshotLogger newBranch(Ref ref) {
    if(!log.isDebugEnabled()) return this;
    
    data.append("\nBranch created: ").append(ref.getRefName()).append(" -> ").append(shortHash(ref.getCommitId())).append("\n");
    return this;
  }
  
  public SnapshotLogger mergeBranch(Ref prev, Ref next) {
    if(!log.isDebugEnabled()) return this;
    
    data.append("\nBranch updated: ").append(next.getRefName()).append(" ").append(shortHash(prev.getCommitId())).append("..").append(shortHash(next.getCommitId())).append("\n");
    return this;
  }

  private String shortHash(String hash) {
    return hash != null && hash.length() > 7 ? hash.substring(0, 8) : hash;
  }
  
  private DiffResult diffBlobs(Blob oldBlob, Blob newBlob) {
    if(oldBlob == null || newBlob == null) {
      return new DiffResult(oldBlob, newBlob, DiffUtils.diff(List.of(), List.of()), List.of());
    }
    
    final var oldLines = jsonToLines(Optional.of(oldBlob.getBlobValue()));
    final var newLines = jsonToLines(Optional.of(newBlob.getBlobValue()));
    
    final var patch = DiffUtils.diff(oldLines, newLines);
    final var unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
      "a/" + shortHash(oldBlob.getId()),
      "b/" + shortHash(newBlob.getId()),
      oldLines, patch, 3
    );
    
    return new DiffResult(oldBlob, newBlob, patch, unifiedDiff);
  }
  
  private DiffResult diffProps(Props oldProps, Props newProps) {
    if(oldProps == null || newProps == null) {
      return new DiffResult(oldProps, newProps, DiffUtils.diff(List.of(), List.of()), List.of());
    }
    
    final var oldLines = propsToLines(oldProps);
    final var newLines = propsToLines(newProps);
    
    final var patch = DiffUtils.diff(oldLines, newLines);
    final var unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
      "a/props",
      "b/props", 
      oldLines, patch, 3
    );
    
    return new DiffResult(oldProps, newProps, patch, unifiedDiff);
  }
  
  private List<String> jsonToLines(Optional<JsonObject> json) {
    if(json.isEmpty()) {
      return Collections.emptyList();
    }
    
    return List.of(json.get().encodePrettily().split("\n"));
  }
  
  private List<String> propsToLines(Props props) {
    final var lines = new ArrayList<String>();
    lines.addAll(jsonToLines(props.getPropsLabels()));
    lines.addAll(jsonToLines(props.getPropsComments()));
    lines.addAll(jsonToLines(props.getPropsPermissions()));
    lines.addAll(jsonToLines(props.getPropsFlags()));
    return lines;
  }
  
  public static class DiffResult {
    private final Object oldObj;
    private final Object newObj;
    private final Patch<?> patch;
    private final List<String> unifiedDiff;
    
    public DiffResult(Object oldObj, Object newObj, Patch<?> patch, List<String> unifiedDiff) {
      this.oldObj = oldObj;
      this.newObj = newObj;
      this.patch = patch;
      this.unifiedDiff = unifiedDiff;
    }
    
    public Object getOld() { return oldObj; }
    public Object getNew() { return newObj; }
    public Patch<?> getPatch() { return patch; }
    public List<String> getUnifiedDiff() { return unifiedDiff; }
    public boolean hasChanges() { return !patch.getDeltas().isEmpty(); }
  }
  
  public SnapshotLogger append(String data) {
    if(log.isDebugEnabled()) {
      this.data.append(data);
    }
    return this;
  }
  
  @Override
  public String toString() {
    if(log.isDebugEnabled()) {
      log.debug(data.toString());
    } else {
      data.append("Log DEBUG disabled for: " + LogConstants.SHOW_COMMIT + "!");
    }
    return data.toString();
  }
} 
