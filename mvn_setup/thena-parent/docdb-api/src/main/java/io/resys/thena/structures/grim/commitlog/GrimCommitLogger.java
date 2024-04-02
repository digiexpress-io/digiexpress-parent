package io.resys.thena.structures.grim.commitlog;

import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public class GrimCommitLogger {
  private final String tenantId;
  private final String commitId;
  
  private int count_added;
  private int count_deleted;
  private int count_merged;
  private final StringBuilder added = new StringBuilder();
  private final StringBuilder merged = new StringBuilder();
  private final StringBuilder removed = new StringBuilder();
  
  public void add(IsGrimObject entity) {    
    added
      .append("  + ").append(entity.getId()).append(System.lineSeparator())
      .append("    ").append(JsonObject.mapFrom(entity)).append(System.lineSeparator());
  }
  
  public void remove(IsGrimObject entity) {
    
  }
  public void update(IsGrimObject previous, IsGrimObject next) {
    
  }
 
  public String build() {
    return new StringBuilder()
      .append("commit: ").append(commitId).append(", tenant: ").append(tenantId)
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
      .append(removed)
      
      .append(System.lineSeparator())
      .append(" | merged")
      .append(System.lineSeparator())
      .append("  +- merged: ").append(count_merged).append(" entries")
      .append(System.lineSeparator())
      .append(merged)
      .toString();


  }
}