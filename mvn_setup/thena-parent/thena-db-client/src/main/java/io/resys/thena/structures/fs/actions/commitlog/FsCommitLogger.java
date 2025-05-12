package io.resys.thena.structures.fs.actions.commitlog;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import io.resys.thena.api.entities.fs.FsCommit;
import io.resys.thena.api.entities.fs.FsDirentAssignment;
import io.resys.thena.api.entities.fs.FsDirentLink;
import io.resys.thena.api.entities.fs.FsDirentRemark;
import io.resys.thena.api.entities.fs.ImmutableFsDirentLink;
import io.resys.thena.api.entities.fs.ImmutableFsDirentRemark;
import io.resys.thena.api.entities.fs.ThenaFsObject.IsFsObject;
import io.vertx.core.json.JsonObject;



public class FsCommitLogger {
  private final String tenantId;
  private final String commitId;
  private final FsCommit commit;
  public static List<String> SKIP = Arrays.asList(
      "commitId",
      "createdWithCommitId");
  
  
  private int count_added;
  private int count_deleted;
  private int count_merged;
  private final StringBuilder added = new StringBuilder();
  private final StringBuilder merged = new StringBuilder();
  private final List<IsFsObject> removed = new ArrayList<>();
    

  public FsCommitLogger(String tenantId, FsCommit commit) {
    super();
    this.tenantId = tenantId;
    this.commitId = commit.getCommitId();
    this.commit = commit;
  }

  public void add(IsFsObject entity) {    
    count_added++;
    added
      .append("  + ").append(entity.getId()).append("::").append(entity.getDocType()).append(System.lineSeparator())
      .append("    ").append(toJson(entity)).append(System.lineSeparator());
  }
  public void remove(IsFsObject entity) {
    count_deleted++;
    removed.add(entity);
  }
  
  public void merge(IsFsObject previous, IsFsObject next) {

    final var a = toJson(previous);
    final var b = toJson(next);    
    final var diff = new StringBuilder();
    
    for(final var entries : a.getMap().entrySet()) {
      final var changedFrom = entries.getValue();
      final var changedTo = b.getValue(entries.getKey());
      
      if(SKIP.contains(entries.getKey()) ) {
        continue;
      }
      
      if(Objects.equal(changedFrom, changedTo)) {
        continue;
      }
      diff.append("   diff: ").append(entries.getKey())
        .append(" :: ")
        .append(changedFrom).append(" -> ").append(changedTo)
        .append(System.lineSeparator());
        
    }
    
    if(diff.isEmpty()) {
      return;
    }
    
    merged
      .append("  +- ").append(next.getId()).append("::").append(next.getDocType()).append(System.lineSeparator())
      .append("   -  ").append(a).append(System.lineSeparator())
      .append("   +  ").append(b).append(System.lineSeparator())
      .append(diff);
    count_merged++;
  }
  
  private JsonObject toJson(IsFsObject entity) {
    
    if(entity instanceof FsDirentLink) {
      final var link = ImmutableFsDirentLink.builder().from((FsDirentLink) entity)
          .transitives(null)
          .build();
      return JsonObject.mapFrom(link);
    } else if(entity instanceof FsDirentRemark) {
      final var link = ImmutableFsDirentRemark.builder().from((FsDirentRemark) entity)
          .transitives(null)
          .build();
      return JsonObject.mapFrom(link);
    }
    
    return JsonObject.mapFrom(entity);
  }
  
  @SuppressWarnings({ "incomplete-switch" }) 
  private int compare(IsFsObject a, IsFsObject b) {
    if(a.getDocType() != b.getDocType()) {
      return a.getDocType().compareTo(b.getDocType());
    }
    
    switch (a.getDocType()) {
    case FS_DIRENT_ASSIGNMENT: {
      final FsDirentAssignment a1 = (FsDirentAssignment) a;
      final FsDirentAssignment a2 = (FsDirentAssignment) b;
      return ComparisonChain.start()
        .compare(a1.getAssignmentType(), a2.getAssignmentType())
        .compare(a1.getAssignee(), a2.getAssignee())
        .result();
    }      
    case FS_DIRENT_LINKS: {
      final FsDirentLink a1 = (FsDirentLink) a;
      final FsDirentLink a2 = (FsDirentLink) b;
      return ComparisonChain.start()
        .compare(a1.getLinkType(), a2.getLinkType())
        .compare(a1.getLinkValue() + "", a2.getLinkValue() + "")
        .result();
    }
    }
    
    return 0;
  }
  
  private String removed() {
    final var result = new StringBuilder();
    this.removed.stream()
    .sorted(this::compare)
    .forEach(entity -> {
      result
      .append("  - ").append(entity.getId()).append("::").append(entity.getDocType()).append(System.lineSeparator())
      .append("    ").append(toJson(entity)).append(System.lineSeparator());
    });;
    
    return result.toString();
  }
 
  public String build() {
    return new StringBuilder()
      .append("commit: ").append(commitId).append(", tenant: ").append(tenantId).append(System.lineSeparator())
      .append("author: ").append(commit.getCommitAuthor()).append(", message: ").append(commit.getCommitMessage())
      .append(System.lineSeparator())
      .append(" | created")
      .append(System.lineSeparator())
      .append("  + added new: ").append(count_added).append(" entries")
      .append(System.lineSeparator())
      .append(added)
      
      .append(System.lineSeparator())
      .append(" | deleted")
      .append(System.lineSeparator())
      .append("  - deleted: ").append(count_deleted).append(" entries")
      .append(System.lineSeparator())
      .append(removed())
      
      .append(System.lineSeparator())
      .append(" | merged")
      .append(System.lineSeparator())
      .append("  +- merged: ").append(count_merged).append(" entries")
      .append(System.lineSeparator())
      .append(merged)
      .toString();


  }
}
