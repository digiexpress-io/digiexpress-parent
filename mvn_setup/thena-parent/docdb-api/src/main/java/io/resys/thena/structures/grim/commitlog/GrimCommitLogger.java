package io.resys.thena.structures.grim.commitlog;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.thena.api.entities.grim.GrimAssignment;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimCommitViewer;
import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.api.entities.grim.GrimMission;
import io.resys.thena.api.entities.grim.GrimMissionData;
import io.resys.thena.api.entities.grim.GrimMissionLabel;
import io.resys.thena.api.entities.grim.GrimMissionLink;
import io.resys.thena.api.entities.grim.GrimObjective;
import io.resys.thena.api.entities.grim.GrimObjectiveGoal;
import io.resys.thena.api.entities.grim.GrimRemark;
import io.resys.thena.api.entities.grim.ImmutableGrimCommit;
import io.resys.thena.api.entities.grim.ImmutableGrimCommitTree;
import io.resys.thena.api.entities.grim.ThenaGrimObject.IsGrimObject;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.support.OidUtils;
import io.vertx.core.json.JsonObject;



public class GrimCommitLogger {
  private final String tenantId;;
  private final String commitId;
  private final ImmutableGrimCommit.Builder commit;
  private final GrimCommit parentCommit;
  private final ImmutableGrimBatchForOne.Builder next;
  private final Collection<JsonObject> commands;
  private final List<IsGrimObject> added = new ArrayList<>(); 
  private final List<IsGrimObject> removed = new ArrayList<>();
  private final List<IsGrimObject> updated = new ArrayList<>();
  
  public GrimCommitLogger(
      String tenantId,
      String author, 
      String message,
      GrimCommit parentCommit, 
      Collection<JsonObject> commands) {
    super();
    final var createdAt = OffsetDateTime.now();
    this.commitId = OidUtils.gen();
    this.tenantId = tenantId;
    this.parentCommit = parentCommit;
    this.commands = commands;
    this.commit = ImmutableGrimCommit.builder()
        .commitId(commitId)
        .commitAuthor(author)
        .commitMessage(message)
        .commitLog("")
        .createdAt(createdAt)
        .parentCommitId(parentCommit == null ? null : parentCommit.getCommitId());
    this.next = ImmutableGrimBatchForOne.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("");
    
    
  }


  public enum GrimOpType {
    ADD, MERGE, DELETE
  }
  public String getTenantId() {
    return tenantId;
  }
  public String getCommitId() {
    return commitId;
  }
  public GrimCommitLogger visit(GrimOpType type, GrimAssignment entity) {
    add(type, entity);
    return this;
  }
  public GrimCommitLogger visit(GrimOpType type, GrimCommitViewer entity) {
    add(type, entity);
    return this;
  }
  public GrimCommitLogger visit(GrimOpType type, GrimLabel entity) {
    add(type, entity);
    return this;
  }
  public GrimCommitLogger visit(GrimOpType type, GrimMission entity) {
    this.commit.missionId(entity.getId());
    add(type, entity);
    return this;
  }
  public GrimCommitLogger visit(GrimOpType type, GrimMissionData entity) {
    add(type, entity);
    return this;
  }
  public GrimCommitLogger visit(GrimOpType type, GrimMissionLabel entity) {
    add(type, entity);
    return this;
  }
  public GrimCommitLogger visit(GrimOpType type, GrimMissionLink entity) {
    add(type, entity);
    return this;
  }
  public GrimCommitLogger visit(GrimOpType type, GrimObjective entity) {
    add(type, entity);
    return this;
  }
  public GrimCommitLogger visit(GrimOpType type, GrimObjectiveGoal entity) {
    add(type, entity);
    return this;
  }
  public GrimCommitLogger visit(GrimOpType type, GrimRemark entity) {
    add(type, entity);
    return this;
  }
  
  private void add(GrimOpType type, IsGrimObject entity) {
    if(type == GrimOpType.ADD) {
      added.add(entity);
    } else if(type == GrimOpType.MERGE) {
      updated.add(entity);
    } else {
      removed.add(entity);
    }
    
    this.next.addCommitTrees(ImmutableGrimCommitTree.builder()
        .id(OidUtils.gen())
        .commitId(commitId)
        .commandType("thena-api")
        .commandBody(JsonObject.mapFrom(entity))
        .build());
  }
  public ImmutableGrimBatchForOne close() {
    for(final var command : this.commands) {
      this.next.addCommitTrees(ImmutableGrimCommitTree.builder()
          .id(OidUtils.gen())
          .commitId(commitId)
          .commandType("user-api")
          .commandBody(command)
          .build());
    }
    
    final var log = new StringBuilder();
    if(!added.isEmpty()) {
      log
      .append(System.lineSeparator())
      .append(" | created")
      .append(System.lineSeparator())
      .append("  + commit:          ").append(commitId).append(" added: ").append(added.size() + "").append(" entries")
      .append(System.lineSeparator());

      for(final var logEntries : this.added.stream().collect(Collectors.groupingBy(e -> e.getDocType(), Collectors.toList())).entrySet()) {
        log
        .append("  + ").append(logEntries.getKey()).append(": added ").append(logEntries.getValue().size())
        .append(System.lineSeparator());
        
        for(final var value : logEntries.getValue()) {
          log.append("  ++ ").append(JsonObject.mapFrom(value)).append(System.lineSeparator());
        }
      }
    }

    if(!removed.isEmpty()) {
      log
      .append(System.lineSeparator())
      .append(" | deleted")
      .append(System.lineSeparator())
      .append("  + commit:          ").append(commitId).append(" removed: ").append(removed.size() + "").append(" entries")
      .append(System.lineSeparator());

      for(final var logEntries : this.removed.stream().collect(Collectors.groupingBy(e -> e.getDocType(), Collectors.toList())).entrySet()) {
        log
        .append("  - ").append(logEntries.getKey()).append(": deleted ").append(logEntries.getValue().size())
        .append(System.lineSeparator());
        
        for(final var value : logEntries.getValue()) {
          log.append("  -- ").append(JsonObject.mapFrom(value)).append(System.lineSeparator());
        }
      }
    }

    if(!removed.isEmpty()) {
      log
      .append(System.lineSeparator())
      .append(" | merged")
      .append(System.lineSeparator())
      .append("  + commit:          ").append(commitId).append(" merged: ").append(updated.size() + "").append(" entries")
      .append(System.lineSeparator());

      for(final var logEntries : this.updated.stream().collect(Collectors.groupingBy(e -> e.getDocType(), Collectors.toList())).entrySet()) {
        log
        .append("  + ").append(logEntries.getKey()).append(": merged ").append(logEntries.getValue().size())
        .append(System.lineSeparator());
        
        for(final var value : logEntries.getValue()) {
          log.append("  ++ ").append(JsonObject.mapFrom(value)).append(System.lineSeparator());
        }
      }
    }
    
    
    this.next.addCommits(this.commit.commitLog(log.toString()).build()).log("");
    return this.next.build();
  }
}
