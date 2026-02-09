package io.resys.thena.fs.printer;

/*-
 * #%L
 * thena-fs-client
 * %%
 * Copyright (C) 2015 - 2026 Copyright 2022 ReSys OÜ
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

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ComparisonChain;

import io.resys.thena.fs.tables.FsDb;
import io.resys.thena.jackson.QuarkusJacksonJsonCodec;
import io.vertx.core.json.JsonObject;

public class FileSystemPrinter {

  private final FsDb db;
  private final PrintingMode mode;
  private final Map<String, String> replacements;
  
  public FileSystemPrinter(FsDb db) {
    super();
    this.db = db;
    this.mode = PrintingMode.REAL;
    this.replacements = new HashMap<>();
  }
  
  public FileSystemPrinter(FsDb db,  Map<String, String> replacements) {
    super();
    this.db = db;
    this.mode = PrintingMode.IMAGINARY;
    this.replacements = replacements;
  }

  public static enum PrintingMode {
    /**
     * replaces all id-s(in sequence) and timestamp-s(hardcoded value) with imaginary values
     * this is useful in tests where data needs to be static... ie cant make a test against constantly changing timestamps or identifiers
     */
    IMAGINARY,
    
    /**
     * Print data as is, just order it
     */
    REAL
  }
  
  public String printSync() {
    final var world = db.query().findAll().await().atMost(Duration.ofMinutes(1));
    final boolean isStatic = mode == PrintingMode.IMAGINARY;
    final Map<String, String> wipes = new HashMap<>();
    
    final Function<String, String> ID = (id) -> {
      if (!isStatic) {
        return id;
      }
      if (id == null) {
        return null;
      }
      if (replacements.containsKey(id)) {
        return replacements.get(id);
      }
      final var next = String.valueOf(replacements.size() + 1);
      replacements.put(id, next);
      return next;
    };

    final Function<Object, String> TR = (input) -> {
      if (input == null) {
        return "null";
      }
      if (!isStatic) {
        return JsonObject.mapFrom(input).encode();
      }
      final var id = JsonObject.mapFrom(input).encode();
      if (wipes.containsKey(id)) {
        return wipes.get(id);
      }
      wipes.put(id, "null");
      return "null";
    };

    final Function<OffsetDateTime, String> DATES = (input) -> {
      if (input == null) {
        return null;
      }
      try {
        final var id = QuarkusJacksonJsonCodec.mapper().writeValueAsString(input);
        if (!isStatic) {
          return id;
        }
        if (replacements.containsKey(id)) {
          return replacements.get(id);
        }
        final var next = "\"OffsetDateTime.now()\"";
        replacements.put(id, next);
        return next;
      } catch (Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }
    };

    final var result = new StringBuilder();
    
    result.append(System.lineSeparator()).append("FileSystem World").append(System.lineSeparator());
    
    // Pre-process all entities for ID/date replacement
    for (final var ref : world.getRef().values()) {
      ID.apply(ref.getId());
      ID.apply(ref.getCommitId());
      if (ref.getTransitives() != null) {
        DATES.apply(ref.getTransitives().getCommit().getCommitCreatedAt());
      }
    }
    
    for (final var commit : world.getCommit().values()) {
      ID.apply(commit.getId());
      ID.apply(commit.getTreeId());
      ID.apply(commit.getParentId().orElse(null));
      ID.apply(commit.getMergeId().orElse(null));
      DATES.apply(commit.getCommitCreatedAt());
      if (commit.getTransitives() != null) {
        DATES.apply(commit.getTransitives().getParentCreatedAt().orElse(null));
        DATES.apply(commit.getTransitives().getMergeCreatedAt().orElse(null));
      }
    }
    
    for (final var tree : world.getTree().values()) {
      ID.apply(tree.getId());
      for (final var node : tree.getTreeNodes()) {
        ID.apply(node.getId());
        ID.apply(node.getBlobId().orElse(null));
        ID.apply(node.getPropsId().orElse(null));
        if (node.getTransitives() != null) {
          DATES.apply(node.getTransitives().getCreatedAt());
          DATES.apply(node.getTransitives().getUpdatedAt());
        }
      }
      if (tree.getTransitives() != null) {
        DATES.apply(tree.getTransitives().getCreatedAt());
        DATES.apply(tree.getTransitives().getUpdatedAt());
      }
    }
    
    for (final var blob : world.getBlob().values()) {
      ID.apply(blob.getId());
      if (blob.getTransitives() != null) {
        DATES.apply(blob.getTransitives().getCreatedAt());
        DATES.apply(blob.getTransitives().getUpdatedAt());
      }
    }
    
    for (final var props : world.getProps().values()) {
      ID.apply(props.getId());
      if (props.getTransitives() != null) {
        DATES.apply(props.getTransitives().getCreatedAt());
        DATES.apply(props.getTransitives().getUpdatedAt());
      }
    }
    
    for (final var tag : world.getTag().values()) {
      ID.apply(tag.getId());
      ID.apply(tag.getCommitId());
      DATES.apply(tag.getTagCreatedAt());
      DATES.apply(tag.getTagStartsAt().orElse(null));
      if (tag.getTransitives() != null && tag.getTransitives().getCommit() != null) {
        DATES.apply(tag.getTransitives().getCommit().getCommitCreatedAt());
      }
    }

    // Print References (Branches)
    result.append("References (Branches):").append(System.lineSeparator());
    for (final var ref : world.getRef().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getRefName(), b.getRefName())
            .result())
        .toList()) {
      
      result.append("  - ").append(ref.getRefName())
          .append(" -> ").append(ID.apply(ref.getCommitId()));
      
      if (ref.getBranchDescription().isPresent()) {
        result.append(" (").append(ref.getBranchDescription().get()).append(")");
      }
      if (ref.getBranchAuthor().isPresent()) {
        result.append(" by ").append(ref.getBranchAuthor().get());
      }
      
      result.append(System.lineSeparator());
    }

    // Print Commits
    result.append("Commits:").append(System.lineSeparator());
    for (final var commit : world.getCommit().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getCommitCreatedAt(), b.getCommitCreatedAt())
            .compare(ID.apply(a.getId()), ID.apply(b.getId()))
            .result())
        .toList()) {
      
      result.append("  - ").append(ID.apply(commit.getId()))
          .append(" by ").append(commit.getCommitAuthor())
          .append(" at ").append(DATES.apply(commit.getCommitCreatedAt()))
          .append(System.lineSeparator());
      result.append("    tree: ").append(ID.apply(commit.getTreeId()));
      
      if (commit.getParentId().isPresent()) {
        result.append(", parent: ").append(ID.apply(commit.getParentId().get()));
      }
      if (commit.getMergeId().isPresent()) {
        result.append(", merge: ").append(ID.apply(commit.getMergeId().get()));
      }
      
      result.append(System.lineSeparator());
      result.append("    message: ").append(commit.getCommitMessage()).append(System.lineSeparator());
    }

    // Print Trees
    result.append("Trees:").append(System.lineSeparator());
    for (final var tree : world.getTree().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(ID.apply(a.getId()), ID.apply(b.getId()))
            .result())
        .toList()) {
      
      result.append("  - Tree ").append(ID.apply(tree.getId()))
          .append(" (").append(tree.getTreeNodes().size()).append(" nodes)")
          .append(System.lineSeparator());
      
      for (final var node : tree.getTreeNodes().stream()
          .sorted((a, b) -> ComparisonChain.start()
              .compare(a.getFullPath(), b.getFullPath())
              .result())
          .toList()) {
        
        result.append("    ").append(node.getNodePath()).append("/").append(node.getNodeName());
        
        if (node.getBlobId().isPresent()) {
          result.append(" -> blob:").append(ID.apply(node.getBlobId().get()));
        } else {
          result.append(" (folder)");
        }
        
        if (node.getPropsId().isPresent()) {
          result.append(", props:").append(ID.apply(node.getPropsId().get()));
        }
        
        result.append(System.lineSeparator());
      }
    }

    // Print Blobs
    result.append("Blobs:").append(System.lineSeparator());
    for (final var blob : world.getBlob().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getBlobType(), b.getBlobType())
            .compare(ID.apply(a.getId()), ID.apply(b.getId()))
            .result())
        .toList()) {
      
      result.append("  - ").append(ID.apply(blob.getId()))
          .append(" (").append(blob.getBlobType()).append(")")
          .append(" size: ").append(blob.getBlobValue().encode().length())
          .append(System.lineSeparator());
    }

    // Print Properties
    result.append("Properties:").append(System.lineSeparator());
    for (final var props : world.getProps().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(ID.apply(a.getId()), ID.apply(b.getId()))
            .result())
        .toList()) {
      
      result.append("  - ").append(ID.apply(props.getId())).append(System.lineSeparator());
      result.append("    labels: ").append(TR.apply(props.getPropsLabels())).append(System.lineSeparator());
      result.append("    comments: ").append(TR.apply(props.getPropsComments())).append(System.lineSeparator());
      result.append("    permissions: ").append(TR.apply(props.getPropsPermissions())).append(System.lineSeparator());
      result.append("    flags: ").append(TR.apply(props.getPropsFlags())).append(System.lineSeparator());
    }

    // Print Tags
    result.append("Tags:").append(System.lineSeparator());
    for (final var tag : world.getTag().values().stream()
        .sorted((a, b) -> ComparisonChain.start()
            .compare(a.getTagCreatedAt(), b.getTagCreatedAt())
            .compare(a.getTagName(), b.getTagName())
            .result())
        .toList()) {
      
      result.append("  - ").append(tag.getTagName())
          .append(" -> ").append(ID.apply(tag.getCommitId()))
          .append(" by ").append(tag.getTagAuthor())
          .append(" at ").append(DATES.apply(tag.getTagCreatedAt()))
          .append(System.lineSeparator());
      
      if (tag.getTagDescription().isPresent()) {
        result.append("    description: ").append(tag.getTagDescription().get()).append(System.lineSeparator());
      }
      
      if (tag.getExternalId().isPresent()) {
        result.append("    external: ").append(tag.getExternalId().get()).append(System.lineSeparator());
      }
      
      if (!tag.getTagErrors().isEmpty()) {
        result.append("    errors: ").append(TR.apply(tag.getTagErrors())).append(System.lineSeparator());
      }
    }

    return result.toString();
  }
}
