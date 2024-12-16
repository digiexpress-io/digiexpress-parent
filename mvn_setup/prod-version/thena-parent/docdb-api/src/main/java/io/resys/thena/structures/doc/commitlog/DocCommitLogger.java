package io.resys.thena.structures.doc.commitlog;

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
import java.util.List;

import com.google.common.base.Objects;

import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.vertx.core.json.JsonObject;



public class DocCommitLogger {
  private final String tenantId;
  private final String commitId;
  private final DocCommit commit;

  private int count_added;
  private int count_deleted;
  private int count_merged;
  private final StringBuilder added = new StringBuilder();
  private final StringBuilder merged = new StringBuilder();
  private final List<IsDocObject> removed = new ArrayList<>();
  
  public DocCommitLogger(String tenantId, DocCommit commit) {
    super();
    this.tenantId = tenantId;
    this.commitId = commit.getId();
    this.commit = commit;
  }

  public void add(IsDocObject entity) {
    count_added++;
    added
      .append("  + ").append(entity.getId()).append("::").append(entity.getDocType()).append(System.lineSeparator())
      .append("    ").append(toJson(entity)).append(System.lineSeparator());
  }
  
  private JsonObject toJson(IsDocObject entity) {
    return JsonObject.mapFrom(entity);
  }
  
  public void remove(IsDocObject entity) {
    count_deleted++;
    removed.add(entity);
  }
  
  private int compare(IsDocObject a, IsDocObject b) {
    if(a.getDocType() != b.getDocType()) {
      return a.getDocType().compareTo(b.getDocType());
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
  
  public void merge(IsDocObject previous, IsDocObject next) {
    count_merged++;
    final var a = toJson(previous);
    final var b = toJson(next);
    
    merged
    .append("  +- ").append(next.getId()).append("::").append(next.getDocType()).append(System.lineSeparator())
    .append("   -  ").append(a).append(System.lineSeparator())
    .append("   +  ").append(b).append(System.lineSeparator());
    
    
    for(final var entries : a.getMap().entrySet()) {
      final var changedFrom = entries.getValue();
      final var changedTo = b.getValue(entries.getKey());
      if(Objects.equal(changedFrom, changedTo)) {
        continue;
      }
      merged.append("   diff: ").append(entries.getKey())
        .append(" :: ")
        .append(changedFrom).append(" -> ").append(changedTo)
        .append(System.lineSeparator());
        
    }
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
