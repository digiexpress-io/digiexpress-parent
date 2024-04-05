package io.resys.thena.structures.grim.modify;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.resys.thena.api.actions.GrimCommitActions.ManyMissionsEnvelope;
import io.resys.thena.api.actions.GrimCommitActions.ModifyManyMissions;
import io.resys.thena.api.actions.ImmutableManyMissionsEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ImmutableGrimCommit;
import io.resys.thena.api.entities.grim.ThenaGrimContainers.GrimMissionContainer;
import io.resys.thena.api.entities.grim.ThenaGrimMergeObject.MergeMission;
import io.resys.thena.api.entities.grim.ThenaGrimObject.GrimDocType;
import io.resys.thena.api.envelope.ImmutableMessage;
import io.resys.thena.spi.DbState;
import io.resys.thena.spi.ImmutableTxScope;
import io.resys.thena.structures.BatchStatus;
import io.resys.thena.structures.grim.GrimInserts.GrimBatchMissions;
import io.resys.thena.structures.grim.GrimState;
import io.resys.thena.structures.grim.ImmutableGrimBatchMissions;
import io.resys.thena.structures.grim.commitlog.GrimCommitBuilder;
import io.resys.thena.support.OidUtils;
import io.resys.thena.support.RepoAssert;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class ModifyManyMissionsImpl implements ModifyManyMissions {

  private final DbState state;
  private final String tenantId;
  
  private String author;
  private String message;
  private final Map<String, Consumer<MergeMission>> missions = new LinkedHashMap<>();
  private ImmutableGrimCommit parentCommit;
  
  
  @Override
  public ModifyManyMissions commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public ModifyManyMissions commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public ModifyManyMissions modifyMission(String missionId, Consumer<MergeMission> modifyMission) {
    RepoAssert.notNull(modifyMission, () -> "modifyMission can't be empty!");
    RepoAssert.isTrue(!missions.containsKey(missionId), () -> "modifyMission with id: '" + missionId + "' already exists!");
    missions.put(missionId, modifyMission);
    return this;
  }
  @Override
  public Uni<ManyMissionsEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notEmpty(missions, () -> "missions can't be empty!");

    // Create parent commit to bind all 
    if(this.missions.size() == 1) {
      parentCommit = null;
    } else {
      parentCommit = ImmutableGrimCommit.builder()
        .commitId(OidUtils.gen())
        .commitAuthor(author)
        .commitMessage(message)
        .createdAt(OffsetDateTime.now())
        .commitLog("batch of: " + this.missions.size() + " entries")
        .build();
    }
    
    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withGrimTransaction(scope, this::doInTx);
  }

  private Uni<ManyMissionsEnvelope> doInTx(GrimState tx) {
    return createRequest(tx)
        .collect().asList()
        .onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(ModifyManyMissionsException.class).recoverWithItem(ex -> {
          final ModifyManyMissionsException error = (ModifyManyMissionsException) ex;          
          return ImmutableManyMissionsEnvelope.builder()
            .repoId(tenantId)
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'").append(" is rejected.")
                  .append(System.lineSeparator())
                  .append("Message: ").append(error.getMessage())
                  .toString())
                .exception(error)
                .build())
            .status(CommitResultStatus.ERROR)
          .build();
        });
  }

  private ManyMissionsEnvelope validateRequest(GrimState tx, List<GrimBatchMissions> request) {
    if(request.size() != this.missions.size()) {
      final var found = request.stream()
          .map(i -> i.getMissions().iterator().next().getId())
          .toList();
      final var source = this.missions.keySet().stream().toList();
      final var notFound = new ArrayList<>(source);
      notFound.removeAll(found);
      return ImmutableManyMissionsEnvelope.builder()
            .repoId(tenantId)
            .log("")
            .addMessages(ImmutableMessage.builder()
                .text(new StringBuilder()
                  .append("Commit to: '").append(tenantId).append("'")
                  .append(" is rejected.")
                  .append(" Could not find all missions: expected: '").append(this.missions.size()).append("' but found: '").append(request.size()).append("'!\r\n")
                  .append("  - not found: ").append(String.join(",", notFound))
                  .toString())
                .build())
            .status(CommitResultStatus.ERROR)
            .build();
    }
    return null;
  }
  
  private Uni<ManyMissionsEnvelope> createResponse(GrimState tx, List<GrimBatchMissions> request) {
    final var isErrors = validateRequest(tx, request);
    if(isErrors != null) {
      return Uni.createFrom().item(isErrors);
    }
    
    // Merge requests
    final var start = ImmutableGrimBatchMissions.builder()
        .tenantId(tenantId)
        .log("")
        .status(BatchStatus.OK);
    if(parentCommit != null) {
      start.addCommits(parentCommit);
    }
    
    
    request.forEach(r -> start.from(r));
    
    // Patch all in current TX
    return tx.insert().batchMany(start.build()).onItem().transform(rsp -> {
      
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new ModifyManyMissionsException("Failed to modify missions!", rsp);
      }
      
      return ImmutableManyMissionsEnvelope.builder()
          .repoId(tenantId)
          .log(rsp.getLog())
          .missions(rsp.getMissions())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();      
    });
  }
  
  private Multi<GrimBatchMissions> createRequest(GrimState tx) {
    return tx.query().missions()
    .missionId(this.missions.keySet().toArray(new String[]{}))
    .excludeDocs(GrimDocType.GRIM_COMMANDS, GrimDocType.GRIM_COMMIT_VIEWER, GrimDocType.GRIM_COMMIT)
    .findAll().onItem().transform(labels -> createRequest(tx, labels));
  }
  
  
  private GrimBatchMissions createRequest(GrimState tx, GrimMissionContainer container) {
    RepoAssert.isTrue(container.getMissions().size() == 1, () -> "Mission container must be grouped by missions, one mission per container!");
    
    final var missionId =  container.getMissions().keySet().iterator().next();
    
    final var start = ImmutableGrimBatchMissions.builder()
        .tenantId(tenantId)
        .status(BatchStatus.OK)
        .log("")
        .build();
    final var createdAt = OffsetDateTime.now();
    
    ImmutableGrimBatchMissions next = start;    
    final var logger = new GrimCommitBuilder(tenantId, 
        ImmutableGrimCommit.builder()
          .commitId(OidUtils.gen())
          .commitAuthor(author)
          .commitMessage(message)
          .commitLog("")
          .createdAt(createdAt)
          .parentCommitId(parentCommit == null ? null : parentCommit.getCommitId())
          .build()
    );
    
    final var mergeMission = new MergeMissionBuilder(container, logger);
    this.missions.get(missionId).accept(mergeMission);
    final var created = mergeMission.close();
    
    next = ImmutableGrimBatchMissions.builder()
        .from(start)
        .from(created)
        .from(logger.withMissionId(missionId).close())
        .build();
    return next;
  }
  
  
  public static class ModifyManyMissionsException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final GrimBatchMissions batch;
    public ModifyManyMissionsException(String message, GrimBatchMissions batch) {
      super(message);
      this.batch = batch;
    }
    public GrimBatchMissions getBatch() {
      return batch;
    }
  }
}
