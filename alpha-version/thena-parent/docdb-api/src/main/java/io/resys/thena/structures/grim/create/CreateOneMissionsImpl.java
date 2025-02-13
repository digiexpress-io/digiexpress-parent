package io.resys.thena.structures.grim.create;

import java.time.OffsetDateTime;
import java.util.function.Consumer;

import io.resys.thena.api.actions.GrimCommitActions.CreateOneMission;
import io.resys.thena.api.actions.GrimCommitActions.OneMissionEnvelope;
import io.resys.thena.api.actions.ImmutableOneMissionEnvelope;
import io.resys.thena.api.entities.CommitResultStatus;
import io.resys.thena.api.entities.grim.ImmutableGrimCommit;
import io.resys.thena.api.entities.grim.ThenaGrimNewObject.NewMission;
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
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class CreateOneMissionsImpl implements CreateOneMission {

  private final DbState state;
  private final String tenantId;
  
  private String author;
  private String message;
  private Consumer<NewMission> mission;
  
  @Override
  public CreateOneMission commitAuthor(String author) {
    this.author = RepoAssert.notEmpty(author, () -> "author can't be empty!"); 
    return this;
  }
  @Override
  public CreateOneMission commitMessage(String message) {
    this.message = RepoAssert.notEmpty(message, () -> "message can't be empty!");
    return this;
  }
  @Override
  public CreateOneMission mission(Consumer<NewMission> addMission) {
    RepoAssert.notNull(addMission, () -> "addMission can't be empty!");
    mission = addMission;
    return this;
  }

  @Override
  public Uni<OneMissionEnvelope> build() {
    RepoAssert.notEmpty(tenantId, () -> "tenantId can't be empty!");
    RepoAssert.notEmpty(author, () -> "author can't be empty!");
    RepoAssert.notEmpty(message, () -> "message can't be empty!");
    RepoAssert.notNull(mission, () -> "mission can't be empty!");

    final var scope = ImmutableTxScope.builder().commitAuthor(author).commitMessage(message).tenantId(tenantId).build();
    return this.state.withGrimTransaction(scope, this::doInTx);
  }

  private Uni<OneMissionEnvelope> doInTx(GrimState tx) {
    return createRequest(tx).onItem().transformToUni(request -> createResponse(tx, request))
        .onFailure(CreateOneMissionException.class).recoverWithItem(ex -> {
          final CreateOneMissionException error = (CreateOneMissionException) ex;          
          return ImmutableOneMissionEnvelope.builder()
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
  
  private Uni<OneMissionEnvelope> createResponse(GrimState tx, GrimBatchMissions request) {
    return tx.insert().batchMany(request).onItem().transform(rsp -> {
      if(rsp.getStatus() == BatchStatus.CONFLICT || rsp.getStatus() == BatchStatus.ERROR) {
        throw new CreateOneMissionException("Failed to create mission!", rsp);
      }
      
      final OneMissionEnvelope result = ImmutableOneMissionEnvelope.builder()
          .repoId(tenantId)
          .mission(rsp.getMissions().iterator().next())
          .addAllMessages(rsp.getMessages())
          .status(BatchStatus.mapStatus(rsp.getStatus()))
          .build();
      return result;
    });
  }
  
  private Uni<GrimBatchMissions> createRequest(GrimState tx) {
  
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

          .build()
    );
    
    final var newMission = new NewMissionBuilder(logger);
    this.mission.accept(newMission);
    final var created = newMission.close();
    
    final var missionId = created.getMissions().iterator().next().getId();
    
    next = ImmutableGrimBatchMissions.builder()
        .from(start)
        .from(created)
        .from(logger.withMissionId(missionId).close())
        .build();
  
    return Uni.createFrom().item(next);
  }
  
  public static class CreateOneMissionException extends RuntimeException {
    private static final long serialVersionUID = -6202574733069488724L;
    private final GrimBatchMissions batch;
    public CreateOneMissionException(String message, GrimBatchMissions batch) {
      super(message + System.lineSeparator() + " " +
          String.join(System.lineSeparator() + " ", batch.getMessages().stream().map(e -> e.getText()).toList()));
      
      batch.getMessages().stream().filter(e -> e.getException() != null).forEach(e -> addSuppressed(e.getException()));
      this.batch = batch;
    }
    public GrimBatchMissions getBatch() {
      return batch;
    }
  }
}
