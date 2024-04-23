package io.resys.thena.structures.doc.commitlog;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

import io.resys.thena.api.entities.doc.DocCommit;
import io.resys.thena.api.entities.doc.DocEntity.IsDocObject;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLink;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
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
    
    if(entity instanceof GrimMissionLink) {
      final var link = ImmutableGrimMissionLink.builder().from((GrimMissionLink) entity)
          .transitives(null)
          .build();
      return JsonObject.mapFrom(link);
    } else if(entity instanceof GrimRemark) {
      final var link = ImmutableGrimRemark.builder().from((GrimRemark) entity)
          .transitives(null)
          .build();
      return JsonObject.mapFrom(link);
    }
    
    return JsonObject.mapFrom(entity);
  }
  
  public void remove(IsDocObject entity) {
    count_deleted++;
    removed.add(entity);
  }
  
  @SuppressWarnings({ "incomplete-switch" }) 
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