package io.resys.thena.structures.org.commitlog;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Objects;

import io.resys.thena.api.entities.org.OrgCommit;
import io.resys.thena.api.entities.org.ThenaOrgObject.IsOrgObject;
import io.vertx.core.json.JsonObject;



public class OrgCommitLogger {
  private final String tenantId;
  private final String commitId;
  private final OrgCommit commit;

  private int count_added;
  private int count_deleted;
  private int count_merged;
  private final StringBuilder added = new StringBuilder();
  private final StringBuilder merged = new StringBuilder();
  private final List<IsOrgObject> removed = new ArrayList<>();
    
  
  
  
  public OrgCommitLogger(String tenantId, OrgCommit commit) {
    super();
    this.tenantId = tenantId;
    this.commitId = commit.getCommitId();
    this.commit = commit;
  }

  public void add(IsOrgObject entity) {    
    count_added++;
    added
      .append("  + ").append(entity.getId()).append("::").append(entity.getDocType()).append(System.lineSeparator())
      .append("    ").append(JsonObject.mapFrom(entity)).append(System.lineSeparator());
  }
  
  public void remove(IsOrgObject entity) {
    count_deleted++;
    removed.add(entity);
  }
  
  private int compare(IsOrgObject a, IsOrgObject b) {
    if(a.getDocType() != b.getDocType()) {
      return a.getDocType().compareTo(b.getDocType());
    }
    /*
    switch (a.getDocType()) {
    
    case GRIM_ASSIGNMENT: {
      final GrimAssignment a1 = (GrimAssignment) a;
      final GrimAssignment a2 = (GrimAssignment) b;
      return ComparisonChain.start()
        .compare(a1.getAssignmentType(), a2.getAssignmentType())
        .compare(a1.getAssignee(), a2.getAssignee())
        .result();
    }
    }
    */
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
  
  public void merge(IsOrgObject previous, IsOrgObject next) {
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