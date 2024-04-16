package io.resys.thena.structures.grim.commitlog;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimMissionLink;
import io.resys.thena.api.entities.grim.ImmutableGrimRemark;
import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.vertx.core.json.JsonObject;



public class GrimCommitLogger {
  private final String tenantId;
  private final String commitId;
  private final GrimCommit commit;

  private int count_added;
  private int count_deleted;
  private int count_merged;
  private final StringBuilder added = new StringBuilder();
  private final StringBuilder merged = new StringBuilder();
  private final List<IsGrimObject> removed = new ArrayList<>();
    
  
  
  
  public GrimCommitLogger(String tenantId, GrimCommit commit) {
    super();
    this.tenantId = tenantId;
    this.commitId = commit.getCommitId();
    this.commit = commit;
  }

  public void add(IsGrimObject entity) {    
    count_added++;
    added
      .append("  + ").append(entity.getId()).append("::").append(entity.getDocType()).append(System.lineSeparator())
      .append("    ").append(toJson(entity)).append(System.lineSeparator());
  }
  
  private JsonObject toJson(IsGrimObject entity) {
    
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
  
  public void remove(IsGrimObject entity) {
    count_deleted++;
    removed.add(entity);
  }
  
  private int compare(IsGrimObject a, IsGrimObject b) {
    if(a.getDocType() != b.getDocType()) {
      return a.getDocType().compareTo(b.getDocType());
    }
    
    switch (a.getDocType()) {
    case GRIM_ASSIGNMENT: {
      final GrimAssignment a1 = (GrimAssignment) a;
      final GrimAssignment a2 = (GrimAssignment) b;
      return ComparisonChain.start()
        .compare(a1.getAssignmentType(), a2.getAssignmentType())
        .compare(a1.getAssignee(), a2.getAssignee())
        .result();
    }      
    case GRIM_MISSION_LINKS: {
      final GrimMissionLink a1 = (GrimMissionLink) a;
      final GrimMissionLink a2 = (GrimMissionLink) b;
      return ComparisonChain.start()
        .compare(a1.getLinkType(), a2.getLinkType())
        .compare(a1.getExternalId() + "", a2.getExternalId() + "")
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
      .append("    ").append(JsonObject.mapFrom(entity)).append(System.lineSeparator());
    });;
    
    return result.toString();
  }
  
  public void merge(IsGrimObject previous, IsGrimObject next) {
    count_merged++;
    final var a = JsonObject.mapFrom(previous);
    final var b = JsonObject.mapFrom(next);
    
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