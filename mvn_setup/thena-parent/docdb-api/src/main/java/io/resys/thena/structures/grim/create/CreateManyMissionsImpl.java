package io.resys.thena.structures.grim.create;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import io.resys.thena.api.actions.GrimCommitActions.CreateManyMissions;
import io.resys.thena.api.actions.GrimCommitActions.ManyMissionsEnvelope;
import io.resys.thena.api.actions.ImmutableManyMissionsEnvelope;
import io.resys.thena.api.entities.grim.GrimCommit;
import io.resys.thena.api.entities.grim.GrimLabel;
import io.resys.thena.api.entities.grim.ImmutableGrimCommit;
import io.resys.thena.api.entities.grim.ThenaGrimChanges.MissionChanges;
import io.resys.thena.spi.DbState;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.GrimInserts.GrimBatchForOne;
import io.resys.thena.structures.grim.GrimState;
import io.resys.thena.structures.grim.ImmutableGrimBatchForOne;
import io.resys.thena.structures.grim.commitlog.GrimCommitLogger;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateManyMissionsImpl implements CreateManyMissions {

  private final DbState state;
  private final String tenantId;
  
  private String author;
  private String message;
  private final LinkedHashMap<Collection<JsonObject>, Consumer<MissionChanges>> missions = new LinkedHashMap<>();
  
  @Override
  public CreateManyMissions commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public CreateManyMissions commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public CreateManyMissions addMission(Collection<JsonObject> newCommands, Consumer<MissionChanges> addMission) {
    RepoAssert.notNull(newCommands, () -> "newCommands can't be empty!");
    RepoAssert.notNull(addMission, () -> "addMission can't be empty!");
    missions.put(new ArrayList<JsonObject>(newCommands), addMission);
    return this;
  }

  @Override
  public Uni<ManyMissionsEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(missions, () -> "missions can't be empty!");

    return this.state.withGrimTransaction(tenantId, this::doInTx);
  }

  private Uni<ManyMissionsEnvelope> doInTx(GrimState tx) {
    return createRequest(tx).onItem().transformToUni(request -> createResponse(tx, request));
  }
  
  private Uni<ManyMissionsEnvelope> createResponse(GrimState tx, GrimBatchForOne request) {
    return tx.insert().batchMany(request).onItem().transform(rsp -> {
      return ImmutableManyMissionsEnvelope.builder()
          .repoId(tenantId)
          .log(rsp.getLog())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();      
    });
  }
  
  private Uni<GrimBatchForOne> createRequest(GrimState tx) {
    return tx.query().labels().findAll().onItem().transform(labels -> createRequest(tx, labels));
  }
  
  private GrimBatchForOne createRequest(GrimState tx, List<GrimLabel> labels) {
    final Map<String, GrimLabel> all_labels = labels.stream().collect(Collectors.toMap(e -> e.getId(), e -> e));
    final var start = ImmutableGrimBatchForOne.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();

    ImmutableGrimBatchForOne next = start;
    final GrimCommit parentCommit;
    if(this.missions.size() == 1) {
      parentCommit = null;
    } else {
      final var createdAt = OffsetDateTime.now();

      parentCommit = ImmutableGrimCommit.builder()
        .commitId(OidUtils.gen())
        .commitAuthor(author)
        .commitMessage(message)
        .createdAt(createdAt)
        .commitLog("batch of: " + this.missions.size() + " entries")
        .build();
      next.withCommits(parentCommit);
    }
    
    for(final var entry : this.missions.entrySet()) {
      final var logger = new GrimCommitLogger(tenantId, author, message, parentCommit, entry.getKey());
      final var newMission = new NewMissionBuilder(Collections.unmodifiableMap(all_labels), logger);
      entry.getValue().accept(newMission);
      final var created = newMission.close();
      created.getLabels().forEach(e -> all_labels.put(e.getId(), e));
      created.getUpdateLabels().forEach(e -> all_labels.put(e.getId(), e));      
      
      
      next = ImmutableGrimBatchForOne.builder()
          // merge init state
          .from(start)
          // merge mission state
          .from(created)
          // merge commit state
          .from(logger.close())
          .build();
    }
    
    return next;
  }
}
