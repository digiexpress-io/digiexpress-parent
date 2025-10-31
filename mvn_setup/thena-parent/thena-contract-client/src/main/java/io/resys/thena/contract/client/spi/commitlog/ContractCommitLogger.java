package io.resys.thena.contract.client.spi.commitlog;

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

import io.resys.thena.contract.client.entities.Capability;
import io.resys.thena.contract.client.entities.Commit;
import io.resys.thena.contract.client.entities.ContractEntity;
import io.resys.thena.contract.client.entities.ImmutableCapability;
import io.resys.thena.contract.client.entities.ImmutableReference;
import io.resys.thena.contract.client.entities.Reference;
import io.vertx.core.json.JsonObject;



public class ContractCommitLogger {
  private final String tenantId;
  private final String commitId;
  private final Commit commit;
  public static List<String> SKIP = Arrays.asList(
      "commitId",
      "createdWithCommitId");
  
  
  private int count_added;
  private int count_deleted;
  private int count_merged;
  private final StringBuilder added = new StringBuilder();
  private final StringBuilder merged = new StringBuilder();
  private final List<ContractEntity> removed = new ArrayList<>();
    

  public ContractCommitLogger(String tenantId, Commit commit) {
    super();
    this.tenantId = tenantId;
    this.commitId = commit.getCommitId();
    this.commit = commit;
  }

  public void add(ContractEntity entity) {    
    count_added++;
    added
      .append("  + ").append(entity.getId()).append("::").append(entity.getDocType()).append(System.lineSeparator())
      .append("    ").append(toJson(entity)).append(System.lineSeparator());
  }
  public void remove(ContractEntity entity) {
    count_deleted++;
    removed.add(entity);
  }
  
  public void merge(ContractEntity previous, ContractEntity next) {

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
  
  private JsonObject toJson(ContractEntity entity) {
    
    if(entity instanceof Reference) {
      final var link = ImmutableReference.builder().from((Reference) entity)
          .transitives(null)
          .build();
      return JsonObject.mapFrom(link);
    } else if(entity instanceof Capability) {
      final var link = ImmutableCapability.builder().from((Capability) entity)
          .transitives(null)
          .build();
      return JsonObject.mapFrom(link);
    }
    
    return JsonObject.mapFrom(entity);
  }
  
  @SuppressWarnings({ "incomplete-switch" }) 
  private int compare(ContractEntity a, ContractEntity b) {
    if(a.getDocType() != b.getDocType()) {
      return a.getDocType().compareTo(b.getDocType());
    }
    
    switch (a.getDocType()) {
    case REFERENCE: {
      final Reference a1 = (Reference) a;
      final Reference a2 = (Reference) b;
      return ComparisonChain.start()
        .compare(a1.getReferenceType(), a2.getReferenceType())
        .compare(a1.getReferenceValue(), a2.getReferenceValue())
        .result();
    }      
    case CAPABILITY: {
      final Capability a1 = (Capability) a;
      final Capability a2 = (Capability) b;
      return ComparisonChain.start()
        .compare(a1.getCapabilityType(), a2.getCapabilityType())
        .compare(a1.getCapabilityCode() + "", a2.getCapabilityCode() + "")
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
